package com.sunny.livechat.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2020/1/3 16:59
 */
public class Rom {
    static boolean isIntentAvailable(Intent intent, Context context) {
        return intent != null && context.getPackageManager().queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }


    static String getProp(String name) {
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop  " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            String line = input.readLine();
            input.close();
            return line;
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
