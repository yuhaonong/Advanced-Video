package io.agora;

import android.content.Context;

import io.agora.rtm.RtmClient;

public class AgoraAPIOnlySignal implements IAgoraAPI {
    private static AgoraAPIOnlySignal sInstance = null;
    private RtmClient mRtmClient;

    AgoraAPIOnlySignal() {
    }

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

    }

    @Override
    public void login(String appId, String account, String token, int uid, String deviceID) {

    }

    @Override
    public void logout() {

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
