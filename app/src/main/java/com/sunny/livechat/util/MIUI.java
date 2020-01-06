package com.sunny.livechat.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;

import static com.sunny.livechat.util.Rom.isIntentAvailable;

/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2020/1/3 16:56
 */
public class MIUI {
    private static final String miui = "ro.miui.ui.version.name";
    private static final String miui5 = "V5";
    private static final String miui6 = "V6";
    private static final String miui7 = "V7";
    private static final String miui8 = "V8";
    private static final String miui9 = "V9";


    public static boolean rom() {
        return Build.MANUFACTURER.equals("Xiaomi");
    }

    private static String getProp() {
        return Rom.getProp(miui);
    }


    public static void req(final Context context) {
        switch (getProp()) {
            case miui5:
                reqForMiui5(context);
                break;
            case miui6:
            case miui7:
                reqForMiui67(context);
                break;
            case miui8:
            case miui9:
                reqForMiui89(context);
                break;
        }

    }


    private static void reqForMiui5(Context context) {
        String packageName = context.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isIntentAvailable(intent, context)) {
            context.startActivity(intent);
        }
    }

    private static void reqForMiui67(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter",
                "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isIntentAvailable(intent, context)) {
            context.startActivity(intent);
        }
    }

    private static void reqForMiui89(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isIntentAvailable(intent, context)) {
            context.startActivity(intent);
        } else {
            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setPackage("com.miui.securitycenter");
            intent.putExtra("extra_pkgname", context.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isIntentAvailable(intent, context)) {
                context.startActivity(intent);
            }
        }
    }


    /**
     * 有些机型在添加TYPE-TOAST类型时会自动改为TYPE_SYSTEM_ALERT，通过此方法可以屏蔽修改
     * 但是...即使成功显示出悬浮窗，移动的话也会崩溃
     */
    private static void addViewToWindow(WindowManager wm, View view, WindowManager.LayoutParams params) {
        setMiUI_International(true);
        wm.addView(view, params);
        setMiUI_International(false);
    }


    private static void setMiUI_International(boolean flag) {
        try {
            Class BuildForMi = Class.forName("miui.os.Build");
            Field isInternational = BuildForMi.getDeclaredField("IS_INTERNATIONAL_BUILD");
            isInternational.setAccessible(true);
            isInternational.setBoolean(null, flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
