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

package com.kii.demo.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.engine.KiiCloudClient;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.cloud.storage.exception.CloudExecutionException;
import com.kii.demo.R;
import com.kii.demo.utils.UiUtils;

public class StartActivity extends Activity {
    protected static final String TAG = "StartActivity";

    public static final String ACTION_ENTER_PASSWORD = "com.kii.demo.sync.ENTER_PASSWORD";
    public static final String ACTION_LOGOUT = "com.kii.demo.sync.LOGOUT";

    EditText mUsr = null;
    EditText mPwd = null;

    ProgressBar mProgressStatus = null;
    TextView mProgressMsg = null;
    TextView mLastSyncTime = null;
    TextView mStorage = null;
    TextView mServerSite = null;
    TextView mUserInfo = null;
    Button mRegister = null;
    Button mViewer = null;
    Button mLogin = null;
    Button mLogout = null;
    Button mChangePwd = null;
    Button mSetting = null;

    private static StartActivity mActivity = null;
    private UserCallback mUserCallback;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mActivity = this;

        mStorage = (TextView) findViewById(R.id.storage);
        mUsr = (EditText) findViewById(R.id.username);
        mPwd = (EditText) findViewById(R.id.password);

        mProgressStatus = (ProgressBar) findViewById(R.id.progress);
        mProgressMsg = (TextView) findViewById(R.id.progress_text);
        mLastSyncTime = (TextView) findViewById(R.id.lastSyncTime);
        mServerSite = (TextView) findViewById(R.id.server);
        mUserInfo = (TextView) findViewById(R.id.kiiid);
        mServerSite.setVisibility(View.GONE);
        mUserInfo.setVisibility(View.GONE);

        mRegister = (Button) findViewById(R.id.register);
        mRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDemo();
            }
        });

        mLogin = (Button) findViewById(R.id.login);
        mLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogin.setEnabled(false);
                loginDemo();
            }
        });

        mLogout = (Button) findViewById(R.id.logout);
        mLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDemo();
            }
        });

        mChangePwd = (Button) findViewById(R.id.changePwd);
        mChangePwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changePwd();
            }
        });

        mViewer = (Button) findViewById(R.id.viewer);
        mViewer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KiiCloudClient.getInstance(mActivity) != null) {
                    Intent intent = new Intent(mActivity,
                            FragmentTabsPager.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(mActivity, "Kii Client is not ready",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSetting = (Button) findViewById(R.id.launch_settings);
        mSetting.setVisibility(View.GONE);

        updateView();

        Intent i = getIntent();
        if ((i != null) && (i.getAction() != null)) {
            if (i.getAction().compareToIgnoreCase(ACTION_ENTER_PASSWORD) == 0) {
                updatePwd();
            } else if (i.getAction().compareToIgnoreCase(ACTION_LOGOUT) == 0) {
                logoutDemo();
            }
        }
        mUserCallback = new UserCallback();
    }

    void updateView() {
        Log.d(TAG, "upateView: client is " + KiiCloudClient.getInstance(this));
        KiiUser um = KiiCloudClient.getInstance(this).getloginUser();
        if (um != null) {
            Intent i = getIntent();
            if ((i == null)
                    || (i.getAction() == null)
                    || ((i.getAction().compareToIgnoreCase(
                            Intent.ACTION_CONFIGURATION_CHANGED) != 0) && (i
                            .getAction().compareToIgnoreCase(
                                    ACTION_ENTER_PASSWORD) != 0))) {
                Intent intent = new Intent(mActivity, FragmentTabsPager.class);
                startActivity(intent);
                finish();
            } else {
                mUsr.setText(um.getEmail());
                mUsr.setEnabled(false);

                mRegister.setEnabled(false);
                mLogin.setEnabled(true);
                mLogin.setText("ReLogIn");
                mViewer.setEnabled(true);
                mLogout.setEnabled(true);
                mChangePwd.setEnabled(true);
                mSetting.setEnabled(true);
                updateSyncStatus();
            }
        } else {
            Toast.makeText(mActivity, "Register or Login", Toast.LENGTH_SHORT)
                    .show();
            mRegister.setEnabled(true);
            mLogin.setEnabled(true);
            mLogin.setText("Log In");
            mUsr.setEnabled(true);
            mViewer.setEnabled(false);
            mLogout.setEnabled(false);
            mChangePwd.setEnabled(false);
            mSetting.setEnabled(false);
            updateSyncStatus();
        }
    }

    protected void updatePwd() {
        final EditText input = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verification has failed")
                .setMessage("Enter New Password:")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newPassword = input.getText().toString();
                        if (TextUtils.isEmpty(newPassword)) {
                            Toast.makeText(mActivity,
                                    "Password can't be nothing.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            updatePwd(newPassword);
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                Toast.makeText(mActivity,
                                        "Cancel Enter Password.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void updatePwd(String newPassword) {
        mPwd.setText(newPassword);
        KiiCloudClient.getInstance(this).login(mUserCallback,
                mUsr.getText().toString(), newPassword);
    }

    private void changePwd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.change_password_view, null);
        mAlertDialog = builder
                .setTitle("Change Password")
                .setMessage("Enter new password:")
                .setView(v)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String oldPassword = ((EditText) v
                                .findViewById(R.id.old_password)).getText()
                                .toString();
                        String newPassword = ((EditText) v
                                .findViewById(R.id.new_password)).getText()
                                .toString();
                        String newConfirm = ((EditText) v
                                .findViewById(R.id.confirm_new_password))
                                .getText().toString();
                        if (TextUtils.isEmpty(newPassword)) {
                            Toast.makeText(mActivity,
                                    "Password can't be nothing.",
                                    Toast.LENGTH_SHORT).show();
                        } else if (!newPassword.contentEquals(newConfirm)) {
                            Toast.makeText(mActivity, "Password not same!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            changePwd(newPassword, oldPassword);
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                Toast.makeText(mActivity,
                                        "Cancel Change Password.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).create();
        mAlertDialog.show();
    }

    private void changePwd(String newPassword, String oldPassword) {
        mPwd.setText(newPassword);
        KiiCloudClient.getInstance(this).changePassword(mUserCallback,
                newPassword, oldPassword);
    }

    private void logoutDemo() {
        KiiCloudClient.getInstance(this).logout();
        updateView();
    }

    private void registerDemo() {
        try {
            KiiUser user = new KiiUser();
            user.setEmail(mUsr.getText().toString());
            String username = "Test" + System.currentTimeMillis();
            KiiCloudClient.getInstance(this).register(mUserCallback, user,
                    username, mPwd.getText().toString());
            showDialog(0);
        } catch (Exception e) {
            Log.d(TAG, "exception: " + e.getMessage());
            Toast.makeText(this,
                    "Please check email or username input: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loginDemo() {
        if (KiiCloudClient.getInstance(this).getloginUser() != null) {
            KiiCloudClient.getInstance(this).logout();
        }
        KiiCloudClient.getInstance(this).login(mUserCallback,
                mUsr.getText().toString(), mPwd.getText().toString());
        showDialog(0);
    }

    static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private static AlertDialog mAlertDialog = null;

    @Override
    public void onDestroy() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        super.onDestroy();
    }

    private void updateSyncStatus() {
        KiiCloudClient kClient = KiiCloudClient.getInstance(mActivity);
        if (kClient == null) {
            return;
        }
        mLastSyncTime.setText(UiUtils.getLastSyncTime(this));
    }

    public class UserCallback extends KiiUserCallBack {

        @Override
        public void onLoginCompleted(int token, boolean success, KiiUser user,
                Exception exception) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            Log.d(TAG, "onTaskCompleted, success? " + success);
            if (success) {
                Toast.makeText(mActivity,
                        mActivity.getString(R.string.login_success),
                        Toast.LENGTH_SHORT);
                Intent intent = new Intent(mActivity, FragmentTabsPager.class);
                mActivity.startActivity(intent);
                finish();
            } else {
                if (exception instanceof CloudExecutionException) {
                    CloudExecutionException cloudException = (CloudExecutionException) exception;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Error:" + cloudException.getError());
                    sb.append("\n\n");
                    sb.append("Exception:" + cloudException.getException());
                    sb.append("\n\n");
                    sb.append("Error Details:"
                            + cloudException.getErrorDetails());
                    String msg = sb.toString();
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            StartActivity.this);
                    builder.setTitle("LogIn Failed")
                            .setMessage(msg)
                            .setNegativeButton(getString(android.R.string.ok),
                                    null).show();
                }

                updateView();
            }
        }

        @Override
        public void onTaskCancel(int token) {
            // TODO Auto-generated method stub
            super.onTaskCancel(token);
        }

        @Override
        public void onRegisterCompleted(int token, boolean success,
                KiiUser user, Exception exception) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (success) {
                Toast.makeText(mActivity,
                        mActivity.getString(R.string.register_success),
                        Toast.LENGTH_LONG).show();
                updateView();
            } else {
                if (exception instanceof CloudExecutionException) {
                    CloudExecutionException cloudException = (CloudExecutionException) exception;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Error:" + cloudException.getError());
                    sb.append("\n\n");
                    sb.append("Exception:" + cloudException.getException());
                    sb.append("\n\n");
                    sb.append("Error Details:"
                            + cloudException.getErrorDetails());
                    String msg = sb.toString();
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            StartActivity.this);
                    builder.setTitle("Register Failed")
                            .setMessage(msg)
                            .setNegativeButton(getString(android.R.string.ok),
                                    null).show();
                }
                updateView();
            }
        }

        @Override
        public void onChangePasswordCompleted(int token, boolean success,
                Exception exception) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (success) {
                Toast.makeText(mActivity,
                        mActivity.getString(R.string.change_password_success),
                        Toast.LENGTH_LONG).show();
                updateView();
            } else {
                if (exception instanceof CloudExecutionException) {
                    CloudExecutionException cloudException = (CloudExecutionException) exception;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Error:" + cloudException.getError());
                    sb.append("\n\n");
                    sb.append("Exception:" + cloudException.getException());
                    sb.append("\n\n");
                    sb.append("Error Details:"
                            + cloudException.getErrorDetails());
                    String msg = sb.toString();
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            StartActivity.this);
                    builder.setTitle("Change Password Failed")
                            .setMessage(msg)
                            .setNegativeButton(getString(android.R.string.ok),
                                    null).show();
                }
                updateView();
            }
        }

    }

    ProgressDialog mProgressDialog;

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Please wait..");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                return mProgressDialog;
            default:
                return null;
        }
    }
}