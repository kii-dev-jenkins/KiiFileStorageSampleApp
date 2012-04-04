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
