//
//
// Copyright 2012 Kii Corporation
// http://kii.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//

package com.kii.cloud.engine;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;

import com.kii.cloud.storage.KiiFile;
import com.kii.cloud.storage.callback.KiiFileCallBack;
import com.kii.demo.utils.Utils;

public class CloudCallback extends KiiFileCallBack {
    private Context mContext;

    // download
    private SparseArray<KiiFile> mDownQueue = new SparseArray<KiiFile>();
    // local file upload, use filepath as key
    private SparseArray<KiiFile> mUploadQueue = new SparseArray<KiiFile>();
    // file body updated, use KiiFile#toUri().toString() as key
    private SparseArray<KiiFile> mUpdateQueue = new SparseArray<KiiFile>();
    // trash files
    private List<KiiFile> mTrashFiles = new ArrayList<KiiFile>();
    // cloud files
    private List<KiiFile> mCloudFiles = new ArrayList<KiiFile>();
    // token map
    private SparseIntArray mTokenMap = new SparseIntArray();

    public CloudCallback(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onUploadCompleted(int token, boolean success, KiiFile file,
            Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_UPLOAD, token, success);
        mUploadQueue.remove(token);
        final KiiFile f = file;
        if (!success) {
            // workaround: delete the uploaded kiifile, it is dirty data
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (f != null) {
                            f.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        doRefresh();
    }

    @Override
    public void onUpdateCompleted(int token, boolean success, KiiFile file,
            Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_UPDATE, token, success);
        mUpdateQueue.remove(token);
        doRefresh();
    }

    @Override
    public void onDownloadBodyCompleted(int token, boolean success,
            Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_DOWNLOAD, token, success);
        mDownQueue.remove(token);
        doRefresh();
    }

    private void showTaskCompleteToast(int action, int token, boolean success) {
        mTokenMap.delete(token);
        if (action == ActionType.ACTION_LIST_FILES) {
            // only show list complete after list trash
            return;
        }
        Toast.makeText(mContext, Utils.getUserActionString(action, success),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefreshCompleted(int token, boolean success, KiiFile file,
            Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_LIST_FILES, token, success);
    }

    @Override
    public void onEmptyTrashCompleted(int token, boolean success,
            Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_EMPTY_TRASH, token, success);
        doRefresh();
    }

    @Override
    public void onDeleteCompleted(int token, boolean success,
            Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_DELETE, token, success);
        doRefresh();
    }

    @Override
    public void onMoveTrashCompleted(int token, boolean success, KiiFile file,
            Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_TRASH, token, success);
        doRefresh();
    }

    @Override
    public void onRestoreTrashCompleted(int token, boolean success,
            KiiFile file, Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_RESTORE, token, success);
        doRefresh();
    }

    @Override
    public void onListWorkingCompleted(int token, boolean success,
            List<KiiFile> files, Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_LIST_FILES, token, success);
        if (success) {
            mCloudFiles.clear();
            mCloudFiles.addAll(files);
        }
        Log.d(TAG, "onListWorkingCompleted");
        int tk = KiiFile.listTrashedFiles(this, Constants.ANDROID_EXT);
        addTokenAction(tk, ActionType.ACTION_LIST_TRASH);
    }

    @Override
    public void onListTrashCompleted(int token, boolean success,
            List<KiiFile> files, Exception exception) {
        showTaskCompleteToast(ActionType.ACTION_LIST_TRASH, token, success);
        if (success) {
            mTrashFiles.clear();
            mTrashFiles.addAll(files);
        }
        Log.d(TAG, "onListTrashCompleted");
        mContext.sendBroadcast(new Intent(Constants.UI_REFRESH_INTENT));
        if (KiiCloudClient.getInstance(mContext).mActivity != null) {
            KiiCloudClient.getInstance(mContext).mActivity.stopProgress();
        }
    }

    @Override
    public void onTaskCancel(int token) {
        Log.d(TAG, "onTaskCancel: token is " + token);
        if (mTokenMap.indexOfKey(token) > 0) {
            Toast.makeText(mContext,
                    Utils.getResultString(mTokenMap.get(token), false),
                    Toast.LENGTH_SHORT).show();
            mTokenMap.delete(token);
        }
    }

    @Override
    public void onTaskStart(int token) {
        // TODO: show notification;
    }

    void addToDownQueue(int token, KiiFile file) {
        mDownQueue.put(token, file);
    }

    SparseArray<KiiFile> getDownQueue() {
        return mDownQueue;
    }

    void addToUploadQueue(int token, KiiFile f) {
        mUploadQueue.put(token, f);
    }

    void addToUpdateQueue(int token, KiiFile file) {
        mUpdateQueue.put(token, file);
    }

    SparseArray<KiiFile> getUploadQueue() {
        return mUploadQueue;
    }

    SparseArray<KiiFile> getUpdateQueue() {
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

    boolean hasWorkInProgress() {
        if (mTokenMap.size() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    void doRefresh() {
        if (!hasWorkInProgress()) {
            KiiCloudClient.getInstance(mContext).refresh();
        }
    }

}
