package io.agora;


import android.content.Context;

public final class AgoraAPI extends AgoraAPIOnlySignal {

    private AgoraAPI() {}

    public static AgoraAPIOnlySignal getInstance(Context context, String appId) {
        return AgoraAPIOnlySignal.getInstance(context, appId);
    }

    public static class CallBack implements ICallBack {

        @Override
        public void onLoginSuccess() {

        }

        @Override
        public void onLoginFailed(int ecode) {

        }

        @Override
        public void onLogout(int ecode) {

        }

        @Override
        public void onQueryUserStatusResult(String name, String status) {

        }

        @Override
        public void onError(String name, int ecode, String desc) {

        }

        @Override
        public void onInviteReceived(String channelID, String account, int uid, String extra) {

        }

        @Override
        public void onInviteReceivedByPeer(String channelID, String account, int uid) {

        }

        @Override
        public void onInviteAcceptedByPeer(String channelID, String account, int uid, String extra) {

        }

        @Override
        public void onInviteRefusedByPeer(String channelID, String account, int uid, String extra) {

        }

        @Override
        public void onInviteEndByMyself(String channelID, String account, int uid) {

        }

        @Override
        public void onInviteEndByPeer(String channelID, String account, int uid, String extra) {

        }

        @Override
        public void onInviteFailed(String channelID, String account, int uid, int ecode, String extra) {

        }

        @Override
        public void onReconnecting(int nretry) {

        }

        @Override
        public void onReconnected(int fd) {

        }

        @Override
        public void onConnectionAborted() {

        }
    }
}
