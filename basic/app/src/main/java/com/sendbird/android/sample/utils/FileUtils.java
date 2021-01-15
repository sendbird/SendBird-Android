package com.sendbird.android.sample.utils;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.Hashtable;

/**
 * DateUtils related to file handling (for sending / downloading file messages).
 */

public class FileUtils {

    // Prevent instantiation
    private FileUtils() {

    }

    public static Hashtable<String, Object> getFileInfo(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            String mime = context.getContentResolver().getType(uri);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                Hashtable<String, Object> value = new Hashtable<>();

                if (cursor.moveToFirst()) {
                    String name = cursor.getString(nameIndex);
                    int size = (int) cursor.getLong(sizeIndex);

                    if (TextUtils.isEmpty(name)) {
                        name = "Temp_" + uri.hashCode() + "." + extractExtension(context, uri);
                    }
                    File file = new File(context.getCacheDir(), name);

                    ParcelFileDescriptor inputPFD = context.getContentResolver().openFileDescriptor(uri, "r");
                    FileDescriptor fd = null;
                    if (inputPFD != null) {
                        fd = inputPFD.getFileDescriptor();
                    }
                    FileInputStream inputStream = new FileInputStream(fd);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    int read;
                    byte[] bytes = new byte[1024];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }

                    value.put("path", file.getAbsolutePath());
                    value.put("size", size);
                    value.put("mime", mime);
                    value.put("name", name);
                }
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(e.getLocalizedMessage(), "File not found.");
            return null;
        }
        return null;
    }

    public static String extractExtension(@NonNull Context context, @NonNull Uri uri) {
        String extension;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            extension = extractExtension(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
    }

    public static String extractExtension(@NonNull String mimeType) {
        final MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(mimeType);
    }

    /**
     * Downloads a file using DownloadManager.
     */
    public static void downloadFile(Context context, String url, String fileName) {
        DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(url));
        downloadRequest.setTitle(fileName);

        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            downloadRequest.allowScanningByMediaScanner();
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(downloadRequest);
    }


    /**
     * Converts byte value to String.
     */
    public static String toReadableFileSize(long size) {
        if (size <= 0) return "0KB";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static void saveToFile(File file, String data) throws IOException {
        File tempFile = File.createTempFile("sendbird", "temp");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(data.getBytes());
        fos.close();

        if(!tempFile.renameTo(file)) {
            throw new IOException("Error to rename file to " + file.getAbsolutePath());
        }
    }

    public static String loadFromFile(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[8192];
        int read;
        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
            builder.append(buffer, 0, read);
        }
        return builder.toString();
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
