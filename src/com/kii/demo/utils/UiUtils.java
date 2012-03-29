package com.kii.demo.utils;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.storage.KiiFile;
import com.kii.demo.R;

public class UiUtils {

    public static void setTrashStatus(ImageView statusIcon, int status) {
        if (status == 1) {
            statusIcon.setImageResource(R.drawable.sync_cloud);
            statusIcon.setVisibility(View.VISIBLE);
        } else if (status == 2) {
            statusIcon.setImageResource(R.drawable.sync_trashcan);
            statusIcon.setVisibility(View.VISIBLE);
        } else {
            statusIcon.setVisibility(View.GONE);
        }
    }

    public static Intent getLaunchFileIntent(String path, MimeInfo mime) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        if (mime == null) {
            return null;
        }
        Intent commIntent = null;
        Uri fileUri = Uri.fromFile(new File(path));
        commIntent = new Intent(Intent.ACTION_VIEW);
        commIntent.setDataAndType(fileUri, mime.getMimeType());
        return commIntent;
    }

    public static Intent getLaunchURLIntent(URL url, String mimeType) {
        Intent commIntent = null;
        commIntent = new Intent(Intent.ACTION_VIEW);
        if (mimeType.startsWith("video")) {
            commIntent.setDataAndType(Uri.parse(url.toString()), "video/*");
        } else if (mimeType.startsWith("audio")) {
            commIntent.setDataAndType(Uri.parse(url.toString()), mimeType);
        } else {
            commIntent.setData(Uri.parse(url.toString()));
        }
        return commIntent;
    }

    public static String getLastSyncTime(Context context) {
        BackupPref.init(context);
        long backupTime = BackupPref.getLastRefreshTime();
        if (backupTime > 0) {
            return String.format("Last successful sync is %s",
                    (String) DateUtils.getRelativeTimeSpanString(backupTime,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_RELATIVE
                                    | DateUtils.FORMAT_ABBREV_ALL));
        } else {
            return context.getString(R.string.no_last_sync_time);
        }
    }

    public static View setIcon(int iconID, View curView) {
        ImageView imageView = (ImageView) curView
                .findViewById(R.id.list_complex_icon);
        if (iconID < 0) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setImageResource(iconID);
        }

        return curView;
    }

    /**
     * Default is 2 lines text, change to single line text
     * 
     * @param text
     * @param curView
     * @return
     */
    public static View setOneLineText(String text, boolean alighCenter,
            View curView) {
        TextView textView = (TextView) curView
                .findViewById(R.id.list_complex_1line_title);
        textView.setText(text);
        if (alighCenter) {
            textView.setGravity(Gravity.CENTER);
        }
        curView.findViewById(R.id.list_complex_1line_text).setVisibility(
                View.VISIBLE);
        curView.findViewById(R.id.list_complex_2lines_text).setVisibility(
                View.GONE);
        return curView;
    }

    public static View setOneLineText(SpannableString title, View curView) {
        TextView textView = (TextView) curView
                .findViewById(R.id.list_complex_1line_title);
        textView.setText(title);
        curView.findViewById(R.id.list_complex_1line_text).setVisibility(
                View.VISIBLE);
        curView.findViewById(R.id.list_complex_2lines_text).setVisibility(
                View.GONE);
        return curView;
    }

    /**
     * Set 2 lines text, title and caption
     * 
     * @param title
     * @param caption
     * @param curView
     * @return
     */
    public static View setTwoLinesText(SpannableString title,
            SpannableString caption, View curView) {

        TextView titleView = (TextView) curView
                .findViewById(R.id.list_complex_title);
        TextView captionView = (TextView) curView
                .findViewById(R.id.list_complex_caption);
        titleView.setText(title);

        if (caption != null) {
            captionView.setText(caption);
        } else {
            captionView.setText("");
        }

        captionView
                .setPadding(captionView.getPaddingLeft(),
                        captionView.getPaddingTop(), 50,
                        captionView.getPaddingBottom());

        curView.findViewById(R.id.list_complex_1line_text).setVisibility(
                View.GONE);
        curView.findViewById(R.id.list_complex_2lines_text).setVisibility(
                View.VISIBLE);
        curView.findViewById(R.id.list_complex_sub_caption).setVisibility(
                View.GONE);
        return curView;
    }

    public static View setTwoLinesText(SpannableString title,
            SpannableString caption, String subCaption, int iconId, View curView) {
        TextView titleView = (TextView) curView
                .findViewById(R.id.list_complex_title);
        TextView captionView = (TextView) curView
                .findViewById(R.id.list_complex_caption);
        titleView.setText(title);
        if (caption != null) {
            captionView.setText(caption);
        } else {
            captionView.setText("");
        }

        // text on the bottom right
        TextView subCaptionView = (TextView) curView
                .findViewById(R.id.list_complex_sub_caption);

        if (subCaption != null) {
            subCaptionView.setText(subCaption);
        } else {
            subCaptionView.setText("");
        }

        curView.findViewById(R.id.list_complex_1line_text).setVisibility(
                View.GONE);
        curView.findViewById(R.id.list_complex_2lines_text).setVisibility(
                View.VISIBLE);
        curView.findViewById(R.id.list_complex_sub_caption).setVisibility(
                View.VISIBLE);
        return curView;
    }

    /**
     * Set the icon
     * 
     * @param icon
     *            if null, ignore
     * @param curView
     * @return curView
     */
    public static View setIcon(Drawable icon, View curView) {
        ImageView imageView = (ImageView) curView
                .findViewById(R.id.list_complex_icon);

        if (icon != null) {
            imageView.setImageDrawable(icon);
            curView.findViewById(R.id.list_complex_icon_main).setVisibility(
                    View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
            curView.findViewById(R.id.list_complex_icon_main).setVisibility(
                    View.GONE);
        }

        return curView;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static String getKiiFileCaption(KiiFile file, int type) {
        String caption = (String) DateUtils.formatSameDayTime(
                file.getModifedTime(), System.currentTimeMillis(),
                DateFormat.SHORT, DateFormat.SHORT);
        return caption;
    }

    /**
     * Convert the error code to error message
     * 
     * @param code
     * @param context
     * @return
     */
    public static String getErrorMsg(int code, Context context) {
        // TODO: return some valid message
        return "";
    }

}
