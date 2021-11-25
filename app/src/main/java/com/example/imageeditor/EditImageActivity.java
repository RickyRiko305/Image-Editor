package com.example.imageeditor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.example.imageeditor.MainActivity.bitmap;
import static com.example.imageeditor.MainActivity.imageFileName;
import static com.example.imageeditor.MainActivity.uri;
import static com.example.imageeditor.MainActivity.uri1;
import static com.example.imageeditor.MainActivity.currentPhotoPat;

public class EditImageActivity extends AppCompatActivity {

    ImageView imageView;
    Button rotateButton;
    Button saveButton;
    Button cropButton;
    int mCurrRotation = 0;
    static Bitmap rotateBitmap;
    static Bitmap cropThenRotateBitmap;
    static Bitmap rotateThenCropBitmap;
    final int PIC_CROP = 1;
    static Bitmap croppedBitmap;
    static Uri resultUri;
    static Bitmap unChangedBitmap;
    boolean isRotate = false;
    static Bitmap bitmapBasic;
    float fromRotation;
    float toRotation;


    public void makeBitmapNull(){
        mCurrRotation=0;
        toRotation=0;
        fromRotation=0;
        rotateBitmap=null;
        croppedBitmap=null;
        rotateThenCropBitmap=null;
        cropThenRotateBitmap=null;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void undo(View view) {
        Matrix matrix = new Matrix();


        toRotation = mCurrRotation += 90;

        final RotateAnimation rotateAnimation = new RotateAnimation(
                fromRotation, 0, imageView.getWidth() / 2, imageView.getHeight() / 2);

        rotateAnimation.setDuration(1000);
        rotateAnimation.setFillAfter(true);


        matrix.setRotate(toRotation);
        System.out.println(toRotation + "TO ROTATION");
        System.out.println(fromRotation + "FROM ROTATION");
        if (croppedBitmap != null) {
            cropThenRotateBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(), matrix, true);
            //resultUri = getImageUri(this,cropThenRotateBitmap);
        } else {
            rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //resultUri = getImageUri(this,rotateBitmap);
        }

        imageView.setImageBitmap(bitmap);
        imageView.startAnimation(rotateAnimation);
        makeBitmapNull();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void crop(View view) {

        if(uri1!=null){
            CropImage.activity(uri1)
                    .start(this);

        }
        else if (uri!=null){
            CropImage.activity(uri)
                    .start(this);
        }
        else if(cropThenRotateBitmap!=null){
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            cropThenRotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            CropImage.activity(getImageUri(this, cropThenRotateBitmap))
                    .start(this);
        }
        else if(bitmap!=null){
            CropImage.activity(getImageUri(this,bitmap))
                    .start(this);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                imageView.setImageURI(resultUri);
//                Matrix matrix = new Matrix();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                System.out.println(imageView.getRotation());
                croppedBitmap = bitmapDrawable.getBitmap();
                //resultUri = getImageUri(this,croppedBitmap);
                if (isRotate) {
                    rotateThenCropBitmap = croppedBitmap;
                }
                //BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                cropThenRotateBitmap = bitmapDrawable.getBitmap();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void save(View view) throws IOException {


        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        ContentResolver resolver = getContentResolver();
        //Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        resultUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (cropThenRotateBitmap != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            cropThenRotateBitmap = bitmapDrawable.getBitmap();
            Matrix matrix = new Matrix();
            matrix.setRotate(toRotation);
            cropThenRotateBitmap = Bitmap.createBitmap(cropThenRotateBitmap, 0, 0, cropThenRotateBitmap.getWidth(), cropThenRotateBitmap.getHeight(), matrix, true);
            resultUri = getImageUri(this, cropThenRotateBitmap);
           // makeBitmapNull();

        } else if (rotateThenCropBitmap != null) {
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
//            rotateThenCropBitmap = bitmapDrawable.getBitmap();
            resultUri = getImageUri(this, rotateThenCropBitmap);
            //makeBitmapNull();

        } else if (rotateBitmap != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            rotateBitmap = bitmapDrawable.getBitmap();
            resultUri = getImageUri(this, rotateBitmap);
            //makeBitmapNull();
        } else if (croppedBitmap != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            croppedBitmap = bitmapDrawable.getBitmap();
            resultUri = getImageUri(this, croppedBitmap);
            //makeBitmapNull();
        } else if (bitmap != null) {
            resultUri = getImageUri(this, bitmap);
            //makeBitmapNull();
        }
        OutputStream imageOutStream = null;

        try {
            if (resultUri == null) {
                //throw new IOException("Failed to insert MediaStore row");
            }

            imageOutStream = resolver.openOutputStream(resultUri);
            if (cropThenRotateBitmap != null) {
                if (!cropThenRotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            } else if (rotateThenCropBitmap != null) {
                if (!rotateThenCropBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            } else if (croppedBitmap != null) {
                if (!croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            } else if (rotateBitmap != null) {
                if (!rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            } else {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            }

            Toast.makeText(this, "Imave Saved", Toast.LENGTH_SHORT).show();
            Log.i("Edit : ","Imave Saved");

        } finally {
            if (imageOutStream != null) {
                imageOutStream.close();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                finish();
                startActivity(intent);
            }
        }

    }

    public void rotate(View view) {
        isRotate = true;
        mCurrRotation %= 360;
        Matrix matrix = new Matrix();


        System.out.println(imageView.getRotation());
        fromRotation = mCurrRotation;
        toRotation = mCurrRotation += 90;

        final RotateAnimation rotateAnimation = new RotateAnimation(
                fromRotation, toRotation, imageView.getWidth() / 2, imageView.getHeight() / 2);

        rotateAnimation.setDuration(1000);
        rotateAnimation.setFillAfter(true);


        matrix.setRotate(toRotation);
        System.out.println(toRotation + "TO ROTATION");
        System.out.println(fromRotation + "FROM ROTATION");
        if (croppedBitmap != null) {
            cropThenRotateBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(), matrix, true);
            //resultUri = getImageUri(this,cropThenRotateBitmap);
        } else {
            rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //resultUri = getImageUri(this,rotateBitmap);
        }
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        cropThenRotateBitmap = bitmapDrawable.getBitmap();
        cropThenRotateBitmap = Bitmap.createBitmap(cropThenRotateBitmap, 0, 0, cropThenRotateBitmap.getWidth(), cropThenRotateBitmap.getHeight(), matrix, true);


        imageView.startAnimation(rotateAnimation);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        isStoragePermissionGranted();

        imageView = findViewById(R.id.editImageView);
        rotateButton = findViewById(R.id.rotateButton);
        saveButton = findViewById(R.id.saveButton);
        cropButton = findViewById(R.id.cropButton);
        ActivityCompat.requestPermissions(EditImageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        try {
            imageView.setImageBitmap(bitmap);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted");
                return true;
            } else {

                //Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            //Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}