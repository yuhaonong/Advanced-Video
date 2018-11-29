package io.agora;

public interface IAgoraAPI {
    interface ICallBack {
        void onLoginSuccess();
        void onLoginFailed(int ecode);
        void onLogout(int ecode);
        void onQueryUserStatusResult(String name,String status);
        void onError(String name,int ecode,String desc);
        void onInviteReceived(String channelID,String account,int uid,String extra);
        void onInviteReceivedByPeer(String channelID,String account,int uid);
        void onInviteAcceptedByPeer(String channelID,String account,int uid, String extra);
        void onInviteRefusedByPeer(String channelID,String account,int uid, String extra);
        void onInviteEndByMyself(String channelID,String account,int uid);
        void onInviteEndByPeer(String channelID,String account,int uid, String extra);
        void onInviteFailed(String channelID,String account,int uid,int ecode, String extra);
    }

    void callbackSet(ICallBack handler);
    void login(String appId, String account, String token, int uid, String deviceID);
    void logout();
    void queryUserStatus(String account);
    void channelInviteUser(String channelID,String account,int uid);
    void channelInviteAccept(String channelID,String account,int uid,String extra);
    void channelInviteRefuse(String channelID,String account,int uid, String extra);
    void channelInviteEnd(String channelID,String account,int uid);
}
