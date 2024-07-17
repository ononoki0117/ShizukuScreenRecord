package com.gsclab.shizukuscreenrecord.util;

import android.content.pm.PackageManager;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuRemoteProcess;

public class ShizukuUtil {
    public static ShizukuRemoteProcess mProcess = null;

    public static boolean checkPermission(int code) {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            return false;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Granted
            return true;
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
            return false;
        } else {
            // Request the permission
            Shizuku.requestPermission(code);
            return false;
        }
    }
}
