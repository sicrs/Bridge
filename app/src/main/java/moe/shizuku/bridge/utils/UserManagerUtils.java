package moe.shizuku.bridge.utils;

import android.content.Context;
import android.os.UserManager;

/**
 * Created by Rikka on 2017/4/14.
 */

public class UserManagerUtils {

    public static boolean isUsingWorkProfile(Context context) {
        UserManager um = context.getSystemService(UserManager.class);
        return um != null && um.getUserProfiles().size() > 1;
    }
}
