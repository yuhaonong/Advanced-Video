package io.agora;

import android.content.Context;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.IResultCallback;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmStatusCode;

public class AgoraAPIOnlySignal implements IAgoraAPI {
    private static AgoraAPIOnlySignal sInstance = null;
    private RtmClient mRtmClient;
    private ICallBack mCallback;
    private Object mLock = new Object();

    AgoraAPIOnlySignal() {}

    private AgoraAPIOnlySignal(Context context, String appID) {
        try {
            mRtmClient = RtmClient.createInstance(context, appID, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AgoraAPIOnlySignal getInstance(Context context, String appID) {
        synchronized (AgoraAPIOnlySignal.class) {
            if (sInstance == null) {
                sInstance = new AgoraAPIOnlySignal(context, appID);
            }
        }
        return sInstance;
    }

    @Override
    public void callbackSet(ICallBack handler) {
        synchronized (mLock) {
            mCallback = handler;
        }
    }

    @Override
    public void login(String appId, String account, String token, int uid, String deviceID) {
        if (mRtmClient == null) {
            synchronized (mLock) {
                mCallback.onLoginFailed(RtmStatusCode.LoginError.LOGIN_ERR_REJECTED);
            }
            return;
        }

        mRtmClient.login(token, account, new IResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                synchronized (mLock) {
                    if (mCallback != null) {
                        mCallback.onLoginSuccess();
                    }
                }
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                synchronized (mLock) {
                    if (mCallback != null) {
                        mCallback.onLoginFailed(errorInfo.getErrorCode());
                    }
                }
            }
        });
    }

    @Override
    public void logout() {
        if (mRtmClient == null) {
            return;
        }

        mRtmClient.logout(new IResultCallback() {
            @Override
            public void onSuccess(Object o) {
                synchronized (mCallback) {
                    if (mCallback != null) {
                        mCallback.onLogout(RtmStatusCode.LogoutError.LOGOUT_ERR_OK);
                    }
                }
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                synchronized (mCallback) {
                    if (mCallback != null) {
                        mCallback.onLogout(errorInfo.getErrorCode());
                    }
                }
            }
        });

    }

    @Override
    public void queryUserStatus(String account) {

    }

    @Override
    public void channelInviteUser(String channelID, String account, int uid) {

    }

    @Override
    public void channelInviteAccept(String channelID, String account, int uid, String extra) {

    }

    @Override
    public void channelInviteRefuse(String channelID, String account, int uid, String extra) {

    }

    @Override
    public void channelInviteEnd(String channelID, String account, int uid) {

    }
}
