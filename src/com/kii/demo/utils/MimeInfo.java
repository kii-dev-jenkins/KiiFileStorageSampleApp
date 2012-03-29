package com.kii.demo.utils;


public class MimeInfo {
    String mimeType;
    int icon;

    MimeInfo(int icon, String mimeType) {
        this.mimeType = mimeType;
        this.icon = icon;
        MimeUtil.mimeInfos.put(mimeType.toLowerCase(), this);
    }

    public int getIconID() {
        return icon;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isType(String type) {
        return mimeType.startsWith(type.toLowerCase());
    }
}
