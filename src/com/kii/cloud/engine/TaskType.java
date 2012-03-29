package com.kii.cloud.engine;

public class TaskType {
    public static class FileTask {
        public static final int UPLOAD = 0;
        public static final int DOWNLOAD = 1;
        public static final int UPDATE = 2;
        public static final int UPDATE_METADATA = 3;
        public static final int REFRESH = 4;
        public static final int DELETE = 5;
        public static final int MOVE_TRASH = 6;
        public static final int RESTORE_TRASH = 7;
        public static final int LIST_CLOUD = 8;
        public static final int LIST_TRASH = 9;
    }

    public static class UserTask {
        public static final int LOGIN = 0;
        public static final int REGISTER = 1;
        public static final int CHANGE_PASSWORD = 2;
        public static final int UPDATE = 3;
        public static final int DELETE = 4;
        public static final int REFRESH = 5;
    }
}
