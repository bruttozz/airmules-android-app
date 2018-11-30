package com.example.asthana.airmuleschat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WeChatAppRegister extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WXAPIFactory.createWXAPI(context, WeChat.APP_ID).registerApp(WeChat.APP_ID);
    }
}
