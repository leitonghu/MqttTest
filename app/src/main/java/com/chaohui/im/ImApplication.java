package com.chaohui.im;

import android.app.Application;

import com.chaohui.im.common.util.PushService;

/**
 * Created by lei on 2016/10/31.
 */
public class ImApplication  extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        PushService.actionStart(this);
    }
}
