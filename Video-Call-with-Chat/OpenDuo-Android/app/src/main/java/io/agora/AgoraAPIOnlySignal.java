package io.agora;

import android.content.Context;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.IResultCallback;
import io.agora.rtm.IStateListener;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.RtmStatusCode;

public class AgoraAPIOnlySignal implements IAgoraAPI {

    /**
     * signals for calling system use case
     */
    private static final String SIGNAL_PREFIX = "SIG_";
    private static final String SIG_QUERY_USER_STATUS = SIGNAL_PREFIX + "QUERY_USER_STATUS";
    private static final String SIG_CHANNEL_INVITE_USER = SIGNAL_PREFIX + "CHANNEL_INVITE_USER";
    private static final String SIG_CHANNEL_INVITE_ACCEPT = SIGNAL_PREFIX + "CHANNEL_INVITE_ACCEPT";
    private static final String SIG_CHANNEL_INVITE_REFUSE = SIGNAL_PREFIX + "CHANNEL_INVITE_REFUSE";
    private static final String SIG_CHANNEL_INVITE_END = SIGNAL_PREFIX + "CHANNEL_INVITE_END";
    private static final String SIG_CHANNEL_INVITE_USER2 = SIGNAL_PREFIX + "CHANNEL_INVITE_USER2";

    private static AgoraAPIOnlySignal sInstance = null;

    private RtmClient mRtmClient;
    private ICallBack mCallback;
    private Object mLock = new Object();

    private RtmClientListener mRtmClientListener = new RtmClientListener() {
        @Override
        public void onConnectionStateChanged(int newState) {
            synchronized (mLock) {
                if (mCallback == null) {
                    return;
                }
                switch (newState) {
                    case RtmStatusCode.ConnectionState.CONNECTION_STATE_DISCONNECTED:
                        mCallback.onReconnecting(0);
                        break;
                    case RtmStatusCode.ConnectionState.CONNECTION_STATE_CONNECTED:
                        mCallback.onReconnected(0);
                        break;
                    case RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED:
                        mCallback.onConnectionAborted();
                    default:
                        break;
                }
            }
        }

        @Override
        public void onMessageReceived(RtmMessage message, String peerId) {
            if (message == null) {
                return;
            }
            String text = message.getText();
            if (text == null || text.isEmpty()) {
                return;
            }
            if (text.startsWith(SIGNAL_PREFIX)) {
                handleSignal(text, peerId);
            }
        }
    };

    AgoraAPIOnlySignal() {}

    private AgoraAPIOnlySignal(Context context, String appID) {
        try {
            mRtmClient = RtmClient.createInstance(context, appID, mRtmClientListener);
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
                synchronized (mLock) {
                    if (mCallback != null) {
                        mCallback.onLogout(RtmStatusCode.LogoutError.LOGOUT_ERR_OK);
                    }
                }
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                synchronized (mLock) {
                    if (mCallback != null) {
                        mCallback.onLogout(errorInfo.getErrorCode());
                    }
                }
            }
        });

    }

    @Override
    public void queryUserStatus(final String account) {
        mRtmClient.sendMessageToPeer(account, createSignal(SIG_QUERY_USER_STATUS),
                new IStateListener() {
            @Override
            public void onStateChanged(int newState) {
                String result = (newState == RtmStatusCode.PeerMessageState.PEER_MESSAGE_RECEIVED_BY_PEER) ? "1" : "0";
                synchronized (mLock) {
                    if (mCallback != null) {
                        mCallback.onQueryUserStatusResult(account, result);
                    }
                }
            }
        });
    }

    @Override
    public void channelInviteUser(final String channelID, final String account, final int uid) {
        mRtmClient.sendMessageToPeer(account, createSignal(SIG_CHANNEL_INVITE_USER),
                new IStateListener() {
                    @Override
                    public void onStateChanged(int newState) {
                        synchronized (mLock) {
                            if (mCallback == null) {
                                return;
                            }
                            if (newState == RtmStatusCode.PeerMessageState.PEER_MESSAGE_RECEIVED_BY_PEER) {
                                mCallback.onInviteReceivedByPeer(channelID, account, uid);
                            } else {
                                // TODO: 2018/11/29 error code mapping
                                mCallback.onInviteFailed(channelID, account, uid, newState, "");
                            }
                        }
                    }
        });
    }

    @Override
    public void channelInviteUser2(final String channelID, final String account, final String extra) {
        mRtmClient.sendMessageToPeer(account, createSignal(SIG_CHANNEL_INVITE_USER2),
                new IStateListener() {
                    @Override
                    public void onStateChanged(int newState) {
                        synchronized (mLock) {
                            if (mCallback == null) {
                                return;
                            }
                            if (newState == RtmStatusCode.PeerMessageState.PEER_MESSAGE_RECEIVED_BY_PEER) {
                                mCallback.onInviteReceivedByPeer(channelID, account, 0);
                            } else {
                                // TODO: 2018/11/29 error code mapping
                                mCallback.onInviteFailed(channelID, account, 0, newState, "");
                            }
                        }
                    }
                });
    }

    @Override
    public void channelInviteAccept(String channelID, String account, int uid, String extra) {
        mRtmClient.sendMessageToPeer(account, createSignal(SIG_CHANNEL_INVITE_ACCEPT), null);
    }

    @Override
    public void channelInviteRefuse(String channelID, String account, int uid, String extra) {
        mRtmClient.sendMessageToPeer(account, createSignal(SIG_CHANNEL_INVITE_REFUSE), null);
    }

    @Override
    public void channelInviteEnd(String channelID, String account, int uid) {
        mRtmClient.sendMessageToPeer(account, createSignal(SIG_CHANNEL_INVITE_END), null);
        synchronized (mLock) {
            if (mCallback != null) {
                mCallback.onInviteEndByMyself(channelID, account, uid);
            }
        }
    }

    private void handleSignal(String cmd, String peerId) {
        // TODO: 2018/11/29 channelId and extra
        synchronized (mLock) {
            if (mCallback == null) {
                return;
            }
            switch (cmd) {
                case SIG_QUERY_USER_STATUS: {
                    // DO NOTHING
                }
                break;
                case SIG_CHANNEL_INVITE_USER:
                case SIG_CHANNEL_INVITE_USER2: {
                    mCallback.onInviteReceived("", peerId, 0, "");
                }
                break;
                case SIG_CHANNEL_INVITE_ACCEPT: {
                    mCallback.onInviteAcceptedByPeer("", peerId, 0, "");
                }
                break;
                case SIG_CHANNEL_INVITE_REFUSE: {
                    mCallback.onInviteRefusedByPeer("", peerId, 0, "");
                }
                break;
                case SIG_CHANNEL_INVITE_END: {
                    mCallback.onInviteEndByPeer("", peerId, 0, "");
                }
                break;
                default:
                    // unknown command
                    break;
            }
        }
    }

    private RtmMessage createSignal(String cmd) {
        RtmMessage message = RtmMessage.createMessage();
        message.setText(cmd);
        return message;
    }

}
