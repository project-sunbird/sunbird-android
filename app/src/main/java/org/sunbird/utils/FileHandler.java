package org.sunbird.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import org.ekstep.genieservices.commons.utils.StringUtil;
import org.sunbird.GlobalApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created on 4/29/2016.
 *
 * @author swayangjit_gwl
 */
public class FileHandler {

    private static final String TAG = FileHandler.class.getSimpleName();

    public static String getAttachmentFilePath(Uri uri) {
        Context context = GlobalApplication.getInstance();

        InputStream is = null;
        FileOutputStream os = null;
        String fullPath = null;
        String name = null;

        try {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
            cursor.moveToFirst();
            int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            if (nameIndex >= 0) {
                name = cursor.getString(nameIndex);
            }
            fullPath = Environment.getExternalStorageDirectory() + "/" + name;
            is = context.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(fullPath);

            byte[] buffer = new byte[4096];
            int count;
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
            os.close();
            is.close();
        } catch (Exception e) {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (fullPath != null) {
                File f = new File(fullPath);
                f.delete();
            }
        }

        return fullPath;
    }

    public static String createFileFromAsset(Context context, String fileName) {
        try {
            AssetManager am = context.getAssets();
            InputStream inputStream = am.open(fileName);
            String tempAssetFilePath = FileHandler.getTempLocation(getExternalFilesDir(context), System.currentTimeMillis() + ".ecar").toString();
            OutputStream outputStream = new FileOutputStream(tempAssetFilePath);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return tempAssetFilePath;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void createFolders(String loc, String dir) {
        File f = new File(loc, dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    public static File getTmpDir(File externalFilesDir) {
        return new File(externalFilesDir.getPath() + "/tmp");
    }


    @NonNull
    public static File getExternalFilesDir(Context context) {
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null)
            throw new RuntimeException("External file could not be loaded.");
        return externalFilesDir;
    }

    public static String getExportGenieAPKFilePath(File externalFilesDir) {
        File file = getTempLocation(externalFilesDir, "Genie.apk");
        if (file.exists()) {
            file.delete();
        }
        return file.getAbsolutePath();
    }

    public static File getTempLocation(File externalFilesDir, String fileName) {
        createFolders(getTmpDirFilePath(externalFilesDir), "");
        return new File(getTmpDir(externalFilesDir), fileName);
    }

    public static String getTmpDirFilePath(File externalFilesDir) {
        File tmpLocation = getTmpDir(externalFilesDir);
        return tmpLocation.getAbsolutePath();
    }

    public static String getProfileDirFilePath(File externalFilesDir, String directoryName) {
        File tmpLocation = getRequiredDirectory(externalFilesDir, directoryName);
        return tmpLocation.getAbsolutePath();
    }

    public static File getRequiredDirectory(File externalFilesDir, String directoryName) {
        File directory = new File(externalFilesDir.getPath() + "/" + directoryName);

        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        return directory;
    }

    public static void removeLastLineFromFile(final String filePath) throws IOException {
        final List<String> lines = new LinkedList<>();
        final Scanner reader = new Scanner(new FileInputStream(filePath), "UTF-8");
        while (reader.hasNextLine()) {
            lines.add(reader.nextLine().concat(System.getProperty("line.separator")));
        }
        reader.close();
        lines.remove(lines.size() - 1);
        final BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false));
        for (final String line : lines)
            writer.write(line);
        writer.flush();
        writer.close();
    }

    public static boolean checkIfFileExists(String filePath) {
        File fileToCheck = new File(filePath);
        if (fileToCheck.exists()) {
            return true;
        }

        return false;
    }

    public static void createFileInTheDirectory(String filePath) {
        try {
            File f = new File(filePath);
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();

            FileOutputStream out = new FileOutputStream(f);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete all files in the tmp directory that are older than 1 days.
     *
     * @param file
     */
    public static void deleteTmpFiles(File file) {
        if (file != null && file.exists()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteTmpFiles(f);
                }
            }

            //find the diff for file last modified date&time
            long diff = System.currentTimeMillis() - file.lastModified();
            if (diff > TimeUnit.DAYS.toMillis(1)) {
                file.delete();
            }
        }
    }

    /**
     * Save the data to the file
     *
     * @param filePath
     * @param data
     */
    public static void saveToFile(String filePath, String data) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
            fileOutputStream.write((data.concat(System.getProperty("line.separator"))).getBytes());
        } catch (FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    /**
     * Read the data from the given file
     *
     * @param filePath
     * @return
     */
    public static String readFile(String filePath) {
        String line = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(filePath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + ",");
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        }

        if (!StringUtil.isNullOrEmpty(line)) {
            line = line.substring(0, line.length() - 1);
            return line;
        } else {
            return line;
        }
    }

    /**
     * Read last line from a given file
     *
     * @param filePath
     * @return
     */
    public static String readLastLineFromFile(String filePath) {
        String currentLine = null;
        String lastLine = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(filePath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while ((currentLine = bufferedReader.readLine()) != null) {
                lastLine = currentLine;
            }
            fileInputStream.close();

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
        return lastLine;
    }


    private static long getFreeUsableSpace(Context context) {
        File externalFilesDir = getExternalFilesDir(context);
        if (externalFilesDir != null) {
            return externalFilesDir.getUsableSpace();
        }
        return 0;
    }

    public static boolean isDeviceMemoryAvailable(Context context, long fileSize) {

        long deviceUsableSpace = getFreeUsableSpace(context);
        long BUFFER_SIZE = calculateBufferSize(fileSize);
        if (deviceUsableSpace < ((fileSize) + BUFFER_SIZE)) {
            return false;
        }

        return true;
    }

    private static long calculateBufferSize(long fileSize) {
        long bufferSize = 0;
        if (fileSize > 0) {
            bufferSize = fileSize / 5;
        }
        return bufferSize;
    }

//    public static boolean isSecondaryStorageAvailable() {
//        boolean isSecondaryStorage = false;
//        String secondaryStorage = MountPointUtils.getExternalSecondaryStorage();
//        if (!StringUtil.isNullOrEmpty(secondaryStorage)) {
//            File path = new File(secondaryStorage);
//            if (path != null && path.exists()) {
//                isSecondaryStorage = true;
//            }
//        }
//        return isSecondaryStorage;
//    }
//
//    public static String getSecondaryStorageFilePath() {
//        boolean isSecondaryStorage = false;
//        String secondaryStorage = MountPointUtils.getExternalSecondaryStorage();
//        if (!StringUtil.isNullOrEmpty(secondaryStorage)) {
//            File path = new File(secondaryStorage);
//            if (path != null && path.exists()) {
//                return secondaryStorage;
//            }
//        }
//        if (!isSecondaryStorage) {
//            return getSystemRootFilePath();
//        }
//        return null;
//    }

    private static String getSystemRootFilePath() {
        File rootFile = Environment.getRootDirectory();
        File parentFile = rootFile.getParentFile();
        return parentFile.getAbsolutePath();
    }

//    public static long getAvailableExternalMemorySize() {
//        if (isSecondaryStorageAvailable()) {
//            StatFs stat = new StatFs(getSecondaryStorageFilePath());
//            long blockSize;
//            long availableBlocks;
//            if (Build.VERSION.SDK_INT >= 18) {
//                blockSize = stat.getBlockSizeLong();
//                availableBlocks = stat.getAvailableBlocksLong();
//            } else {
//                blockSize = (long) stat.getBlockSize();
//                availableBlocks = (long) stat.getAvailableBlocks();
//            }
//
//            return availableBlocks * blockSize;
//        } else {
//            return 0L;
//        }
//    }
//
//    public static long getTotalExternalMemorySize() {
//        if (isSecondaryStorageAvailable()) {
//            StatFs stat = new StatFs(getSecondaryStorageFilePath());
//            long blockSize;
//            long totalBlocks;
//            if (Build.VERSION.SDK_INT >= 18) {
//                blockSize = stat.getBlockSizeLong();
//                totalBlocks = stat.getBlockCountLong();
//            } else {
//                blockSize = (long) stat.getBlockSize();
//                totalBlocks = (long) stat.getBlockCount();
//            }
//
//            return totalBlocks * blockSize;
//        } else {
//            return 0L;
//        }
//    }

    public static long folderSize(File directory) {
        long length = 0;
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        }

        return length;
    }

//    public static String getExternalSdcardPath(Context context) {
//        String sdCardPath = MountPointUtils.getExternalSecondaryStorage();
//        File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
//
//        if (StringUtil.isNullOrEmpty(sdCardPath)) {
//            Util.showCustomToast(R.string.msg_no_sdcard_found);
//        } else {
//            for (File d : dirs) {
//                String path = d.getPath();
//                if (path.contains(sdCardPath))
//                    return path;
//            }
//        }
//        return null;
//    }

//    public static boolean isSelectedStorageAvailable(Context context) {
//        String filepath = FileHandler.getDefaultStoragePath(context);
//        if (!StringUtil.isNullOrEmpty(filepath)) {
//            File file = new File(filepath);
//            return file.exists();
//        }
//        return false;
//    }
//
//    public static String getDefaultStoragePath(Context context) {
//        String filepath;
//        boolean isExternalDefaultStorage = PreferenceUtil.getPreferenceWrapper().getBoolean(PreferenceKey.KEY_SET_EXTERNAL_STORAGE_DEFAULT, false);
//        if (isExternalDefaultStorage) {
//            filepath = getExternalSdcardPath(context);
//        } else {
//            filepath = FileHandler.getExternalFilesDir(context).toString();
//        }
//        return filepath;
//    }
}
