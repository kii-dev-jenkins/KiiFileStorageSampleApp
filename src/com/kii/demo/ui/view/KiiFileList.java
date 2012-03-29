package com.kii.demo.ui.view;

import com.kii.cloud.storage.KiiFile;


public class KiiFileList {
    private KiiFile root = null;
    private KiiFile[] children = null;
    private String title = null;

    KiiFileList(KiiFile kFile) {
        root = kFile;
        title = root.getTitle();
    }

    /**
     * Create a empty KiiFile with title only
     * 
     * @param title
     */
    KiiFileList(String title) {
        this.title = title;
    }

    /**
     * Create a empty KiiFile with title only
     * 
     * @param title
     */
    KiiFileList(String title, KiiFile[] list) {
        this.title = title;
        children = list;
    }

    boolean hasChildren() {
        if (children == null) {
            return false;
        }
        return true;
    }

    KiiFile getChild(int index) {
        if ((children != null) && (children.length > index)) {
            return children[index];
        }
        return null;
    }

    int getChildrenCount() {
        if (children != null) {
            return children.length;
        }
        return 0;
    }

    KiiFile getParent() {
        return root;
    }

    String getTitle() {
        return title + " (" + getChildrenCount() + ")";
    }
}