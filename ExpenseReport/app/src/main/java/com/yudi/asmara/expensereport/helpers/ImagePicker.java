package com.yudi.asmara.expensereport.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import com.yudi.asmara.expensereport.utils.AppConfig;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePicker {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_PICK = 2;
    public static File storageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), AppConfig.FOLDER_NAME);
    private static Uri photoURI;

    public static void takePhotoFromCamera(Activity activity) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File image = new File(storageDir, "IMG_" + timeStamp + ".jpg");
        photoURI = FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".provider", image);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        activity.startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
    }

    public static void takePhotoFromCamera(Activity activity, ActivityResultLauncher<Intent> launcher) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File image = new File(storageDir, "IMG_" + timeStamp + ".jpg");
        photoURI = FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".provider", image);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        launcher.launch(takePhotoIntent);
    }

    public static void takePhotoFromCamera(Activity activity, ActivityResultLauncher<Intent> launcher, String folderName) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (folderName.isEmpty() || folderName.equals("null")) {
            folderName = "00";
        }

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), AppConfig.FOLDER_NAME + "/" + folderName);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File image = new File(storageDir, "IMG_" + timeStamp + ".jpg");
        photoURI = FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".provider", image);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        launcher.launch(takePhotoIntent);
    }

    public static void pickPhotoFromGallery(Activity activity) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        photoPickerIntent.setType("image/*");
        activity.startActivityForResult(photoPickerIntent, REQUEST_IMAGE_PICK);
    }

    public static void pickPhotoFromGallery(Activity activity, ActivityResultLauncher<Intent> launcher) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        photoPickerIntent.setType("image/*");
        launcher.launch(photoPickerIntent);
    }


    // Add onActivityResult method to get the selected image
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Image captured from camera
                // Handle the captured image here
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Image picked from gallery
                // Handle the picked image here
            }
        }
    }

    public static Uri getCurrentPhotoURI() {
        return photoURI;
    }
}
