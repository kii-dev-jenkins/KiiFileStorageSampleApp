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

package com.kii.demo.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.engine.Constants;
import com.kii.cloud.engine.KiiCloudClient;
import com.kii.cloud.storage.KiiFile;
import com.kii.demo.R;
import com.kii.demo.ui.ProgressListActivity;
import com.kii.demo.ui.view.ActionItem;
import com.kii.demo.ui.view.KiiFileExpandableListAdapter;
import com.kii.demo.ui.view.QuickAction;
import com.kii.demo.utils.UiUtils;
import com.kii.demo.utils.Utils;

public class KiiFileFragment extends Fragment {
    private static View mView;
    private ExpandableListView mList = null;
    KiiFileExpandableListAdapter mAdapter;

    final static int MENU_RESTORE_TRASH = 201;
    final static int MENU_MOVE_TRASH = 202;
    final static int MENU_DELETE = 203;
    final static int MENU_DELETE_LOCAL = 204;
    final static int MENU_DOWNLOAD = 205;
    final static int MENU_CANCEL = 206;

    private QuickAction mQuickAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.expandable_list_with_header,
                container, false);
        Button b = (Button) mView.findViewById(R.id.button_left);
        b.setText(getString(R.string.button_refresh));
        b.setOnClickListener(mClickListener);
        b = (Button) mView.findViewById(R.id.button_right);
        b.setText(getString(R.string.header_upload));
        b.setOnClickListener(mClickListener);
        refreshUI(getActivity());
        mList = (ExpandableListView) mView.findViewById(android.R.id.list);
        setHasOptionsMenu(true);
        return mView;
    }

    Receiver mReceiver = new Receiver();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new KiiFileExpandableListAdapter(getActivity(),
                KiiCloudClient.getInstance(getActivity()),
                KiiFileExpandableListAdapter.TYPE_DATA, mClickListener);
        mList.setAdapter(mAdapter);
        getActivity().registerReceiver(mReceiver,
                new IntentFilter(Constants.UI_REFRESH_INTENT));
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public final static int PROGRESS_START = 1;
    public final static int PROGRESS_END = 2;
    public final static int PROGRESS_UPDATE = 3;

    private static void refreshUI(Context context) {
        // refresh the header text;
        TextView tv = (TextView) mView.findViewById(R.id.header_text);
        tv.setText(UiUtils.getLastSyncTime(context));
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_left:
                    syncRefresh();
                    break;
                case R.id.button_right:
                    Intent i = new Intent(getActivity(),
                            ProgressListActivity.class);
                    getActivity().startActivity(i);
                    break;
                case R.id.list_complex_more_button:
                    View row = (View) v.getTag();
                    final KiiFile file = (KiiFile) row.getTag();
                    mQuickAction = new QuickAction(getActivity());
                    setActions(file.isInTrash());
                    if (mQuickAction.getActionItem(0) != null) {
                        mQuickAction
                                .setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                                    @Override
                                    public void onItemClick(QuickAction source,
                                            int pos, int actionId) {
                                        handleKiiFileAction(file, actionId);
                                    }
                                });
                        mQuickAction.show(v);
                    }
                    break;
            }
        }
    };

    private void setActions(boolean inTrash) {
        if (inTrash) {
            mQuickAction.addActionItem(new ActionItem(MENU_RESTORE_TRASH,
                    getString(R.string.restore_from_trash)));
        } else {
            mQuickAction.addActionItem(new ActionItem(MENU_MOVE_TRASH,
                    getString(R.string.move_to_trash)));
            mQuickAction.addActionItem(new ActionItem(MENU_DOWNLOAD,
                    getString(R.string.download)));
        }
        mQuickAction.addActionItem(new ActionItem(MENU_DELETE,
                getString(R.string.delete_backup_copy)));
    }

    /**
     * get new records from server if there are any
     */
    private void syncRefresh() {
        KiiCloudClient.getInstance(getActivity()).refresh();
    }

    private void handleKiiFileAction(final KiiFile file, int actionId) {
        final KiiCloudClient client = KiiCloudClient.getInstance(getActivity());
        switch (actionId) {
            case MENU_RESTORE_TRASH:
                client.restoreFromTrash(file);
                break;
            case MENU_MOVE_TRASH:
                client.moveKiiFileToTrash(file);
                break;
            case MENU_DELETE:
                client.delete(file, false);
                break;
            case MENU_DELETE_LOCAL:
                client.delete(file, true);
                break;
            case MENU_CANCEL:
                client.cancel(file);
                break;
            case MENU_DOWNLOAD:
                Toast.makeText(getActivity(),
                        "Download at:" + Utils.getKiiFileDownloadPath(file),
                        Toast.LENGTH_SHORT).show();
                client.download(file, Utils.getKiiFileDownloadPath(file));
                break;

        }
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.UI_REFRESH_INTENT)) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

}