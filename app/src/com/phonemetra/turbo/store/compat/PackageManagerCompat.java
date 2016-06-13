package com.phonemetra.turbo.store.compat;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.util.Log;

import com.phonemetra.turbo.store.Utils;

public class PackageManagerCompat extends Compatibility {

    private static final String TAG = "PackageManagerCompat";

    @TargetApi(11)
    public static void setInstaller(PackageManager mPm, String packageName) {
        if (!hasApi(11)) return;
        try {
            mPm.setInstallerPackageName(packageName, "com.phonemetra.turbo.store");
            Utils.DebugLog(TAG, "Installer package name for " + packageName + " set successfully");
        } catch (Exception e) {
            // Many problems can occur:
            //  * App wasn't installed due to incompatibility
            //  * User canceled install
            //  * Another app interfered in the process
            //  * Another app already set the target's installer package
            //  * ...
            Log.e(TAG, "Could not set installer package name for " +
                    packageName, e);
        }
    }

}
