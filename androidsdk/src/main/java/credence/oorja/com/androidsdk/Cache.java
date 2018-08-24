package credence.oorja.com.androidsdk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gaurav on 13/11/17.
 */

public class Cache {

    private static final String DICTIONARY = "credinfo";

    public static void delete(Context context, String key){
        SharedPreferences settings = context.getSharedPreferences(DICTIONARY, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void save(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(DICTIONARY, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String get(Context context, String key) {
        return get(context, key, null);
    }

    public static String get(Context context, String key, String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(DICTIONARY, 0);
        final String value = settings.getString(key, defaultValue);
        return value;
    }
}
