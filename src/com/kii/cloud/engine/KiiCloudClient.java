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

package com.kii.cloud.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.kii.cloud.storage.KiiClient;
import com.kii.cloud.storage.KiiFile;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.demo.utils.Utils;

/**
 * The purpose of this class is to enforce at most one instance of KiiSyncClient
 * so that it is easy access throughout the application. It also put together
 * the sync, upload, download, caching together. If you are using Android 2.3
 * and above, it is advisable to use Android DownloadManager The caching is
 * provide a quick mechanism for the UI thread to retrieve/check the progress
 * status of the file.
 */
public class KiiCloudClient {

    public static String TAG = "KiiSyncClient";
    private static KiiCloudClient mInstance = null;

    /**
     * Category of TRASH.
     */
    public static final String CATEGORY_TRASH = "trash";
    /**
     * Category of NONE.
     */
    public static final String CATEGORY_NONE = "none";

    public static final String CATEGORY_BACKUP = "backup";

    private static CloudCallback mCloudCallback;

    /**
     * Change the password of the user.
     * 
     * @param oldPassword
     * @param newPassword
     * @return refer to {@link com.kii.sync.SyncMsg SyncMsg}
     */
    public int changePassword(KiiUserCallBack callback, String newPassword,
            String oldPassword) {
        return KiiClient.getCurrentUser().changePassword(callback, newPassword,
                oldPassword);
    }

    public int register(KiiUserCallBack callback, KiiUser user,
            String username, String password) {
        return user.register(callback, username, password);
    }

    /**
     * Log in using the given username and password. If the given password is
     * different from previous one, it will login again else just return OK.
     * 
     * @param username
     * @param password
     * @return
     */
    public int login(KiiUserCallBack callback, String username, String password) {
        return KiiUser.logIn(callback, username, password);
    }

    /**
     * Clear the local sync database and user preference Non Blocking Call
     * 
     * @return
     */
    public int logout() {
        KiiClient.logOut();
        return 0;
    }

    /**
     * @throws InterruptedException
     * @throws InstantiationException
     */
    private KiiCloudClient() {
        KiiClient.initialize(Constants.appId, Constants.appKey,
                Constants.DEFAULT_BASE_URL);
    }

    /**
     * Get an instance of KiiSyncClient. It is designed to be singleton.
     * 
     * @param context
     *            is the application context
     * @return KiiSyncClient
     */
    public static synchronized KiiCloudClient getInstance(Context context) {
        if (mInstance == null) {
            try {
                mInstance = new KiiCloudClient();
                mCloudCallback = new CloudCallback(context);
                // start the backup service
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mInstance;
    }

    /**
     * Get a list of files are backup Non Blocking Call
     * 
     * @return array of KiiFile
     */
    public KiiFile[] getBackupFiles() {
        return mCloudCallback.getCloudFiles();
    }

    /**
     * Check if the file content has changed, if yes, it will upload the file
     * too
     * 
     * @param files
     *            list of KiiFiles to be uploaded
     * @return SyncMsg
     */
    public int updateBody(List<KiiFile> files) {
        for (KiiFile file : files) {
            File localFile = Utils.getLocalFile(file);
            int token = file.update(mCloudCallback, localFile);
            mCloudCallback.addTokenAction(token, TaskType.FileTask.UPDATE);
            mCloudCallback.addToUploadQueue(token, file);
        }
        return CloudMsg.OK;
    }

    /**
     * Delete the KiiFile and call quick refresh
     * 
     * @param KiiFile
     *            the file to be deleted
     * @param deleteLocal
     *            if true, the local file will be deleted
     * @return
     */
    public int delete(KiiFile file, boolean deleteLocal) {
        int ret = file.delete(mCloudCallback);
        mCloudCallback.addTokenAction(ret, TaskType.FileTask.DELETE);
        if (deleteLocal) {
            File f = new File(file.getLocalPath());
            f.delete();
        } else {
        }
        return ret;
    }

    /**
     * Cancel a file to be uploaded. To remove a uploaded file, refer to
     * {@link KiiCloudClient#delete(KiiFile, boolean)}
     * 
     * @return
     */
    public boolean cancel(KiiFile file) {
        int token = findTokenByFile(file);
        return KiiClient.cancelTask(token);
    }

    private int findTokenByFile(KiiFile file) {
        String localPath = file.getLocalPath();
        Map<Integer, KiiFile> set = mCloudCallback.getUploadQueue();
        if (!TextUtils.isEmpty(localPath)) {
            for (Entry<Integer, KiiFile> entry : set.entrySet()) {
                KiiFile f = entry.getValue();
                if (!TextUtils.isEmpty(f.getLocalPath())
                        && (localPath.contentEquals(f.getLocalPath()))) {
                    return entry.getKey();
                }
            }
        }
        String uri = file.toUri().toString();
        set = mCloudCallback.getUpdateQueue();
        if (!TextUtils.isEmpty(uri)) {
            for (Entry<Integer, KiiFile> entry : set.entrySet()) {
                KiiFile f = entry.getValue();
                if (!TextUtils.isEmpty(f.toUri().toString())
                        && (uri.contentEquals(f.toUri().toString()))) {
                    return entry.getKey();
                }
            }
        }
        set = mCloudCallback.getDownQueue();
        if (!TextUtils.isEmpty(uri)) {
            for (Entry<Integer, KiiFile> entry : set.entrySet()) {
                KiiFile f = entry.getValue();
                if (!TextUtils.isEmpty(f.toUri().toString())
                        && (uri.contentEquals(f.toUri().toString()))) {
                    return entry.getKey();
                }
            }
        }
        return -1;
    }

    /**
     * Get a list of trashed files. Non Blocking Call
     * 
     * @return
     */
    public KiiFile[] getTrashFiles() {
        return mCloudCallback.getTrashFiles();
    }

    public int upload(File file) {
        KiiFile f = new KiiFile(file, Constants.ANDROID_EXT);
        f.setRemotePath(getRemotePath(file.getAbsolutePath()));
        int ret = f.upload(mCloudCallback);
        mCloudCallback.addToUploadQueue(ret, f);
        mCloudCallback.addTokenAction(ret, TaskType.FileTask.UPLOAD);
        android.util.Log.d(TAG, "upload: " + file.getAbsolutePath()
                + " result is " + ret);
        return ret;
    }

    /**
     * Move the KiiFile to trash.
     * 
     * @param kiFile
     * @return SyncMsg
     */
    public int moveKiiFileToTrash(KiiFile file) {
        int ret = file.moveToTrash(mCloudCallback);
        mCloudCallback.addTokenAction(ret, TaskType.FileTask.MOVE_TRASH);
        return ret;
    }

    /**
     * Restore the file from Trash
     * 
     * @param file
     * @return
     */
    public int restoreFromTrash(KiiFile file) {
        int ret = file.restoreFromTrash(mCloudCallback);
        mCloudCallback.addTokenAction(ret, TaskType.FileTask.RESTORE_TRASH);
        return ret;
    }

    public int download(KiiFile file, String dest) {
        int ret;
        if (!TextUtils.isEmpty(file.getRemotePath())) {
            ret = file.downloadFileBody(
                    mCloudCallback,
                    Environment.getExternalStorageDirectory() + "/"
                            + file.getRemotePath());
        } else {
            ret = file.downloadFileBody(mCloudCallback, dest);
        }
        mCloudCallback.addToDownQueue(ret, file);
        mCloudCallback.addTokenAction(ret, TaskType.FileTask.DOWNLOAD);
        return ret;
    }

    public int getOverallProgress() {
        // TODO
        return 0;
    }

    public KiiFile[] getDownloadList() {
        Map<Integer, KiiFile> set = mCloudCallback.getDownQueue();
        List<KiiFile> filelist = new ArrayList<KiiFile>();
        for (Entry<Integer, KiiFile> entry : set.entrySet()) {
            filelist.add(entry.getValue());
        }
        return filelist.toArray(new KiiFile[] {});
    }

    /**
     * Get the list of files that are in upload progress Non Blocking Call
     * 
     * @return
     */
    public KiiFile[] getUploadList() {
        Map<Integer, KiiFile> set = mCloudCallback.getUpdateQueue();
        List<KiiFile> filelist = new ArrayList<KiiFile>();
        for (Entry<Integer, KiiFile> entry : set.entrySet()) {
            filelist.add(entry.getValue());
        }
        set = mCloudCallback.getUploadQueue();
        for (Entry<Integer, KiiFile> entry : set.entrySet()) {
            filelist.add(entry.getValue());
        }
        return filelist.toArray(new KiiFile[] {});
    }

    // retrieve the remote file information
    public void refresh() {
        int token = KiiFile.listWorkingFiles(mCloudCallback,
                Constants.ANDROID_EXT);
        mCloudCallback.addTokenAction(token, ActionType.ACTION_LIST_FILES);
        token = KiiFile.listTrashedFiles(mCloudCallback, Constants.ANDROID_EXT);
        mCloudCallback.addTokenAction(token, ActionType.ACTION_LIST_TRASH);
    }

    public KiiUser getloginUser() {
        return KiiClient.getCurrentUser();
    }

    private static String getRemotePath(String path) {
        File sdroot = Environment.getExternalStorageDirectory();
        String sdpath = sdroot.getAbsolutePath();
        if (path.startsWith("/sdcard")) {
            path = path.replaceFirst("/sdcard", "/mnt/sdcard");
        }
        if (path.startsWith(sdpath)) {
            return path.substring(sdpath.length() + 1);
        } else {
            return "";
        }
    }
}