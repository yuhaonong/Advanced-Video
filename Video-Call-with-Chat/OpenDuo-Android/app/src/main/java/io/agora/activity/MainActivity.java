package io.agora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import io.agora.AgoraAPI;
import io.agora.AgoraAPIOnlySignal;
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

    private EditText mTextAccountName;
    private String mAppId;
    private String mAccount;
    private boolean mIsLogin = false;
    private AgoraAPIOnlySignal mAgoraAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppId = getString(R.string.agora_app_id);
        mTextAccountName = (EditText) findViewById(R.id.account_name);
        mTextAccountName.addTextChangedListener(new TextWatcher() {
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
        mAgoraAPI = AGApplication.the().getmAgoraAPI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSignalingCallback();
        if (mIsLogin) {
            mAgoraAPI.logout();
            mIsLogin = false;
        }
    }

    // login signaling
    public void onClickLogin(View v) {
        Log.i(TAG, "onClickLogin");
        mAccount = mTextAccountName.getText().toString().trim();
        mAgoraAPI.login(mAppId, mAccount, "", 0, "");
        mIsLogin = true;
    }

    private void setSignalingCallback() {
        Log.i(TAG, "setSignalingCallback enter.");
        mAgoraAPI.callbackSet(new AgoraAPI.CallBack() {

            @Override
            public void onLoginSuccess() {
                AGApplication.logAndShowToast("onLoginSuccess");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, NumberCallActivity.class);
                        intent.putExtra("account", mAccount);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onLogout(final int ecode) {
                AGApplication.logAndShowToast("onLogout code:" + ecode);
            }

            @Override
            public void onLoginFailed(final int ecode) {
                AGApplication.logAndShowToast(ecode == RtmStatusCode.LoginError.LOGIN_ERR_REJECTED ?
                                "Login rejected" : "Login error:" + ecode);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mIsLogin) {
            mAgoraAPI.logout();
            mIsLogin = false;
        }
        RtcEngine.destroy();
    }
}
