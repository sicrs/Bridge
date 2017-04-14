package moe.shizuku.bridge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import moe.shizuku.bridge.adapter.ChooserAdapter;
import moe.shizuku.bridge.utils.IntentUtils;
import moe.shizuku.bridge.utils.ResolveInfoHelper;
import moe.shizuku.bridge.widget.ResolverDrawerLayout;

/**
 * Created by Rikka on 2017/4/2.
 */

public class ChooserActivity extends Activity {

    public static final String EXTRA_RESOLVE_INFO = BuildConfig.APPLICATION_ID + ".intent.extra.RESOLVE_INFO";

    /**
     * Start target select
     *
     * @param context
     */
    public static void start(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setDataAndType(Uri.parse("content://example"), "*/*");
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(shareIntent, 0);

        start(context, null, null, ResolveInfoHelper.filter(resolveInfo, true));
    }

    /**
     * Start chooser
     *
     * @param context
     * @param uri
     * @param type
     * @param resolveInfo
     */
    public static void start(Context context, Uri uri, String type, List<ResolveInfo> resolveInfo) {
        Intent intent = new Intent(context, ChooserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.setDataAndType(uri, type);
        intent.putParcelableArrayListExtra(EXTRA_RESOLVE_INFO, (ArrayList<? extends Parcelable>) resolveInfo);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooser_grid);

        ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(R.id.contentPanel);
        rdl.setOnDismissedListener(new ResolverDrawerLayout.OnDismissedListener() {
            @Override
            public void onDismissed() {
                finish();
            }
        });

        boolean editMode = getIntent().getData() == null;
        ArrayList<ResolveInfo> resolveInfo = getIntent().getParcelableArrayListExtra(EXTRA_RESOLVE_INFO);

        TextView title = (TextView) findViewById(android.R.id.title);
        title.setText(editMode ? R.string.select_forward_apps_title : R.string.forward_title);

        TextView empty = (TextView) findViewById(android.R.id.empty);
        empty.setVisibility(resolveInfo.isEmpty() ? View.VISIBLE : View.GONE);

        if (!editMode) {
            empty.setText(R.string.select_first);
        }

        TextView profile_button = (TextView) findViewById(R.id.profile_button);
        if (editMode) {
            profile_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            final ResolveInfo info = ResolveInfoHelper.findForward(resolveInfo);
            if (info == null) {
                profile_button.setVisibility(View.GONE);
            } else {
                resolveInfo = ResolveInfoHelper.filterForward(resolveInfo);

                profile_button.setText(info.loadLabel(getPackageManager()));
                profile_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Activity activity = ChooserActivity.this;

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("bridge/" + activity.getIntent().getType());
                        Uri uri = activity.getIntent().getData();
                        uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileprovider", new File(uri.getPath()));
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent.setComponent(ComponentName.createRelative(info.activityInfo.packageName, info.activityInfo.name));

                        IntentUtils.startOtherActivity(activity, intent);

                        activity.finish();
                    }
                });
            }

            if (resolveInfo.isEmpty()
                    && info != null) {
                profile_button.callOnClick();
            }
        }

        RecyclerView list = (RecyclerView) findViewById(R.id.resolver_list);
        list.setLayoutManager(new GridLayoutManager(this, 4));
        list.setAdapter(new ChooserAdapter(resolveInfo, editMode));
    }
}
