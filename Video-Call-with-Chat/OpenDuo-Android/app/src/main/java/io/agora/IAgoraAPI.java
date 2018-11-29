package io.agora;

public interface IAgoraAPI {
    /**
     * signals
     */
    String SIGNAL_PREFIX = "SIG_";
    String SIG_QUERY_USER_STATUS = SIGNAL_PREFIX + "QUERY_USER_STATUS";
    String SIG_CHANNEL_INVITE_USER = SIGNAL_PREFIX + "CHANNEL_INVITE_USER";
    String SIG_CHANNEL_INVITE_ACCEPT = SIGNAL_PREFIX + "CHANNEL_INVITE_ACCEPT";
    String SIG_CHANNEL_INVITE_REFUSE = SIGNAL_PREFIX + "CHANNEL_INVITE_REFUSE";
    String SIG_CHANNEL_INVITE_END = SIGNAL_PREFIX + "CHANNEL_INVITE_END";
    String SIG_CHANNEL_INVITE_USER2 = SIGNAL_PREFIX + "CHANNEL_INVITE_USER2";

    /**
     * set a callback
     * @param handler A handler of ICallBack
     */
    void callbackSet(ICallBack handler);

    /**
     * Log into Agora's Signaling System. Users must always log in before performing any operation.
     * @param appId The App ID provided by Agora
     * @param account User ID defined by the client. It can be up to 64 visible characters (space and "null" is not allowed).
     * @param token
     * @param uid N/A: Set it as 0
     * @param deviceID Set it as NULL
     */
    void login(String appId, String account, String token, int uid, String deviceID);

    /**
     * Log out of the Agora's Signaling System.
     */
    void logout();

    void queryUserStatus(String account);
    void channelInviteUser(String channelID, String account, int uid);
    void channelInviteUser2(String channelID, String account, String extra);
    void channelInviteAccept(String channelID, String account, int uid, String extra);
    void channelInviteRefuse(String channelID, String account, int uid,  String extra);
    void channelInviteEnd(String channelID, String account, int uid);

    interface ICallBack {

        /**
         * triggered when a user is logged into Agora’s signaling system.
         */
        void onLoginSuccess();

        /**
         * triggered when a user has failed to log into Agora’s signaling system.
         * @param err refer to {@link io.agora.rtm.RtmStatusCode.LoginError}
         */
        void onLoginFailed(int err);

        /**
         * triggered when a user is logged out of Agora’s signaling system.
         * @param err refer to {@link io.agora.rtm.RtmStatusCode.LogoutError}
         */
        void onLogout(int err);

        /**
         * An Error has Occurred During SDK Runtime
         * @param name
         * @param ecode
         * @param desc
         */
        @Deprecated
        void onError(String name, int ecode, String desc);

        void onQueryUserStatusResult(String name, String status);
        void onInviteReceived(String channelID, String account, int uid, String extra);
        void onInviteReceivedByPeer(String channelID, String account, int uid);
        void onInviteAcceptedByPeer(String channelID, String account, int uid,  String extra);
        void onInviteRefusedByPeer(String channelID, String account, int uid,  String extra);
        void onInviteEndByMyself(String channelID, String account, int uid);
        void onInviteEndByPeer(String channelID, String account, int uid,  String extra);
        void onInviteFailed(String channelID, String account, int uid, int ecode,  String extra);
        void onReconnecting(int nretry);
        void onReconnected(int fd);
    }

}
