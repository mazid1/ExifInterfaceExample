package me.mazid.imageloader;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_FOR_CAMERA = 111;
    private static final int REQUEST_PERMISSIONS_FOR_GALLERY = 333;

    private static final int REQUEST_CAPTURE_IMG = 222;
    private static final int REQUEST_PICK_IMG = 444;

    private static final List<String> REQUIRED_PERMISSIONS_FOR_CAMERA = new ArrayList<>();
    private static final List<String> REQUIRED_PERMISSIONS_FOR_GALLERY = new ArrayList<>();

    static {
        REQUIRED_PERMISSIONS_FOR_CAMERA.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        REQUIRED_PERMISSIONS_FOR_CAMERA.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        REQUIRED_PERMISSIONS_FOR_CAMERA.add(Manifest.permission.CAMERA);

        REQUIRED_PERMISSIONS_FOR_GALLERY.add(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private static Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermissionAndOpenCamera() {
        List<String> permissionsToBeRequested = new ArrayList<>();
        for(String permission : REQUIRED_PERMISSIONS_FOR_CAMERA) {
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToBeRequested.add(permission);
            }
        }
        if(!permissionsToBeRequested.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToBeRequested.toArray(new String[permissionsToBeRequested.size()]),
                    REQUEST_PERMISSIONS_FOR_CAMERA
            );
        } else {
            openCamera();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestGalleryPermissionAndOpenGallery() {
        List<String> permissionsToBeRequested = new ArrayList<>();
        for(String permission : REQUIRED_PERMISSIONS_FOR_GALLERY) {
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToBeRequested.add(permission);
            }
        }
        if(!permissionsToBeRequested.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToBeRequested.toArray(new String[permissionsToBeRequested.size()]),
                    REQUEST_PERMISSIONS_FOR_GALLERY
            );
        } else {
            openGallery();
        }
    }

    private void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        this.startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMG);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(galleryIntent, REQUEST_PICK_IMG);
    }

    public void onClickCamera(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestCameraPermissionAndOpenCamera();
        } else {
            openCamera();
        }
    }

    public void onClickGallery(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestGalleryPermissionAndOpenGallery();
        } else {
            openGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case REQUEST_PERMISSIONS_FOR_CAMERA: {
                // If request is cancelled, the result array will be empty.
                if(grantResults.length > 0) {
                    boolean allPermissionsGranted = true;
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            allPermissionsGranted = false;
                        }
                    }
                    if(allPermissionsGranted){
                        openCamera();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case REQUEST_PERMISSIONS_FOR_GALLERY: {
                // If request is cancelled, the result array will be empty.
                if (grantResults.length > 0) {
                    boolean allPermissionsGranted = true;
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            allPermissionsGranted = false;
                        }
                    }
                    if(allPermissionsGranted){
                        openGallery();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if(requestCode == REQUEST_PICK_IMG && resultCode == RESULT_OK && data != null) {
                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(
                        selectedImage,
                        filePathColumn,
                        null,
                        null,
                        null
                );
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgDecodableString, options);

                int reqWidth = 480, reqHeight = 800;

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                ImageViewerActivity.setmBitmap(BitmapFactory.decodeFile(imgDecodableString, options));

                ExifInterface exif = null;
                try {
                    File pictureFile = new File(imgDecodableString);
                    exif = new ExifInterface(pictureFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int orientation = ExifInterface.ORIENTATION_NORMAL;

                if (exif != null)
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                performRotation(orientation);

                Intent intent = new Intent(this, ImageViewerActivity.class);
                startActivity(intent);

            } else if(requestCode == REQUEST_CAPTURE_IMG && resultCode == RESULT_OK) {

                int reqWidth = 480, reqHeight = 800;
                try {
                    InputStream inStream;
                    try {
                        inStream = getContentResolver().openInputStream(imageUri);

                        //Decode image size
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;

                        BitmapFactory.decodeStream(inStream, null, options);
                        if(inStream != null) inStream.close();

                        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                        options.inJustDecodeBounds = false;

                        inStream = getContentResolver().openInputStream(imageUri);
                        ImageViewerActivity.setmBitmap(BitmapFactory.decodeStream(inStream, null, options));
                        if(inStream != null) inStream.close();

                        inStream = getContentResolver().openInputStream(imageUri);

                        ExifInterface exif = null;

                        try {
                            //File pictureFile = new File(imgDecodableString);
                            if (Build.VERSION.SDK_INT >= 24) {
                                exif = new ExifInterface(inStream);
                            }
                            else {
                                exif = new ExifInterface(imageUri.getPath());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        int orientation = ExifInterface.ORIENTATION_NORMAL;

                        if (exif != null)
                            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        performRotation(orientation);

                        inStream.close();
                    } catch (Exception e) {
                        Toast.makeText(this, "Exception 1", Toast.LENGTH_SHORT).show();
                    }

                    Intent intent = new Intent(this, ImageViewerActivity.class);
                    startActivity(intent);

                } catch (Exception e) {
                    Toast.makeText(this, "Exception 2", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "You did not pick any image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Exception 3", Toast.LENGTH_SHORT).show();
        }
    }

    private void performRotation(int orientation) {
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                ImageViewerActivity.setmBitmap(rotateBitmap(ImageViewerActivity.getmBitmap(), 90));
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                ImageViewerActivity.setmBitmap(rotateBitmap(ImageViewerActivity.getmBitmap(), 180));
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                ImageViewerActivity.setmBitmap(rotateBitmap(ImageViewerActivity.getmBitmap(), 270));
                break;
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
