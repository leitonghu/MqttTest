package com.chaohui.im.common.util.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/10/31.
 */
public class Tools {

    public static String getMac(Context mContext) {
        WifiManager wifi = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifi.getConnectionInfo();
        if (info.getMacAddress() != null && info.getMacAddress().length()>11) {
            return info.getMacAddress().toString().trim().replace(":", "");
        } else {
            String mmac= (System.currentTimeMillis()+"").substring(1, (System.currentTimeMillis()+"").length());
            return mmac;
        }
    }

    /**
     * 弹出Toast的提示
     *
     * @param mContext
     * @param resId
     *            string id
     */
    public static final void toast(Context mContext, int resId) {
        if (mContext != null)
            toast(mContext, mContext.getString(resId));
    }

    /**
     * 弹出Toast的提示
     *
     * @param mContext
     * @param resId
     */
    public static final void toast(Context mContext, String resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
    }
}
