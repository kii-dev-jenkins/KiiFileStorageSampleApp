//
//
//  Copyright 2012 Kii Corporation
//  http://kii.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//

package com.kii.demo.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.Log;

import com.kii.cloud.engine.ActionType;
import com.kii.cloud.engine.TaskType;
import com.kii.cloud.storage.KiiFile;
import com.kii.cloud.storage.utils.FileUtils;
import com.kii.cloud.storage.utils.ImageUtils;

public class Utils {

    static final String TAG = "Utils";

    /**
     * Convert the status code to human reading language
     * 
     * @param kFile
     * @param context
     * @return String
     */
    static public String getStatus(KiiFile kFile, Context context) {
        // TODO: return some valid status
        return "";
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable t) {

            }
        }
    }

    /**
     * Generate the thumbnail with the given unique key and store in the temp
     * folder
     * 
     * @param pathImage
     * @param destPath
     * @param mimeType
     * @return
     */
    public static String generateThumbnail(Context context, String pathImage,
            String destPath, String mimeType) {
        try {
            File dest = new File(destPath);
            if (!dest.getParentFile().exists()) {
                if (dest.getParentFile().mkdirs() == false) {
                    Log.e(TAG, "Create folder failed:" + dest.getAbsolutePath());
                    return null;
                }
            }
            Bitmap b = null;

            if (mimeType.startsWith("image")) {
                b = ImageUtils.createImageThumbnail2(context, pathImage,
                        Images.Thumbnails.MINI_KIND);
            } else if (mimeType.startsWith("video")) {
                b = ThumbnailUtils.createVideoThumbnail(pathImage,
                        Video.Thumbnails.MICRO_KIND);
            }

            if (b != null) {
                OutputStream fos = null;
                try {
                    fos = new FileOutputStream(dest);
                    b.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                    return dest.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        FileUtils.closeSilently(fos);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "generateThumbnailForImage Exception:" + ex.getMessage());
            return null;
        }
        return null;
    }

    public static void startSync(Context context, String command) {
        // do nothing currently
        /*
         * Intent service = new Intent(context.getApplicationContext(),
         * BackupService.class); if (!TextUtils.isEmpty(command)) {
         * service.setAction(command); } context.startService(service);
         */
    }

    public static Drawable getThumbnailDrawableByFilename(String filename,
            Context context) {
        Drawable ret = null;
        Cursor c = Images.Media.query(context.getContentResolver(),
                Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { BaseColumns._ID }, MediaColumns.DATA + "=?",
                new String[] { filename }, null);
        try {
            if ((c != null) && c.moveToFirst()) {
                long id = c.getLong(0);
                Bitmap b = Images.Thumbnails.getThumbnail(
                        context.getContentResolver(), id,
                        Images.Thumbnails.MICRO_KIND,
                        new BitmapFactory.Options());
                if (b != null) {
                    ret = new BitmapDrawable(b);
                }
            } else {
                // create the thumbnail by myself
                MimeInfo mime = MimeUtil.getInfoByFileName(filename);
                if (mime != null && mime.isType("image")) {
                    Bitmap b = ImageUtils.createImageThumbnail2(context,
                            filename, Images.Thumbnails.MICRO_KIND);
                    ret = new BitmapDrawable(b);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return ret;
    }

    public static boolean isKiiFileInTrash(KiiFile file) {
        if (file == null) {
            return false;
        }
        return file.isInTrash();
    }

    /**
     * check if the local file is the same as given KiiFile
     * 
     * @param file
     * @return true if the same otherwise false
     */
    public static boolean bodySameAsLocal(KiiFile file) {
        String localPath = file.getLocalPath();
        if (TextUtils.isEmpty(localPath)) {
            return false;
        }
        File localFile = new File(localPath);
        if (!(localFile.exists() && localFile.isFile())) {
            return false;
        }
        long fileUpdated = localFile.lastModified();
        long lastUpdated = file.getModifedTime();
        if (lastUpdated == -1) {
            return false;
        }
        if (fileUpdated != lastUpdated) {
            return true;
        }
        long size = localFile.length();
        if (size != file.getFileSize()) {
            return true;
        }
        return false;
    }

    /*
     * get the destination download path of a KiiFile object
     */
    public static String getKiiFileDownloadPath(KiiFile file) {
        String title = file.getTitle();
        String downloadFolder =Environment.getExternalStorageDirectory().getAbsolutePath() ;
        String dest = downloadFolder + "/" + title;
        File f = new File(dest);
        if (f.exists()) {
            int sufpos = title.lastIndexOf(".");
            String time = String.valueOf(System.currentTimeMillis());
            if (sufpos < 0) {
                title = title + "-" + time;
            } else {
                title = title.substring(0, sufpos) + "-" + time
                        + title.substring(sufpos);
            }
            dest = downloadFolder + "/" + title;
        }
        return dest;
    }

    public static Drawable getThumbnailByResize(byte[] thumbnail) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(thumbnail, 0,
                    thumbnail.length);
            if (bitmap.getHeight() > 120) {
                // resize the bitmap if too big, save memory
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        (bitmap.getWidth() * 120) / bitmap.getHeight(), 120,
                        false);
            }
            return new BitmapDrawable(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(bitmap!=null) {
                bitmap.recycle();
            }
        }
        return null;
    }

    public static String getResultString(int result, boolean success) {
        StringBuilder sb = new StringBuilder();
        // set action string
        sb.append(getFileActionString(result));
        sb.append(success ? " success." : " failed.");
        return sb.toString();
    }

    private static String getFileActionString(int type) {
        switch (type) {
            case TaskType.FileTask.UPLOAD:
                return "Upload";
            case TaskType.FileTask.DELETE:
                return "Delete";
            case TaskType.FileTask.DOWNLOAD:
                return "Download";
            case TaskType.FileTask.REFRESH:
                return "Get info";
            case TaskType.FileTask.LIST_CLOUD:
            case TaskType.FileTask.LIST_TRASH:
                return "Query";
            case TaskType.FileTask.RESTORE_TRASH:
                return "Restore";
            case TaskType.FileTask.MOVE_TRASH:
                return "Trash";
            case TaskType.FileTask.UPDATE:
                return "Update";
            default:
                return "";
        }
    }

    public static CharSequence getUserActionString(int result,
            boolean success) {
        StringBuilder sb = new StringBuilder();
        sb.append(getUserActionString(result));
        sb.append(": ");
        sb.append(success ? "success." : "failed.");
        return sb.toString();
    }

    private static String getUserActionString(int type) {
        switch(type) {
            case ActionType.ACTION_DOWNLOAD:
                return "Download";
            case ActionType.ACTION_LIST_FILES:
            case ActionType.ACTION_LIST_TRASH:
                return "Refresh";
            case ActionType.ACTION_RESTORE:
                return "Restore";
            case ActionType.ACTION_TRASH:
                return "trash";
            case ActionType.ACTION_UPDATE:
                return "Update";
            case ActionType.ACTION_UPLOAD:
                return "Upload";
            case ActionType.ACTION_EMPTY_TRASH:
            	return "Empty trash";
            case ActionType.ACTION_DELETE:
            	return "Delete";
            default:
                break;
        }
        return "";
    }
    
    public static File getLocalFile(KiiFile file) {
        String remotePath = file.getRemotePath();
        String localPath = file.getLocalPath();
        File localFile = null;
        if (!TextUtils.isEmpty(remotePath)) {
            localFile = new File(Environment.getExternalStorageDirectory()
                    + "/" + remotePath);

        } else if (!TextUtils.isEmpty(localPath)) {
            localFile = new File(localPath);
        }
        if ((localFile != null) && localFile.exists()) {
            return localFile;
        }
        return null;
    }
}
