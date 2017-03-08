package example.com.lookweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 本地存储工具 <功能详细描述>
 */
public class SPUtil {
    private SharedPreferences sp;
    private Editor edit;
    private static SPUtil spu;

    public static SPUtil getInstance(Context c) {
        if (spu == null) {
            spu = new SPUtil();
            spu.sp = c.getSharedPreferences("rfb", 0);
            spu.edit = spu.sp.edit();
        }
        return spu;
    }

    private SPUtil() {
    }

    public String getStringValue(String key) {
        return sp.getString(key, null);
    }

    public long getLongValue(String key) {
        return sp.getLong(key, 0);
    }

    public int getIntValue(String key) {
        return sp.getInt(key, 0);
    }

    public boolean getBooleanValue(String key) {
        return sp.getBoolean(key, true);
    }

    public void putValue(String key, String value) {
        edit.putString(key, value);
        edit.commit();
    }

    public void putValue(String key, long value) {
        edit.putLong(key, value);
        edit.commit();
    }

    public void putValue(String key, int value) {
        edit.putInt(key, value);
        edit.commit();
    }

    public void putValue(String key, boolean value) {
        edit.putBoolean(key, value);
        edit.commit();
    }


    //清除里面的数据
    public boolean clearSP() {
        edit.clear();
        edit.commit();
        return true;
    }

}
