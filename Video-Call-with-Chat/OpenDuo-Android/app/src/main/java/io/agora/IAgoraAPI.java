package io.agora;

public interface IAgoraAPI {

    /**
     * Set a callback.
     */
    void callbackSet(ICallBack handler);

    /**
     * Log into Agora's Signaling System. Users must always log in before performing any operation.
     * @param appId The App ID provided by Agora.
     * @param account User ID defined by the client. It can be up to 64 visible characters
     *               (space and "null" is not allowed).
     * @param token
     * @param uid N/A: Set it as 0.
     * @param deviceID Set it as NULL.
     */
    void login(String appId, String account, String token, int uid, String deviceID);

    /**
     * Log out of the Agora's Signaling System.
     */
    void logout();

    /**
     * Checks whether a user is online and triggers the onQueryUserStatusResult callback function.
     * when called successfully.
     * @param account Account of the user.
     */
    void queryUserStatus(String account);

    /**
     * Initiates a call, that is, invites a user to join a specified channel.
     * @param channelID Channel name. It can be up to 128 visible characters.
     * @param account User ID of the other user defined by the client.
     * @param uid N/A. Set as 0.
     */
    void channelInviteUser(String channelID, String account, int uid);

    /**
     * Same as channelInviteUser except for the parameters.
     * @param channelID Channel name: It can be up to 128 visible characters.
     * @param account User ID of the other user defined by the client.
     * @param extra N/A. Set it as NULL.
     */
    void channelInviteUser2(String channelID, String account, String extra);

    /**
     * Accepts the received call request or invitation. Once this method is called, the other user
     * will receive the onInviteAcceptedByPeer callback function.
     * @param channelID Channel name. It can be up to 128 visible characters.
     * @param account The caller’s user ID.
     * @param uid N/A. Set as 0.
     * @param extra N/A. Set it as NULL.
     */
    void channelInviteAccept(String channelID, String account, int uid, String extra);

    /**
     * Rejects a call invitation or request. Once this method is called, the other user receives
     * the onInviteRefusedByPeer callback function.
     * @param channelID Channel name. It can be up to 128 visible characters.
     * @param account User ID of the other user defined by the client.
     * @param uid N/A: Set as 0.
     * @param extra N/A. Set it as NULL.
     */
    void channelInviteRefuse(String channelID, String account, int uid,  String extra);

    /**
     * Ends a call after the call has been connected. The user receives the onInviteEndByMyself
     * callback function, and the other user receives the onInviteEndByPeer callback function.
     * @param channelID Channel name. It can be up to 128 visible characters.
     * @param account User ID of the other user defined by the client.
     * @param uid N/A. Set as 0.
     */
    void channelInviteEnd(String channelID, String account, int uid);


    interface ICallBack {

        /**
         * Triggered when a user is logged into Agora’s signaling system.
         */
        void onLoginSuccess();

        /**
         * Triggered when a user has failed to log into Agora’s signaling system.
         * @param err refer to {@link io.agora.rtm.RtmStatusCode.LoginError}
         */
        void onLoginFailed(int err);

        /**
         * Triggered when a user is logged out of Agora’s signaling system.
         * @param err Generally unused. Refer to {@link io.agora.rtm.RtmStatusCode.LogoutError}.
         */
        void onLogout(int err);

        @Deprecated
        void onError(String name, int ecode, String desc);

        /**
         * Returns the query result of the user status and is triggered when queryUserStatus is called.
         * @param name Account of the user.
         * @param status Status of the user: Whether the user is online.
         *               1: User is online.
         *               0: User is offline.
         */
        void onQueryUserStatusResult(String name, String status);

        /**
         * Triggered when a call invitation or request is received.
         * @param channelID Channel name.
         * @param account User ID of the other user defined by the client.
         * @param uid N/A
         * @param extra N/A
         */
        void onInviteReceived(String channelID, String account, int uid, String extra);

        /**
         * Triggered when the callee has received the call invitation or request.
         * @param channelID Channel name
         * @param account User ID of the callee defined by the client.
         * @param uid N/A
         */
        void onInviteReceivedByPeer(String channelID, String account, int uid);

        /**
         * Received by the caller when the callee has accepted the call invitation or request.
         * @param channelID Channel name.
         * @param account User ID of the callee defined by the client.
         * @param uid N/A
         * @param extra N/A
         */
        void onInviteAcceptedByPeer(String channelID, String account, int uid,  String extra);

        /**
         * Received by the caller when the callee rejects the call invitation or request.
         * @param channelID Channel name.
         * @param account User ID of the callee defined by the client.
         * @param uid N/A
         * @param extra N/A
         */
        void onInviteRefusedByPeer(String channelID, String account, int uid,  String extra);

        /**
         * Triggered when the caller ends the call.
         * @param channelID Channel name.
         * @param account User ID of the caller defined by the client.
         * @param uid N/A.
         */
        void onInviteEndByMyself(String channelID, String account, int uid);

        /**
         * Triggered when the callee has ended the call.
         * @param channelID Channel name.
         * @param account User ID of the callee defined by the client.
         * @param uid N/A
         * @param extra N/A
         */
        void onInviteEndByPeer(String channelID, String account, int uid,  String extra);

        /**
         * Triggered when a call has failed.
         * @param channelID Channel name
         * @param account User ID of the callee defined by the client.
         * @param uid N/A
         * @param ecode Refer to {@link io.agora.rtm.RtmStatusCode.PeerMessageState}
         * @param extra N/A
         */
        void onInviteFailed(String channelID, String account, int uid, int ecode,  String extra);

        /**
         * Triggered when the connection to Agora’s signaling system has been lost after login.
         * @param nretry N/A
         */
        void onReconnecting(int nretry);

        /**
         * Triggered when reconnected to Agora’s signaling system.
         * @param fd N/A
         */
        void onReconnected(int fd);

        /**
         * Triggered when connection is aborted somehow (maybe logged in another device).
         * To re-login, {@link IAgoraAPI#logout} method shall be called, followed by the
         * {@link IAgoraAPI#logout} method call.
         */
        void onConnectionAborted();
    }

}
