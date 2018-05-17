package moe.shizuku.bridge;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Switch;

import moe.shizuku.bridge.service.FileSaveService;
import moe.shizuku.bridge.utils.IntentUtils;
import moe.shizuku.bridge.utils.PackageManagerUtils;
import moe.shizuku.bridge.utils.UserManagerUtils;
import moe.shizuku.support.utils.Settings;

public class MainActivity extends Activity {

    public static final ComponentName SAVE_ACTIVITY = ComponentName.createRelative(BuildConfig.APPLICATION_ID, ".SaveActivity");
    public static final ComponentName FORWARD_ACTIVITY = ComponentName.createRelative(BuildConfig.APPLICATION_ID, ".ForwardingActivity");

    private static final int REQUEST_PERMISSION_SAVE_TO_INTERNAL = 10000;
    private static final int REQUEST_PERMISSION_CACHE_TO_PUBLIC = 10001;

    private Switch mSwitchSave;
    private Switch mSwitchForward;
    private Switch mSwitchForwardToCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            PackageManagerUtils.setComponentState(getPackageManager(), SAVE_ACTIVITY, false);
        }

        mSwitchSave = findViewById(R.id.switch_save);
        mSwitchSave.setChecked(PackageManagerUtils.isComponentEnabled(getPackageManager(), SAVE_ACTIVITY));
        mSwitchSave.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_SAVE_TO_INTERNAL);
                return;
            }
            PackageManagerUtils.setComponentState(getPackageManager(), SAVE_ACTIVITY, mSwitchSave.isChecked());
        });

        mSwitchForward = findViewById(R.id.switch_forward);
        mSwitchForward.setChecked(PackageManagerUtils.isComponentEnabled(getPackageManager(), FORWARD_ACTIVITY));
        mSwitchForward.setOnClickListener(v -> PackageManagerUtils.setComponentState(getPackageManager(), FORWARD_ACTIVITY, mSwitchForward.isChecked()));

        mSwitchForwardToCache = findViewById(R.id.switch_save_to_public);
        mSwitchForwardToCache.setChecked(Settings.getBoolean("cache_to_public", false));
        mSwitchForwardToCache.setOnClickListener(v -> {
            boolean newValue = !Settings.getBoolean("cache_to_public", false);
            if (newValue) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CACHE_TO_PUBLIC);
                    return;
                }
            } else {
                FileSaveService.startClearCache(this);
            }
            Settings.putBoolean("cache_to_public", newValue);
        });

        findViewById(R.id.select_forward_apps).setOnClickListener(v -> ChooserActivity.start(v.getContext()));

        if (!UserManagerUtils.isUsingWorkProfile(this)) {
            findViewById(R.id.help_container).setVisibility(View.GONE);
        }

        //noinspection ConstantConditions
        if ("free".equals(BuildConfig.FLAVOR)) {
            findViewById(android.R.id.button1).setOnClickListener(v -> IntentUtils.startOtherActivity(v.getContext(), new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.bridge"))));

            findViewById(android.R.id.button2).setOnClickListener(v -> IntentUtils.startOtherActivity(v.getContext(), new Intent(Intent.ACTION_VIEW,
                    Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode=https%3A%2F%2Fqr.alipay.com%2Faex01083scje5axcttivf13"))));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_SAVE_TO_INTERNAL:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PackageManagerUtils.setComponentState(getPackageManager(), SAVE_ACTIVITY, true);
                } else {
                    mSwitchSave.setChecked(false);
                }
                break;
            case REQUEST_PERMISSION_CACHE_TO_PUBLIC:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Settings.getBoolean("cache_to_public", true);
                } else {
                    mSwitchForwardToCache.setChecked(false);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
