package org.sunbird.utils;

import android.content.Context;

import org.sunbird.analytics.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by apple on 31/03/17.
 */
public class FileUtil {

    private static final String TAG = "FileUtil";
    private static final String INTERNAL_STORAGE_DIR = "sunbird";

    public static byte[] getFileFromInternalStorageOrAssets(Context context, String fileName, String assetsFolder) throws IOException {
        byte[] data = null;
        try {
            data = getFileFromInternalStorage(context, fileName);
            if (data == null) {
                data = getFileFromAssets(context, assetsFolder + "/" + fileName);
            }
        } catch (Exception e) {
            Logger.e(TAG, "not found in internal storage.", e);
            data = getFileFromAssets(context, assetsFolder + "/" + fileName);
        }
        return data;
    }

    public static byte[] getFileFromInternalStorageOrAssets(Context context, String fileName) throws IOException {
        return getFileFromInternalStorageOrAssets(context, fileName, "");
    }

    public static byte[] getFileFromInternalStorage(Context context, String fileName) throws IOException {
        File file = new File(context.getDir(INTERNAL_STORAGE_DIR, Context.MODE_PRIVATE), fileName);
        if (file.exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos = readFromInputStream(bos, fileInputStream);
            Logger.d(TAG, fileName + " found in internal storage.");
            return bos.toByteArray();
        } else {
            return null;
        }
    }

    public static File getFileFromInternalStorage(String fileName, Context context) {
        return new File(context.getDir(INTERNAL_STORAGE_DIR, Context.MODE_PRIVATE), fileName);
    }

    public static byte[] getFileFromAssets(Context context, String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open(fileName, Context.MODE_PRIVATE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos = readFromInputStream(bos, inputStream);
        Logger.d(TAG, fileName + " found in assets.");
        return bos.toByteArray();
    }

    private static ByteArrayOutputStream readFromInputStream(ByteArrayOutputStream bos, InputStream is) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = is.read(buffer)) != -1) {
            bos.write(buffer, 0, read);
        }
        bos.close();
        is.close();
        return bos;
    }

    public static String md5(final byte[] bytes) throws NoSuchAlgorithmException {
        final String MD5 = "MD5";
        MessageDigest digest = MessageDigest.getInstance(MD5);
        digest.update(bytes);
        byte messageDigest[] = digest.digest();

        // Create Hex String
        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : messageDigest) {
            String h = Integer.toHexString(0xFF & aMessageDigest);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();
    }

    public static void saveFileToInternalStorage(Context context, String fileName, byte[] data) throws IOException {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(context.getDir(INTERNAL_STORAGE_DIR, Context.MODE_PRIVATE), fileName));
            outputStream.write(data);
        } catch (Exception ex) {
            Logger.e(TAG, "Exception while writing stream", ex);
            Logger.exception(ex);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                Logger.e(TAG, "Exception while closing stream", ex);
            }

        }
    }

    public static byte[] getInternalStorageFileAsByte(String fileName, Context context) {
        ByteArrayOutputStream ous = null;
        try {
            ous = new ByteArrayOutputStream();
            InputStream ios = new FileInputStream(FileUtil.getFileFromInternalStorage(fileName, context));
            ous = readFromInputStream(ous, ios);
            return ous.toByteArray();
        } catch (FileNotFoundException e) {
            Logger.e(TAG, "Could not read " + fileName, e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            Logger.e(TAG, "Could not read " + fileName, e);
            deleteFileFromInternalStorage(fileName, context);
            throw new RuntimeException(e);
        } catch (Exception e) {
            Logger.e(TAG, "Could not read " + fileName, e);
            deleteFileFromInternalStorage(fileName, context);
            throw new RuntimeException(e);
        }
    }

    public static boolean deleteFileFromInternalStorage(String fileName, Context context) {
        File corruptedFile = getFileFromInternalStorage(fileName, context);
        if (corruptedFile.exists()) {
            Logger.e(TAG, "FILE CORRUPTED. DISABLING GODEL");
            return corruptedFile.delete();
        } else {
            Logger.d(TAG, fileName + " not found");
            return false;
        }
    }

}
