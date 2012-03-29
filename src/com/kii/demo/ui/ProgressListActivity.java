package com.kii.demo.ui;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;

import com.kii.cloud.engine.KiiCloudClient;
import com.kii.cloud.storage.KiiFile;
import com.kii.demo.R;
import com.kii.demo.ui.view.KiiFileExpandableListAdapter;
import com.kii.demo.utils.UiUtils;

public class ProgressListActivity extends ExpandableListActivity implements
        View.OnClickListener {
    KiiFileExpandableListAdapter mAdapter = null;
    private static final int MENU_ITEM_CANCEL = 2;

    private static final int OPTION_MENU_SETTING = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.expandable_list_with_header);
        Button b = (Button) findViewById(R.id.button_left);
        b.setText(getString(R.string.pause));
        b.setVisibility(View.GONE);
        b = (Button) findViewById(R.id.button_right);
        b.setText(getString(R.string.resume));
        b.setVisibility(View.GONE);
        setHeaderText();
        connect();
        registerForContextMenu(getExpandableListView());
    }

    @Override
    protected void onPause() {
        handler.removeMessages(PROGRESS_AUTO);
        handler.removeMessages(PROGRESS_END);
        super.onPause();
    }

    @Override
    protected void onResume() {
        handler.sendEmptyMessageDelayed(PROGRESS_AUTO, 500);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void connect() {
        adpterSetup();
    }

    static final String TAG = "ProgressListActivity";

    protected void adpterSetup() {
        if (mAdapter == null) {
            KiiCloudClient client = KiiCloudClient.getInstance(this);
            mAdapter = new KiiFileExpandableListAdapter(this, client,
                    KiiFileExpandableListAdapter.TYPE_PROGRESS, this);
            setListAdapter(mAdapter);
            handler.sendEmptyMessageDelayed(PROGRESS_AUTO, 500);
            updateProgress();
        }
    }

    public final static int PROGRESS_START = 1;
    public final static int PROGRESS_END = 3;
    public final static int PROGRESS_AUTO = 4;
    public final static int PROGRESS_UPDATE = 6;

    private int updateProgress() {
        KiiCloudClient kiiClient = KiiCloudClient.getInstance(this);
        if (kiiClient != null) {
            int progress = kiiClient.getOverallProgress();
            if ((progress > 0)) {
                setProgress(progress);
                mAdapter.notifyDataSetChanged();
                return progress;
            }
        }
        return 0;
    }

    private int mProgress = 0;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_AUTO:
                    mProgress = updateProgress();
                    if ((mProgress > 0) || (mAdapter.getGroupCount() > 0)) {
                        setProgressBarIndeterminateVisibility(true);
                        setProgressBarVisibility(true);
                        handler.sendEmptyMessageDelayed(PROGRESS_AUTO, 5000);
                        setHeaderText();
                    } else {
                        setProgressBarIndeterminateVisibility(false);
                        setProgressBarVisibility(false);
                    }
                    break;
                case PROGRESS_START:
                    handler.removeMessages(PROGRESS_AUTO);
                    handler.removeMessages(PROGRESS_END);
                    handler.sendEmptyMessageDelayed(PROGRESS_UPDATE, 500);
                    setProgressBarIndeterminateVisibility(true);
                    setProgressBarVisibility(true);
                    if ((msg.obj != null) && (msg.obj instanceof String)) {
                        setTitle((String) msg.obj);
                    }
                case PROGRESS_UPDATE:
                    handler.removeMessages(PROGRESS_AUTO);
                    handler.sendEmptyMessageDelayed(PROGRESS_UPDATE, 5000);
                    mProgress = updateProgress();
                    setHeaderText();
                    setProgressBarIndeterminateVisibility(true);
                    setProgressBarVisibility(true);
                    mAdapter.notifyDataSetChanged();
                    break;
                case PROGRESS_END:
                default:
                    handler.removeMessages(PROGRESS_AUTO);
                    handler.removeMessages(PROGRESS_UPDATE);
                    handler.removeMessages(PROGRESS_END);
                    setProgressBarIndeterminateVisibility(false);
                    setProgressBarVisibility(false);
                    setTitle(R.string.app_name);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    mProgress = updateProgress();
                    setHeaderText();
                    return;
            }

        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView
                .getPackedPositionType(info.packedPosition);
        int group = ExpandableListView
                .getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView
                .getPackedPositionChild(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            KiiFile kFile = (KiiFile) mAdapter.getChild(group, child);
            if ((kFile != null)) {
                menu.setHeaderTitle(kFile.getTitle());
                KiiCloudClient kiiClient = KiiCloudClient.getInstance(this);
                if (kiiClient == null) {
                    UiUtils.showToast(this, "Not ready.");
                    return;
                }
                menu.add(MENU_ITEM_CANCEL, 0, 0, getString(R.string.cancel));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
                .getMenuInfo();
        int type = ExpandableListView
                .getPackedPositionType(info.packedPosition);
        KiiCloudClient client = KiiCloudClient.getInstance(this);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int groupPos = ExpandableListView
                    .getPackedPositionGroup(info.packedPosition);
            int childPos = ExpandableListView
                    .getPackedPositionChild(info.packedPosition);
            final KiiFile kFile = (KiiFile) mAdapter.getChild(groupPos,
                    childPos);
            switch (item.getGroupId()) {
                case MENU_ITEM_CANCEL:
                    client.cancel(kFile);
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.list_complex_more_button:
                View row = (View) v.getTag();
                getExpandableListView().showContextMenuForChild(row);
                break;
        }
    }

    public void handleButtonLeft(View v) {
        //TODO: don't need
    }

    public void handleButtonRight(View v) {
        //TODO: remove the two buttons
    }

    private void setHeaderText() {
        final String lastSyncTime = UiUtils.getLastSyncTime(this);
        ProgressListActivity.this.getExpandableListView().post(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) findViewById(R.id.header_text);
                if ((mProgress > 0) && (mProgress < 100)) {
                    tv.setText("Progress: " + mProgress + "%");
                } else {
                    tv.setText(lastSyncTime);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, OPTION_MENU_SETTING, 0, getString(R.string.settings));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case OPTION_MENU_SETTING:
                Intent intent = new Intent(this, StartActivity.class);
                intent.setAction(Intent.ACTION_CONFIGURATION_CHANGED);
                startActivity(intent);
                break;
        }
        return true;
    }

}
