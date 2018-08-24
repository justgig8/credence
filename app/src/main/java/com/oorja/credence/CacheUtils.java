package com.oorja.credence;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gaurav on 13/11/17.
 */

public class CacheUtils {

    private static final String DICTIONARY = "info";

    public static void saveCustomerId(Context context, String customerId) {
        SharedPreferences settings = context.getSharedPreferences(DICTIONARY, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("customerId", customerId);
        editor.commit();
    }

    public static String getCustomerId(Context context) {
        SharedPreferences settings = context.getSharedPreferences(DICTIONARY, 0);
        final String customerId = settings.getString("customerId", null);
        return customerId;
    }

    public static boolean getSubscriptionStatus(Context context, String category) {
        String customerId = getCustomerId(context);
        SharedPreferences settings = context.getSharedPreferences(DICTIONARY, 0);
        return settings.getBoolean(customerId + "." + category, false);
    }

    public static void updateSubscriptionStatus(Context context, String category, boolean status) {
        String customerId = getCustomerId(context);
        SharedPreferences settings = context.getSharedPreferences(DICTIONARY, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(customerId + "." + category, status);
        editor.commit();
    }
}
