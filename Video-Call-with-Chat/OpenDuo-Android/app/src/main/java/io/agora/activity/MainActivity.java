package io.agora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import io.agora.AgoraAPI;
import io.agora.openduo.AGApplication;
import io.agora.openduo.R;
import io.agora.rtc.RtcEngine;
import io.agora.rtm.RtmStatusCode;

/**
 * @author Luke Yu
 * app account must be number
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    private EditText textAccountName;
    private String appId;
    private int uid;
    private String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appId = getString(R.string.agora_app_id);
        textAccountName = (EditText) findViewById(R.id.account_name);
        textAccountName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = s.toString().isEmpty();
                findViewById(R.id.button_login).setEnabled(!isEmpty);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCallback();
    }

    // login signaling
    public void onClickLogin(View v) {
        Log.i(TAG, "onClickLogin");
        account = textAccountName.getText().toString().trim();
        AGApplication.the().getmAgoraAPI().login(appId, account, appId, 0, "");
    }

    private void setCallback() {
        Log.i(TAG, "setCallback enter.");
        AGApplication.the().getmAgoraAPI().callbackSet(new AgoraAPI.CallBack() {

            @Override
            public void onLoginSuccess() {
                Log.i(TAG, "onLoginSuccess");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, NumberCallActivity.class);
                        intent.putExtra("uid", uid);
                        intent.putExtra("account", account);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onLogout(int i) {
                Log.i(TAG, "onLogout  i = " + i);
            }

            @Override
            public void onLoginFailed(final int i) {
                Log.i(TAG, "onLoginFailed " + i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i == RtmStatusCode.LoginError.LOGIN_ERR_REJECTED) {
                            Toast.makeText(MainActivity.this, "Login rejected", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Login error: " + i, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        RtcEngine.destroy();
    }
}
