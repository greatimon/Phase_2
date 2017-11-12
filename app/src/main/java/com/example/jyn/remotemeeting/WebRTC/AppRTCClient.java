package com.example.jyn.remotemeeting.WebRTC;

/**
 * Created by JYN on 2017-11-10.
 */


import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.List;

/**
 * AppRTCClient is the interface representing an AppRTC client.
 * AppRTCClient는 AppRTC 클라이언트를 나타내는 인터페이스입니다.
 */
public interface AppRTCClient {

    /**
     * Struct holding the connection parameters of an AppRTC room.
     * AppRTC 룸의 연결 매개 변수를 보유하는 구조체입니다.
     */
    class RoomConnectionParameters {
        public final String roomUrl;
        public final String roomId;
        public final boolean loopback;
        public final String urlParameters;

        public RoomConnectionParameters(String roomUrl, String roomId, boolean loopback, String urlParameters) {
            this.roomUrl = roomUrl;
            this.roomId = roomId;
            this.loopback = loopback;
            this.urlParameters = urlParameters;
        }

        public RoomConnectionParameters(String roomUrl, String roomId, boolean loopback) {
            this(roomUrl, roomId, loopback, null /* urlParameters */);
        }
    }


    /**
     * Asynchronously connect to an AppRTC room URL using supplied connection parameters.
     * Once connection is established onConnectedToRoom() callback with room parameters is invoked.
     *
     * 제공된 연결 매개 변수를 사용하여 AppRTC room URL에 비동기 적으로 연결합니다.
     * 연결이 설정되면 room 매개 변수가있는 onConnectedToRoom() 콜백이 호출됩니다.
     */
    void connectToRoom(RoomConnectionParameters connectionParameters);

    /**
     * Send offerSDP to the other participant.
     * 다른 참가자에게 offerSDP를 보냅니다.
     */
    void sendOfferSdp(final SessionDescription sdp);

    /**
     * Send answerSDP to the other participant.
     * 다른 참가자에게 answerSDP를 보냅니다.
     */
    void sendAnswerSdp(final SessionDescription sdp);

    /**
     * Send Ice candidate to the other participant.
     * 다른 참가자에게 Ice 후보를 보냅니다.
     */
    void sendLocalIceCandidate(final IceCandidate candidate);

    /**
     * Send removed ICE candidates to the other participant.
     * 다른 참가자에게 지운 Ice후보들을 보냅니다.
     */
    void sendLocalIceCandidateRemovals(final IceCandidate[] candidates);

    /**
     * Disconnect from room.
     * room 연결 끊기
     */
    void disconnectFromRoom();


    /**
     * Struct holding the signaling parameters of an AppRTC room.
     * AppRTC 룸의 신호 매개 변수를 보유하는 구조체입니다.
     */
    class SignalingParameters {
        public final List<PeerConnection.IceServer> iceServers;
        public final boolean initiator;
        public final String clientId;
        public final String wssUrl;
        public final String wssPostUrl;
        public final SessionDescription offerSdp;
        public final List<IceCandidate> iceCandidates;

        public SignalingParameters(List<PeerConnection.IceServer> iceServers, boolean initiator,
                                   String clientId, String wssUrl, String wssPostUrl, SessionDescription offerSdp,
                                   List<IceCandidate> iceCandidates) {
            this.iceServers = iceServers;
            this.initiator = initiator;
            this.clientId = clientId;
            this.wssUrl = wssUrl;
            this.wssPostUrl = wssPostUrl;
            this.offerSdp = offerSdp;
            this.iceCandidates = iceCandidates;
        }
    }

    /**
     * Callback interface for messages delivered on signaling channel.
     * 신호 채널에서 전달되는 메시지에 대한 콜백 인터페이스.
     *
     * <p>Methods are guaranteed to be invoked on the UI thread of |activity|.
     * 메소드는 UI 스레드에서 호출되도록 보장됩니다.
     */
    interface SignalingEvents {
        /**
         * Callback fired once the room's signaling parameters SignalingParameters are extracted.
         * 방의 신호 매개 변수 SignalingParameters가 추출되면 콜백이 시작됩니다.
         */
        void onConnectedToRoom(final SignalingParameters params);

        /**
         * Callback fired once remote SDP is received.
         * 원격 SDP가 수신되면 콜백이 시작됩니다.
         */
        void onRemoteDescription(final SessionDescription sdp);

        /**
         * Callback fired once remote Ice candidate is received.
         * 원격 Ice 후보가 수신되면 콜백이 발동됩니다.
         */
        void onRemoteIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once remote Ice candidate removals are received.
         * 원격 Ice 후보 제거가 수신되면 콜백이 발생합니다.
         */
        void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once channel is closed.
         * 채널이 폐쇄되면 콜백이 시작됩니다.
         */
        void onChannelClose();

        /**
         * Callback fired once channel error happened.
         * 채널 오류가 발생하면 콜백이 시작됩니다.
         */
        void onChannelError(final String description);
    }
}
