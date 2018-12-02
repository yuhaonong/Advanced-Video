package io.agora.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import io.agora.AgoraAPI;
import io.agora.AgoraAPIOnlySignal;
import io.agora.openduo.AGApplication;
import io.agora.openduo.R;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.utils.Constant;

/**
 * Created by beryl on 2017/11/6.
 *
 */
public class CallActivity extends AppCompatActivity implements AGApplication.OnAgoraEngineInterface {
    private final String TAG = CallActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private static final int PERMISSION_REQ_ID_STORAGE = PERMISSION_REQ_ID_CAMERA + 1;

    private AgoraAPIOnlySignal mAgoraAPI;
    private RtcEngine mRtcEngine;

    private String mSubscriber;

    private CheckBox mCheckMute;
    private TextView mCallTitle;
    private ImageView mCallHangupBtn;
    private RelativeLayout mLayoutCallIn;

    private FrameLayout mLayoutBigView;
    private FrameLayout mLayoutSmallView;

    private String channelID = "channelid";
    private MediaPlayer mPlayer;
    private int callType = -1;
    private boolean mIsCallInRefuse = false;
    private int mRemoteUid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        mCallTitle = (TextView) findViewById(R.id.meet_title);
        mCheckMute = (CheckBox) findViewById(R.id.call_mute_button);
        mCheckMute.setOnCheckedChangeListener(oncheckChangeListerener);
        mCallHangupBtn = (ImageView) findViewById(R.id.call_button_hangup);
        mLayoutCallIn = (RelativeLayout) findViewById(R.id.call_layout_callin);
        mLayoutBigView = (FrameLayout) findViewById(R.id.remote_video_view_container);
        mLayoutSmallView = (FrameLayout) findViewById(R.id.local_video_view_container);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
                && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_STORAGE)) {
            initAgoraEngineAndJoinChannel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSignalingCallback();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed callType: " + callType + " mIsCallInRefuse: " + mIsCallInRefuse);
        if (callType == Constant.CALL_IN && mIsCallInRefuse) {
            callInRefuse();
        } else {
            callOutHangup();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }

        if (mRtcEngine != null) {
            mRtcEngine.stopPreview();
            mRtcEngine.leaveChannel();
        }
        mRtcEngine = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent");
        setupData();
    }

    private CompoundButton.OnCheckedChangeListener oncheckChangeListerener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mRtcEngine.muteLocalAudioStream(isChecked);
                }
            };

    public void CallClickInit(View v) {
        switch (v.getId()) {

            case R.id.call_in_hangup:
                callInRefuse();
                break;

            case R.id.call_in_pickup:
                mIsCallInRefuse = false;
                joinChannel(); // Tutorial Step 4
                AGApplication.logAndShowToast("callee: channelInviteAccept channelID:" + channelID + " mSubscriber:" + mSubscriber);
                mAgoraAPI.channelInviteAccept(channelID, mSubscriber, 0, null);
                mLayoutCallIn.setVisibility(View.GONE);
                mCallHangupBtn.setVisibility(View.VISIBLE);
                mCallTitle.setVisibility(View.GONE);
                if (mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.stop();
                }
                break;

            case R.id.call_button_hangup: // call out canceled or call ended
                callOutHangup();
                break;
        }
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();
        AGApplication.the().setOnAgoraEngineInterface(this);
        setupData();
    }

    private void setupData() {
        Intent intent = getIntent();
        mSubscriber = intent.getStringExtra("subscriber");
        channelID = intent.getStringExtra("channelID");
        callType = intent.getIntExtra("type", -1);

        if (callType == Constant.CALL_IN) {
            mIsCallInRefuse = true;
            mLayoutCallIn.setVisibility(View.VISIBLE);
            mCallHangupBtn.setVisibility(View.GONE);
            mCallTitle.setText(String.format(Locale.US, "%s is calling...", mSubscriber));

            try {
                mPlayer = MediaPlayer.create(this, R.raw.basic_ring);
                mPlayer.setLooping(true);
                mPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setupLocalVideo(); // Tutorial Step 3
        } else if (callType == Constant.CALL_OUT) {
            mLayoutCallIn.setVisibility(View.GONE);
            mCallHangupBtn.setVisibility(View.VISIBLE);
            mCallTitle.setText(String.format(Locale.US, "%s is be called...", mSubscriber));

            try {
                mPlayer = MediaPlayer.create(this, R.raw.basic_tones);
                mPlayer.setLooping(true);
                mPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            setupLocalVideo();
            joinChannel();
        }
    }

    private void setSignalingCallback() {
        if (mAgoraAPI == null) {
            return;
        }
        mAgoraAPI.callbackSet(new AgoraAPI.CallBack() {

            @Override
            public void onLogout(final int ecode) {
                AGApplication.logAndShowToast("onLogout code:" + ecode);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.putExtra("result", "finish");
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });

            }

            @Override
            public void onInviteReceived(final String channelID, final String account, final int uid, String extra) {
                AGApplication.logAndShowToast("callee: onInviteReceived channel:" + channelID + " account:" + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // refuse since already in call
                        mAgoraAPI.channelInviteRefuse(channelID, account, uid, "{\"status\":1}");
                    }
                });
            }

            @Override
            public void onInviteReceivedByPeer(final String channelID, String account, int uid) {
                AGApplication.logAndShowToast("caller: onInviteReceivedByPeer channel:" + channelID + "  account:" + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCallHangupBtn.setVisibility(View.VISIBLE);
                        mCallTitle.setText(String.format(Locale.US, "%s is being called ...", mSubscriber));
                    }
                });
            }

            @Override
            public void onInviteAcceptedByPeer(String channelID, String account, int uid, String extra) {
                AGApplication.logAndShowToast("caller: onInviteAcceptedByPeer channel:" + channelID + "  account:" + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null && mPlayer.isPlaying()) {
                            mPlayer.stop();
                        }
                        mCallTitle.setVisibility(View.GONE);
                    }
                });

            }

            @Override
            public void onInviteRefusedByPeer(String channelID, final String account, int uid, final String extra) {
                AGApplication.logAndShowToast("caller: onInviteRefusedByPeer channel:" + channelID + " account:" + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null && mPlayer.isPlaying()) {
                            mPlayer.stop();
                        }
                        onEncCallClicked();
                    }
                });
            }


            @Override
            public void onInviteEndByPeer(final String channelID, String account, int uid, String extra) {
                AGApplication.logAndShowToast("onInviteEndByPeer channel:" + channelID + " account:" + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (channelID.equals(CallActivity.this.channelID)) {
                            onEncCallClicked();
                        }

                    }
                });
            }

            @Override
            public void onInviteEndByMyself(String channelID, String account, int uid) {
                AGApplication.logAndShowToast("onInviteEndByMyself channel:" + channelID + " account:" + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onEncCallClicked();
                    }
                });
            }
        });
    }

    private void callOutHangup() {
        if (mAgoraAPI != null) {
            AGApplication.logAndShowToast("channelInviteEnd channel:" + channelID + " mSubscriber:" + mSubscriber);
            mAgoraAPI.channelInviteEnd(channelID, mSubscriber, 0);
        }
    }

    private void callInRefuse() {
        // "status": 0 // Default
        // "status": 1 // Busy
        if (mAgoraAPI != null) {
            AGApplication.logAndShowToast("callee: channelInviteRefuse channel:" + channelID + " mSubscriber:" + mSubscriber);
            mAgoraAPI.channelInviteRefuse(channelID, mSubscriber, 0, "{\"status\":0}");
        }
        onEncCallClicked();
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRemoteUid != 0) {
                    return;
                }
                mRemoteUid = uid;
                setupRemoteVideo(uid);
            }
        });
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRemoteUserLeft(uid);
            }
        });
    }

    @Override
    public void onUserMuteVideo(final int uid, final boolean muted) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRemoteUserVideoMuted(uid, muted);
            }
        });
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        AGApplication.logAndShowToast("onJoinChannelSuccess channel:" + channel + " uid:" + uid);
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        mAgoraAPI = AGApplication.the().getmAgoraAPI();
        mRtcEngine = AGApplication.the().getmRtcEngine();
        Log.i(TAG, "initializeAgoraEngine mRtcEngine: " + mRtcEngine);
        if (mRtcEngine != null) {
            mRtcEngine.setLogFile("/sdcard/sdklog.txt");
        }
        setupVideoProfile();
    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
    }

    // Tutorial Step 3
    private void setupLocalVideo() {
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        mLayoutBigView.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        mLayoutBigView.setVisibility(View.VISIBLE);
        int ret = mRtcEngine.startPreview();
        Log.i(TAG, "setupLocalVideo startPreview ret: " + ret);
    }

    // Tutorial Step 4
    private void joinChannel() {
        int ret = mRtcEngine.joinChannel(null, channelID, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
        Log.i(TAG, "joinChannel ret: " + ret);
    }

    // Tutorial Step 5
    private void setupRemoteVideo(int uid) {
        Log.i(TAG, "setupRemoteVideo uid: " + uid + " " + mLayoutBigView.getChildCount());
        if (mLayoutBigView.getChildCount() >= 1) {
            mLayoutBigView.removeAllViews();
        }

        SurfaceView surfaceViewSmall = RtcEngine.CreateRendererView(getBaseContext());
        surfaceViewSmall.setZOrderMediaOverlay(true);
        mLayoutSmallView.addView(surfaceViewSmall);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceViewSmall, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        mLayoutSmallView.setVisibility(View.VISIBLE);

        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        mLayoutBigView.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        mLayoutBigView.setVisibility(View.VISIBLE);
    }

    // Tutorial Step 6
    public void onEncCallClicked() {
        finish();
    }

    // Tutorial Step 7
    private void onRemoteUserLeft(int uid) {
        if (uid == mRemoteUid) {
            finish();
        }
    }

    // Tutorial Step 8
    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    // Tutorial Step 10
    private void onRemoteUserVideoMuted(int uid, boolean muted) {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
        Object tag = surfaceView.getTag();
        if (tag != null && (Integer) tag == uid) {
            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }


    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    onEncCallClicked();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_STORAGE);
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA);
                    onEncCallClicked();
                }
                break;
            }
            case PERMISSION_REQ_ID_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    onEncCallClicked();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}