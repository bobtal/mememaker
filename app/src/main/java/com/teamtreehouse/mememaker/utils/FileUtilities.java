package com.teamtreehouse.mememaker.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.teamtreehouse.mememaker.MemeMakerApplicationSettings;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Evan Anger on 7/28/14.
 */
public class FileUtilities {

    public static void saveAssetImage(Context context, String assetName) {
        // get the internal storage folder
        File fileDirectory = getFileDirectory(context);

        // get file in the internal storage folder with name = assetName
        File fileToWrite = new File(fileDirectory, assetName);

        // get assets manager to access assets files
        AssetManager assetManager = context.getAssets();
        try {
            // open input stream to read from the assets file
            InputStream in = assetManager.open(assetName);

            // open output stream to write to the internal storage file
            // FileOutputStream class is a subclass of OutputStream
            FileOutputStream out = new FileOutputStream(fileToWrite);

            // copy from inputStream to outputStream
            copyFile(in, out);

            // close the streams after finish copying
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // another way to create a FileOutputStream using the context.
        // Allows for MODE_PRIVATE, there are other deprecated modes
        // MODE_WORLD_READABLE MODE_WORLD_WRITABLE used to make internally stored files
        // available outside the application, but it's not the recommended approach
//        FileOutputStream out2 = context.openFileInput(fileToWrite.getAbsolutePath(), Context.MODE_PRIVATE);
    }

    public static File getFileDirectory(Context context) {
        MemeMakerApplicationSettings settings = new MemeMakerApplicationSettings(context);
        // get value from sharedPreferences for which type of storage to use
        String storageType = settings.getStoragePreference();
        if (storageType.equals(StorageType.INTERNAL)) {
            return context.getFilesDir();
        } else {
            if(isExternalStorageAvailable()) {
                if(storageType.equals(StorageType.PRIVATE_EXTERNAL)) {
                    return context.getExternalFilesDir(null);
                } else  { // PUBLIC_EXTERNAL
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                }
            } else { // if external storage is not available, revert back to using internal storage
                return context.getFilesDir();
            }
        }
    }

    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static File[] listFiles(Context context) {
        File fileDirectory = getFileDirectory(context);
        File[] filteredFiles = fileDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getAbsolutePath().contains(".jpg")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        return filteredFiles;
    }

    public static Uri saveImageForSharing(Context context, Bitmap bitmap,  String assetName) {
        File fileToWrite = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), assetName);

        try {
            FileOutputStream outputStream = new FileOutputStream(fileToWrite);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return Uri.fromFile(fileToWrite);
        }
    }


    public static void saveImage(Context context, Bitmap bitmap, String name) {
        File fileDirectory = getFileDirectory(context);
        File fileToWrite = new File(fileDirectory, name);

        try {
            FileOutputStream outputStream = new FileOutputStream(fileToWrite);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
