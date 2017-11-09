package org.sunbird.utils;


import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.sunbird.models.ApiResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by parth on 17/9/16.
 */
public class RemoteAssetService {

    private static final String TAG = "RemoteAssetService";

    public static Boolean downloadAndSaveFile(Context context, String url, boolean isItSignedJsa) throws IOException, NoSuchAlgorithmException {
        byte[] newFile = null;
        int index = url.lastIndexOf("/");
        String fileName = url.substring(index + 1);
        newFile = downloadFileIfNotModified(context, url, isItSignedJsa);
        if (newFile != null) {
            if (isItSignedJsa) {
                fileName = fileName.replace(".zip", ".jsa");
            }
            FileUtil.saveFileToInternalStorage(context, fileName, newFile);
        } else {
            return false;
        }
        return true;
    }

    /**
     * @param context Context is needed to get the md5 of available file. so that we download only if it is changed
     * @param url     URL from where file has to be downloaded
     * @return Byte Array if file has been changed. It can return null in case of file has not changed or in case of zip files, it looks for signed and jsa files.
     * It verifies the signature and returns Byte Array of jsa file. In case if signature doesnt match, it returns null.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static byte[] downloadFileIfNotModified(Context context, String url, boolean isItSignedJsa) throws IOException, NoSuchAlgorithmException {
        int index = url.lastIndexOf("/");
        String fileName = url.substring(index + 1);
        byte[] currentConfig = null;
        String currentHash = null;
        if (isItSignedJsa && url.endsWith(".zip")) {
            currentHash = context.getSharedPreferences("UPI", Context.MODE_PRIVATE).getString(fileName + "_hash", null);
        } else {
            currentConfig = FileUtil.getFileFromInternalStorageOrAssets(context, fileName);
            if (currentConfig != null) {
                currentHash = FileUtil.md5(currentConfig);
            }
        }

        // Appending timeStamp so that ISP doesn't cache and appending If-None_match header
        HashMap queryParam = new HashMap<String, String>();
        queryParam.put("ts", String.valueOf(System.currentTimeMillis()));
        queryParam.put("If-None-Match", currentHash);

        ApiResponse apiResponse = RestClient.fetchIfModified(url, queryParam);

        //Handling of zip file starts here
        if (apiResponse != null && apiResponse.getStatusCode() == 200 && url.endsWith(".zip") && isItSignedJsa) {
            byte[] response = apiResponse.getData();
            String responseHash = FileUtil.md5(response);
            //Open Stream to unzip downloaded bytes
            InputStream downloadedStream = new ByteArrayInputStream(response);
            ZipInputStream zin = new ZipInputStream(downloadedStream);
            ZipEntry distinctFiles = null;

            try {
                byte[] fileToBeVerified = null;
                byte[] signature = null;
                while ((distinctFiles = zin.getNextEntry()) != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        buffer.write(c);
                    }
                    zin.closeEntry();
                    buffer.close();
                    if (distinctFiles.getName().endsWith(".signature")) {
                        signature = Base64.decode(buffer.toByteArray(), Base64.NO_WRAP);
                    } else if (distinctFiles.getName().endsWith(".jsa")) {
                        fileToBeVerified = buffer.toByteArray();
                    }
                }
                if (fileToBeVerified == null && signature == null) {
                    // If this two files are not found, then it means it doesn't contain jsa and signature file. Can be a corrupted file.
                    // So lets return null.
                    return null;
                }

                ObjectInputStream keyIn = null;
                try {
                    //Access Public Key
                    keyIn = new ObjectInputStream(new ByteArrayInputStream(FileUtil.getFileFromAssets(context, "remoteAssetPublicKey")));
                    PublicKey pubkey = (PublicKey) keyIn.readObject();

                    //Verify Algorithm
                    Signature verifyalg = Signature.getInstance("DSA");
                    verifyalg.initVerify(pubkey);

                    //Verify Signature
                    verifyalg.update(fileToBeVerified);
                    if (!verifyalg.verify(signature)) {
                        response = null;
                    } else {
                        response = fileToBeVerified;
                        context.getSharedPreferences("UPI", Context.MODE_PRIVATE).edit().putString(fileName + "_hash", responseHash).commit();
                        return response;
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Exception while checking digital signature of asset", e);
                } finally {
                    if (keyIn != null) keyIn.close();
                }
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "Got Out of Memory Error", e);
                response = null;
            } catch (Exception e) {
                Log.e(TAG, "Exception while checking digital signature of asset", e);
            }
        }
        //Completes Handling ZIP files
        if (apiResponse.getStatusCode() == 304 || apiResponse.getStatusCode() == -1) {
            return null;
        } else {
            return apiResponse.getData();
        }
    }
}