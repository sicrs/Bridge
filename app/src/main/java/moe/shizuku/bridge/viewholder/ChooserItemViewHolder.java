package moe.shizuku.bridge.viewholder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import moe.shizuku.bridge.BridgeSettings;
import moe.shizuku.bridge.ChooserActivity;
import moe.shizuku.bridge.R;
import moe.shizuku.bridge.utils.IntentUtils;
import moe.shizuku.support.recyclerview.BaseViewHolder;
import moe.shizuku.support.utils.ContextUtils;

/**
 * Created by Rikka on 2017/4/6.
 */

public class ChooserItemViewHolder extends BaseViewHolder<ResolveInfo> {

    public static Creator<ResolveInfo> newCreator(final boolean editMode) {

        return new Creator<ResolveInfo>() {
            @Override
            public BaseViewHolder<ResolveInfo> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
                return new ChooserItemViewHolder(inflater.inflate(R.layout.resolve_grid_item, parent, false), editMode);
            }
        };
    }

    private static final Object CHECK_SELECT_PAYLOAD = new Object();

    private ImageView icon;
    private ImageView target_badge;
    private TextView title;

    private boolean mEditMode;

    public ChooserItemViewHolder(View itemView, boolean editMode) {
        super(itemView);

        mEditMode = editMode;

        icon = itemView.findViewById(android.R.id.icon);
        target_badge = itemView.findViewById(R.id.target_badge);
        title = itemView.findViewById(android.R.id.text1);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditMode) {
                    BridgeSettings.setActivityForward(getData().activityInfo.name, !BridgeSettings.isActivityForward(getData().activityInfo.name));

                    getAdapter().notifyItemChanged(getAdapterPosition(), CHECK_SELECT_PAYLOAD);
                } else {
                    Activity activity = ContextUtils.getActivity(v.getContext());
                    if (activity == null) {
                        return;
                    }

                    Intent intent = new Intent(activity.getIntent());
                    intent.setComponent(ComponentName.createRelative(getData().activityInfo.packageName, getData().activityInfo.name));
                    intent.removeExtra(ChooserActivity.EXTRA_RESOLVE_INFO);
                    IntentUtils.startOtherActivity(v.getContext(), intent);

                    activity.finish();
                }
            }
        });

        if (!editMode) {
            icon.setStateListAnimator(null);
            title.setStateListAnimator(null);
        }
    }

    @Override
    public void onBind() {
        super.onBind();

        if (getData() == null) {
            icon.setImageDrawable(null);
            title.setText(null);
            return;
        }

        if (mEditMode) {
            checkSelected();
        }

        PackageManager pm = itemView.getContext().getPackageManager();

        icon.setImageDrawable(getData().loadIcon(pm));
        title.setText(getData().loadLabel(pm));
    }

    @Override
    public void onBind(@NonNull List<Object> payloads) {
        super.onBind(payloads);

        for (Object payload: payloads) {
            if (CHECK_SELECT_PAYLOAD.equals(payload)) {
                checkSelected();
            }
        }
    }

    private static WeakReference<ColorMatrixColorFilter> filter = new WeakReference<>(null);

    private void checkSelected() {
        boolean selected = BridgeSettings.isActivityForward(getData().activityInfo.name);
        itemView.setSelected(selected);

        if (!selected) {
            if (filter.get() == null) {
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0);
                ColorMatrixColorFilter grayColorFilter = new ColorMatrixColorFilter(cm);
                filter = new WeakReference<>(grayColorFilter);
            }

            icon.setColorFilter(filter.get());
        } else {
            icon.setColorFilter(null);
        }
    }
}
