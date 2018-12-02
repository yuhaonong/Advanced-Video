package io.agora.openduo;

import android.app.Application;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import io.agora.AgoraAPIOnlySignal;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;


public class AGApplication extends Application {
    private final String TAG = AGApplication.class.getSimpleName();

    private static AGApplication mInstance;
    private AgoraAPIOnlySignal mAgoraAPI;
    private RtcEngine mRtcEngine;
    private Handler mMainHandler;

    public static AGApplication the() {
        return mInstance;
    }

    public AGApplication() {
        mInstance = this;
    }

    private OnAgoraEngineInterface onAgoraEngineInterface;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            logAndShowToast("onFirstRemoteVideoDecoded  uid:" + uid);
            if (onAgoraEngineInterface != null) {
                onAgoraEngineInterface.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            }
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            logAndShowToast("onUserOffline uid: " + uid + " reason:" + reason);
            if (onAgoraEngineInterface != null) {
                onAgoraEngineInterface.onUserOffline(uid, reason);
            }
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) {
            logAndShowToast("onUserMuteVideo uid: " + uid + " muted: " + muted);
            if (onAgoraEngineInterface != null) {
                onAgoraEngineInterface.onUserMuteVideo(uid, muted);
            }
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            logAndShowToast("onJoinChannelSuccess channel:" + channel + " uid:" + uid);
            if (onAgoraEngineInterface != null) {
                onAgoraEngineInterface.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();

        mMainHandler = new Handler();
        setupAgoraEngine();
    }

    private void setupAgoraEngine() {
        String appID = getString(R.string.agora_app_id);

        try {
            mAgoraAPI = AgoraAPIOnlySignal.getInstance(this, appID);
            mRtcEngine = RtcEngine.create(getBaseContext(), appID, mRtcEventHandler);
            Log.i(TAG, "setupAgoraEngine mRtcEngine :" + mRtcEngine);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    public void setOnAgoraEngineInterface(OnAgoraEngineInterface onAgoraEngineInterface) {
        this.onAgoraEngineInterface = onAgoraEngineInterface;
    }

    public RtcEngine getmRtcEngine() {
        return mRtcEngine;
    }

    public AgoraAPIOnlySignal getmAgoraAPI() {
        return mAgoraAPI;
    }

    public interface OnAgoraEngineInterface {
        void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed);

        void onUserOffline(int uid, int reason);

        void onUserMuteVideo(final int uid, final boolean muted);

        void onJoinChannelSuccess(String channel, int uid, int elapsed);
    }

    public static void logAndShowToast(final String message) {
        Log.i("AGORA_LOG", message);
        the().mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mInstance, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
