package com.example.asthana.airmuleschat.wxapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.annimon.stream.Optional;
import com.example.asthana.airmuleschat.WeChat;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;


public class WeChatLoginActivity extends AppCompatActivity {

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Optional.ofNullable(intent).ifPresent(from -> handleBroadcast(from));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver();

        SendAuth.Req req = new SendAuth.Req();
        req.scope = WeChat.AUTH_SCOPE;
        req.state = WeChat.STATE;
        WXAPIFactory.createWXAPI(this, WeChat.APP_ID, false).sendReq(req);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }

    private void registerReceiver() {
        registerReceiver(receiver, new IntentFilter(WeChat.WE_CHAT_AUTH_RESULT));
    }

    private void unregisterReceiver() {
        try {
            unregisterReceiver(receiver);
        } catch (Exception ignore) {
        }
    }

    private void handleBroadcast(@NonNull Intent from) {
        final String action = from.getAction();
        if (WeChat.WE_CHAT_AUTH_RESULT.equalsIgnoreCase(action)) {
            Intent result = new Intent();
            result.putExtra(WeChat.WE_CHAT_AUTH_CODE, from.getStringExtra(WeChat.WE_CHAT_AUTH_CODE));
            result.putExtra(WeChat.WE_CHAT_ERROR_CODE, from.getStringExtra(WeChat.WE_CHAT_ERROR_CODE));
            setResult(RESULT_OK, result);
            finish();
        }
    }
}
