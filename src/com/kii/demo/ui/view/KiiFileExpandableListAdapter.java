package com.kii.demo.ui.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.kii.cloud.engine.KiiCloudClient;
import com.kii.cloud.storage.KiiFile;
import com.kii.demo.utils.MimeInfo;
import com.kii.demo.utils.MimeUtil;
import com.kii.demo.utils.UiUtils;
import com.kii.demo.utils.Utils;

public class KiiFileExpandableListAdapter extends BaseExpandableListAdapter {

    // itemsList is the list of items that contain items
    // if items[] is null, it is reference to trash folder
    ArrayList<KiiFileList> itemsList = new ArrayList<KiiFileList>();

    Context mContext = null;
    View.OnClickListener mOnClickListener = null;

    KiiCloudClient kiiClient = null;

    private static HashMap<String, Drawable> ICON_CACHE = new HashMap<String, Drawable>();

    private int mType = -1;
    public static final int TYPE_DATA = 1;
    public static final int TYPE_PROGRESS = 2;

    public KiiFileExpandableListAdapter(Context activity,
            KiiCloudClient kiiClient, int type, OnClickListener listener) {
        if (kiiClient == null) {
            throw new NullPointerException();
        }
        mContext = activity;
        mOnClickListener = listener;
        this.kiiClient = kiiClient;
        this.mType = type;
        addDataSet(itemsList);
    }

    @Override
    public void notifyDataSetChanged() {
        itemsList = new ArrayList<KiiFileList>();
        ICON_CACHE.clear();
        addDataSet(itemsList);
        super.notifyDataSetChanged();
    }

    protected void addDataSet(ArrayList<KiiFileList> itemsList) {
        if (mType == TYPE_PROGRESS) {
            KiiFile[] downloadFiles = kiiClient.getDownloadList();
            if ((downloadFiles != null) && (downloadFiles.length > 0)) {
                itemsList.add(new KiiFileList("Downloading", downloadFiles));
            }

            KiiFile[] uploadingFiles = kiiClient.getUploadList();
            if ((uploadingFiles != null) && (uploadingFiles.length > 0)) {
                itemsList.add(new KiiFileList("Progress", uploadingFiles));
            }
        } else if (mType == TYPE_DATA) {
            KiiFile[] trashFiles = kiiClient.getTrashFiles();
            if ((trashFiles != null) && (trashFiles.length > 0)) {
                itemsList.add(new KiiFileList("Trash", trashFiles));
            } else {
                itemsList.add(new KiiFileList("Trash"));
            }

            KiiFile[] backupFiles = kiiClient.getBackupFiles();
            if ((backupFiles != null) && (backupFiles.length > 0)) {
                itemsList.add(new KiiFileList("Backup", backupFiles));
            } else {
                itemsList.add(new KiiFileList("Backup"));
            }
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        KiiFileList group = itemsList.get(groupPosition);
        return group.getChild(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        KiiFileList group = itemsList.get(groupPosition);
        return group.getChildrenCount();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        KiiFile file = (KiiFile) getChild(groupPosition, childPosition);
        Drawable icon = getKiiFileMainIcon(file);
        KiiListItemView view;
        if (convertView == null) {
            view = new KiiListItemView(mContext, file, kiiClient, icon,
                    mOnClickListener);
        } else {
            view = (KiiListItemView) convertView;
            view.refreshWithNewKiiFile(file, icon);
        }
        String caption = UiUtils.getKiiFileCaption(file, mType);
        String subCaption = Formatter.formatFileSize(mContext,
                file.getFileSize());
        view.setCaption(caption, subCaption);
        return view;
    }

    private static Drawable getKiiFileMainIcon(KiiFile file) {
        Drawable icon = null;
        MimeInfo mime = MimeUtil.getInfoByKiiFile(file);
        byte[] sThumbnail = null;
        if (mime != null) {
            sThumbnail = file.getThumbnail();
        }
        try {
            if (sThumbnail != null && sThumbnail.length > 0) {
                if (ICON_CACHE.containsKey(file.toUri().toString())) {
                    icon = ICON_CACHE.get(file.toUri().toString());
                } else {
                    icon = Utils.getThumbnailByResize(sThumbnail);
                    ICON_CACHE.put(file.toUri().toString(), icon);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return icon;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return itemsList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return itemsList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {

        KiiListItemView view = null;
        KiiFileList group = (KiiFileList) getGroup(groupPosition);

        if (convertView == null) {
            view = new KiiListItemView(mContext, group);
        } else {
            view = (KiiListItemView) convertView;
            view.refreshWithNewGroup(group);
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
