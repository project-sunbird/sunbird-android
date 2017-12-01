package org.sunbird.utils;

/**
 * Created by no on 29/11/17.
 */

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class FileDownloader {

    int mDownloadedFileSize = 0;
    int mTotalFileSize = 0;
    String download_file_url ;
    String localStoreFilePath;
    Handler mHandler;
    private static final String TAG=FileDownloader.class.getSimpleName();

    private OnFileDownloadProgressChangedListener mDownloadChangedListener;
    public  boolean mStopDownload=false;


    public FileDownloader(String fileUrl,OnFileDownloadProgressChangedListener listener,String filePath) {
        this.mHandler=new Handler(Looper.getMainLooper());
        this.download_file_url=fileUrl;
        this.localStoreFilePath = filePath;
        this.mDownloadChangedListener=listener;
    }

    /**Start doownloading of the file*/
    public void startDownload(){
        /**start downloading file*/
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDownloadChangedListener.onDownloadStart();
            }
        });
        downloadFile();
    }
    public void stopDownloading(){
        this.mStopDownload=true;
    }

    int downloadFile(){

        try {
            URL url = new URL(download_file_url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();

            String path = localStoreFilePath.substring(0,localStoreFilePath.lastIndexOf("/"));//set the path where we want to save the file
            File directory = new File(path);
            if(!directory.exists()){       //check if directory exists
                directory.mkdirs();        //create directory if it does not exist
            }
            final File file = new File(localStoreFilePath);
            FileOutputStream fileOutput = new FileOutputStream(file);
            mTotalFileSize = urlConnection.getContentLength();
            byte[] buffer = new byte[1024*1024];    //create a buffer
            int bufferLength = 0;

            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                mDownloadedFileSize += bufferLength;
                Log.e("download", "progress :"+mDownloadedFileSize);
                if(mStopDownload){      /*Check whether to cancel this download or not*/
                    break;
                }

                /* Post the current progress on the service*/
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        float per = ((float) mDownloadedFileSize / mTotalFileSize) * 100;
                        mDownloadChangedListener.onProgressChanged(per);
                    }
                });
            }
            fileOutput.close();    //close the output stream when complete //
            if(!mStopDownload){
                /* Notify through the listener that the file is downloaded*/
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadChangedListener.onFileDownloaded(file.getAbsolutePath());
                    }
                });
            }
        }
        catch (Exception e) {
            Log.e(TAG,"Error in Filedownloader.java"+e);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadChangedListener.onFailure();
                }
            });
        }
        return  0;
    }


    /*Interface to track the downloading status */
    public interface OnFileDownloadProgressChangedListener{
        public void onDownloadStart();
        public void onProgressChanged(float currentProgress);
        public void onFileDownloaded(String currentPath);
        public void onFailure();
    }
}
