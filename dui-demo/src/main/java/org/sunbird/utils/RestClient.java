package org.sunbird.utils;

import android.util.Log;

import org.sunbird.models.ApiResponse;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by parthvora on 05/05/17.
 */

public class RestClient {
    private static final String LOG_TAG = "RestClient";
    private static final int CONNECTION_TIMEOUT = 30 * 1000;
    private static final int READ_TIMEOUT = 60 * 1000;

    private static ApiResponse createErrorResponse(Exception e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatusCode(-1);
        apiResponse.setData(e.getLocalizedMessage().getBytes());
        return apiResponse;
    }

    private static HttpURLConnection getConnection(String locationUrl, Map<String, String> headers, String method) throws Exception {
        HttpURLConnection urlConnection = null;
        URL url = new URL(locationUrl);
        if (url.getProtocol().equals("https")) {
            urlConnection = (HttpsURLConnection) url.openConnection();
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }

        urlConnection.setRequestMethod(method);
        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        urlConnection.setReadTimeout(READ_TIMEOUT);

        if (method.equals("POST"))
            urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        // Set the headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        urlConnection.setRequestProperty("Accept-Encoding", "gzip");
        return urlConnection;
    }

    private static ApiResponse responseHandler(HttpURLConnection urlConnection) throws Exception {
        Map<String, List<String>> headers = urlConnection.getHeaderFields();

        // Read the InputStream - that's the response from server
        int responseCode = urlConnection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            ByteArrayOutputStream bos = readFromInputStream(urlConnection.getInputStream());
            if (bos != null) {
                byte[] resp = bos.toByteArray();
                if (isResponseGZipped(headers)) {
                    resp = gUnZipContent(resp);
                }
                return new ApiResponse(responseCode, resp);
            }
        }
        return new ApiResponse(responseCode, null);
    }

    public static ApiResponse get(String url, Map<String, String> queryParameters, Map<String, String> headers, boolean shouldLog) {
        try {
            return get(url + mapToQueryString(queryParameters), headers, shouldLog);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    public static ApiResponse get(String locationUrl, Map<String, String> headers, boolean shouldLog) {
        HttpURLConnection urlConnection = null;
        try {
            Log.d("test!", headers.toString());
            urlConnection = getConnection(locationUrl, headers, "GET");

            ApiResponse apiResponse = responseHandler(urlConnection);
            return apiResponse;
        } catch (Exception e) {
            return createErrorResponse(e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    public static ApiResponse post(String locationUrl, String body, boolean shouldLog) {
        return post(locationUrl, body, null, shouldLog);
    }

    public static ApiResponse post(String locationUrl, String body, Map<String, String> headers, boolean shouldLog) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = getConnection(locationUrl, headers, "POST");
            BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(body.getBytes());
            out.flush();
            out.close();
            ApiResponse apiResponse = responseHandler(urlConnection);
            return apiResponse;
        } catch (Exception e) {
            return createErrorResponse(e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    public static ApiResponse postZip(String locationUrl, String body, boolean shouldLog) {
        HttpURLConnection urlConnection = null;
        try {
            Map<String, String> header = new HashMap<>();
            header.put("Content-Encoding", "gzip");
            header.put("Content-Type", "application/json");
            urlConnection = getConnection(locationUrl, header, "POST");
            BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(gzipContent(body.getBytes("UTF-8")));
            out.flush();
            out.close();
            return responseHandler(urlConnection);
        } catch (Exception e) {
            return createErrorResponse(e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    public static ApiResponse fetchIfModified(String locationUrl, Map<String, String> headers) throws IOException {
        return get(locationUrl, headers, false);
    }

    private static ByteArrayOutputStream readFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = is.read(buffer)) != -1) {
            bos.write(buffer, 0, read);
        }
        bos.close();
        is.close();
        return bos;
    }

    private static byte[] gUnZipContent(byte[] gzippedData) throws Exception {
        GZIPInputStream zis = null;
        ByteArrayInputStream byteStream = new ByteArrayInputStream(gzippedData);
        zis = new GZIPInputStream(byteStream);
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int numRead = 0;
        while ((numRead = zis.read(bytes)) >= 0) {
            out.write(bytes, 0, numRead);
        }
        if (zis != null) {
            try {
                zis.close();
            } catch (Exception e) {
            }
        }
        return out.toByteArray();
    }

    private static byte[] gzipContent(byte[] uncompressed) throws Exception {
        ByteArrayOutputStream os;
        os = new ByteArrayOutputStream(uncompressed.length);
        GZIPOutputStream gzos = new GZIPOutputStream(os);
        gzos.write(uncompressed);
        os.close();
        gzos.close();
        return os.toByteArray();
    }

    private static boolean isResponseGZipped(Map<String, List<String>> headers) {
        List<String> ceValues = headers.get("Content-Encoding");
        return (ceValues != null && ceValues.contains("gzip"));
    }

    private static String mapToQueryString(Map<String, String> queryParameters) throws UnsupportedEncodingException {
        StringBuilder string = new StringBuilder();

        if (queryParameters.size() > 0) {
            string.append("?");
        }

        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            string.append(entry.getKey());
            string.append("=");
            if (entry.getValue() == null) {
                string.append("");
            } else {
                string.append(URLEncoder.encode(entry.getValue(), "utf-8"));
            }
            string.append("&");
        }

        return string.toString();
    }
}
