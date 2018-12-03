package io.agora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import io.agora.AgoraAPI;
import io.agora.AgoraAPIOnlySignal;
import io.agora.openduo.AGApplication;
import io.agora.openduo.R;
import io.agora.utils.Constant;

/**
 * "*" and "#" is useless
 *
 */
public class NumberCallActivity extends AppCompatActivity {
    private final String TAG = NumberCallActivity.class.getSimpleName();

    private final int REQUEST_CODE = 0x01;

    private AgoraAPIOnlySignal mAgoraAPI;
    private String mMyAccount;
    private String mSubscriber;
    private TextView mCallTitle;
    private EditText mSubscriberPhoneNumberText;
    private StringBuffer mCallNumberText = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        Intent intent = getIntent();
        if (intent != null) {
            mMyAccount = intent.getStringExtra("account");
        }
        mCallTitle = (TextView) findViewById(R.id.meet_title);
        mCallTitle.setText(String.format(Locale.US, "Your account ID is %s", mMyAccount));
        mSubscriberPhoneNumberText = (EditText) findViewById(R.id.call_number_edit);

        mAgoraAPI = AGApplication.the().getmAgoraAPI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCallback();
    }

    public void CallClickInit(View v) {
        switch (v.getId()) {
            case R.id.call_number_delete:
                if (mCallNumberText.length() > 0) {
                    mCallNumberText.delete(mCallNumberText.length() - 1, mCallNumberText.length());
                    mSubscriberPhoneNumberText.setText(mCallNumberText);
                    mSubscriberPhoneNumberText.setSelection(mSubscriberPhoneNumberText.getText().toString().length());
                }
                break;

            case R.id.call_number_call: // number layout call out button
                if (!Constant.isFastlyClick()) {
                    String subscriber = mSubscriberPhoneNumberText.getText().toString();
                    if (mMyAccount.equals(subscriber)) {
                        AGApplication.logAndShowToast("could not call yourself");
                    } else {
                        mSubscriber = subscriber;
                        mAgoraAPI.queryUserStatus(mSubscriber);
                        AGApplication.logAndShowToast("caller: queryUserStatus " + mSubscriber);
                    }
                } else {
                    AGApplication.logAndShowToast("fast click");
                }
                Log.i(TAG, "call number call init");
                break;
        }
    }

    /**
     * number click button
     *
     * @param v
     */
    public void numberClick(View v) {
        mCallNumberText.append(((Button) v).getText().toString());
        mSubscriberPhoneNumberText.setText(mCallNumberText);
        mSubscriberPhoneNumberText.setSelection(mSubscriberPhoneNumberText.getText().toString().length());
    }

    private void setCallback() {
        if (mAgoraAPI == null) {
            return;
        }

        mAgoraAPI.callbackSet(new AgoraAPI.CallBack() {

            @Override
            public void onLoginFailed(int ecode) {
                AGApplication.logAndShowToast("onLoginFailed code:" + ecode);
            }

            @Override
            public void onConnectionAborted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        // logout() will be called on MainActivity resume
                    }
                });
            }

            @Override
            public void onInviteReceived(final String channelID, final String account, int uid,
                                         String extra) { //call out other remote receiver
                AGApplication.logAndShowToast("callee: onInviteReceived channel:" + channelID + " account:" + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(NumberCallActivity.this, CallActivity.class);
                        intent.putExtra("account", mMyAccount);
                        intent.putExtra("channelID", channelID);
                        intent.putExtra("subscriber", account);
                        intent.putExtra("type", Constant.CALL_IN);
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                });
            }

            @Override
            public void onInviteReceivedByPeer(final String channelID, final String account, int uid) {
                AGApplication.logAndShowToast("caller: onInviteReceivedByPeer channel:" + channelID + " account:" + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(NumberCallActivity.this, CallActivity.class);
                        intent.putExtra("account", mMyAccount);
                        intent.putExtra("channelID", channelID);
                        intent.putExtra("subscriber", account);
                        intent.putExtra("type", Constant.CALL_OUT);
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                });
            }

            @Override
            public void onInviteFailed(String channelID, String account, int uid, int ecode, String extra) {
                AGApplication.logAndShowToast("caller: onInviteFailed channel:" + channelID + " account:" + account
                        + " extra: " + extra + " ecode: " + ecode);
            }

            @Override
            public void onQueryUserStatusResult(final String name, final String status) {
                boolean isOnline = "1".equals(status);
                AGApplication.logAndShowToast("caller: onQueryUserStatusResult name:" + name + " status:" + (isOnline ? "online" : "offline"));
                if (isOnline) {
                    String channelID = mMyAccount + mSubscriber;
                    mAgoraAPI.channelInviteUser(channelID, mSubscriber, 0);
                    AGApplication.logAndShowToast("caller: channelInviteUser channel:" + channelID + " subscriber:" + mSubscriber);
                }
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getStringExtra("result").equals("finish")) {
                finish();
            }
        }
    }

}