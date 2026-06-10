package com.yudi.asmara.expensereport.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class FolderDeletion {

    public static void deleteFolder(String folderName) {
        String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + folderName;
        File folderToDelete = new File(folderPath);
        if (folderToDelete.exists()) {
            if (deleteRecursive(folderToDelete)) {
                Log.d("FolderDeletion Legacy", "Folder deleted successfully");
            } else {
                Log.e("FolderDeletion Legacy", "Failed to delete folder");
            }
        } else {
            Log.e("FolderDeletion Legacy", "Folder does not exist");
        }
    }

    private static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }
}
