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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.kii.cloud.storage.KiiFile;
import com.kii.cloud.storage.callback.KiiFileCallBack;
import com.kii.demo.utils.Utils;

public class CloudCallback extends KiiFileCallBack {
    private Context mContext;

    // download
    private Map<Integer, KiiFile> mDownQueue = new HashMap<Integer, KiiFile>();
    // local file upload, use filepath as key
    private Map<Integer, KiiFile> mUploadQueue = new HashMap<Integer, KiiFile>();
    // file body updated, use KiiFile#toUri().toString() as key
    private Map<Integer, KiiFile> mUpdateQueue = new HashMap<Integer, KiiFile>();
    // trash files
    private List<KiiFile> mTrashFiles = new ArrayList<KiiFile>();
    // cloud files
    private List<KiiFile> mCloudFiles = new ArrayList<KiiFile>();
    // token map
    private Map<Integer, Integer> mTokenMap = new HashMap<Integer, Integer>();

    public CloudCallback(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onUploadCompleted(int token, boolean success, KiiFile file,
            Exception exception) {
        showTaskCompleteToast(token, success);
        if (mUploadQueue.containsKey(token)) {
            mUploadQueue.remove(token);
        }
        final KiiFile f = file;
        if (!success) {
            // delete the uploaded kiifile
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        f.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onUpdateCompleted(int token, boolean success, KiiFile file,
            Exception exception) {
        showTaskCompleteToast(token, success);
        if (mUpdateQueue.containsKey(token)) {
            mUpdateQueue.remove(token);
        }
    }

    @Override
    public void onDownloadBodyCompleted(int token, boolean success,
            Exception exception) {
        showTaskCompleteToast(token, success);
        if (mDownQueue.containsKey(token)) {
            mDownQueue.remove(token);
        }
    }

    private void showTaskCompleteToast(int token, boolean success) {
        if (mTokenMap.containsKey(token)) {
            int action = mTokenMap.get(token);
            Toast.makeText(mContext,
                    Utils.getUserActionString(action, success),
                    Toast.LENGTH_SHORT).show();
            mTokenMap.remove(token);
        }
    }

    @Override
    public void onRefreshCompleted(int token, boolean success, KiiFile file,
            Exception exception) {
        showTaskCompleteToast(token, success);
    }

    @Override
    public void onEmptyTrashCompleted(int token, boolean success,
            Exception exception) {
        showTaskCompleteToast(token, success);
    }

    @Override
    public void onDeleteCompleted(int token, boolean success,
            Exception exception) {
        showTaskCompleteToast(token, success);
    }

    @Override
    public void onMoveTrashCompleted(int token, boolean success, KiiFile file,
            Exception exception) {
        showTaskCompleteToast(token, success);
    }

    @Override
    public void onRestoreTrashCompleted(int token, boolean success,
            KiiFile file, Exception exception) {
        showTaskCompleteToast(token, success);
    }

    @Override
    public void onListWorkingCompleted(int token, boolean success,
            List<KiiFile> files, Exception exception) {
        showTaskCompleteToast(token, success);
        if (success) {
            mCloudFiles.clear();
            mCloudFiles.addAll(files);
        }
        mContext.sendBroadcast(new Intent(Constants.UI_REFRESH_INTENT));
    }

    @Override
    public void onListTrashCompleted(int token, boolean success,
            List<KiiFile> files, Exception exception) {
        showTaskCompleteToast(token, success);
        if (success) {
            mTrashFiles.clear();
            mTrashFiles.addAll(files);
        }
        mContext.sendBroadcast(new Intent(Constants.UI_REFRESH_INTENT));
    }

    @Override
    public void onTaskCancel(int token) {
        Log.d(TAG, "onTaskCancel: token is " + token);
        if (mTokenMap.containsKey(token)) {
            Toast.makeText(mContext,
                    Utils.getResultString(mTokenMap.get(token), false),
                    Toast.LENGTH_SHORT).show();
            mTokenMap.remove(token);
        }
        // TODO: cancel notification
    }

    @Override
    public void onTaskStart(int token) {
        // TODO: show notification;
    }

    void addToDownQueue(int token, KiiFile file) {
        mDownQueue.put(token, file);
    }

    Map<Integer, KiiFile> getDownQueue() {
        return mDownQueue;
    }

    void addToUploadQueue(int token, KiiFile f) {
        mUploadQueue.put(token, f);
    }

    void addToUpdateQueue(int token, KiiFile file) {
        mUpdateQueue.put(token, file);
    }

    Map<Integer, KiiFile> getUploadQueue() {
        return mUploadQueue;
    }

    Map<Integer, KiiFile> getUpdateQueue() {
        return mUpdateQueue;
    }

    public static final String TAG = "CloudCallback";

    void addTokenAction(int token, int action) {
        mTokenMap.put(token, action);
    }

    KiiFile[] getCloudFiles() {
        return mCloudFiles.toArray(new KiiFile[] {});
    }

    KiiFile[] getTrashFiles() {
        return mTrashFiles.toArray(new KiiFile[] {});
    }

}
