package com.example.jyn.remotemeeting.WebRTC;

/**
 * Created by JYN on 2017-11-10.
 */

import android.util.Log;

import org.webrtc.ThreadUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;

/**
 * Replacement for WebSocketChannelClient for direct communication between two IP addresses.
 * Handles the signaling between the two clients using a TCP connection.
 *
 * All public methods should be called from a looper executor thread passed in a constructor, otherwise exception will be thrown.
 * All events are dispatched on the same thread.
 *
 * 두 IP 주소 간의 직접 통신을위한 WebSocketChannelClient 대체.
 * TCP 연결을 사용하여 두 클라이언트 간의 신호를 처리합니다.
 * 모든 public 메소드는 생성자에서 전달 된 루퍼 executor 스레드에서 호출해야합니다. 그렇지 않으면 예외가 발생합니다.
 * 모든 이벤트는 동일한 스레드에서 전달됩니다.
 */
public class TCPChannelClient {

    private static final String TAG = "TCPChannelClient";

    private final ExecutorService executor;
    private final ThreadUtils.ThreadChecker executorThreadCheck;
    private final TCPChannelEvents eventListener;
    private TCPSocket socket;

    /**
     * Callback interface for messages delivered on TCP Connection.
     * All callbacks are invoked from the looper executor thread.
     *
     * TCP Connection에서 전달되는 메시지에 대한 콜백 인터페이스.
     * 모든 콜백은 루퍼 실행자 스레드에서 호출됩니다.
     */
    public interface TCPChannelEvents {
        void onTCPConnected(boolean server);
        void onTCPMessage(String message);
        void onTCPError(String description);
        void onTCPClose();
    }


    /**
     * Initializes the TCPChannelClient.
     * If IP is a local IP address, starts a listening server on that IP. If not, instead connects to the IP.
     *
     * @param eventListener Listener that will receive events from the client.
     * @param ip IP address to listen on or connect to.
     * @param port Port to listen on or connect to.
     *
     * TCPChannelClient를 초기화합니다.
     * IP가 로컬 IP 주소 인 경우 해당 IP에서 수신 서버를 시작합니다, 그렇지 않은 경우 대신 IP에 연결합니다.
     *
     * @param eventListener 클라이언트로부터 이벤트를 수신하는 리스너.
     * @param ip 수신 대기하거나 연결할 IP 주소.
     * @param port 수신 대기하거나 연결할 포트.
     */
    public TCPChannelClient(
            ExecutorService executor, TCPChannelEvents eventListener, String ip, int port) {
        this.executor = executor;
        executorThreadCheck = new ThreadUtils.ThreadChecker();
        executorThreadCheck.detachThread();
        this.eventListener = eventListener;

        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            reportError("Invalid IP address.");
            return;
        }

        if (address.isAnyLocalAddress()) {
            socket = new TCPSocketServer(address, port);
        } else {
            socket = new TCPSocketClient(address, port);
        }

        socket.start();
    }


    /**
     * Disconnects the client if not already disconnected. This will fire the onTCPClose event.
     * 아직 연결이 끊어지지 않은 경우 클라이언트의 연결을 끊습니다, 그러면 onTCPClose 이벤트가 발생합니다.
     */
    public void disconnect() {
        executorThreadCheck.checkIsOnValidThread();
        socket.disconnect();
    }


    /**
     * Sends a message on the socket.
     * @param message Message to be sent.
     *
     * 소켓에 메시지를 보냅니다.
     * @param message 보낼 메시지입니다.
     */
    public void send(String message) {
        executorThreadCheck.checkIsOnValidThread();
        socket.send(message);
    }


    /**
     * Helper method for firing onTCPError events. Calls onTCPError on the executor thread.
     * TCPError 이벤트를 발생시키는 Helper method. 실행 프로그램 스레드에서 onTCPError 호출.
     */
    private void reportError(final String message) {
        Log.e(TAG, "TCP Error: " + message);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                eventListener.onTCPError(message);
            }
        });
    }


    /**
     * Base class for server and client sockets.
     * Contains a listening thread that will calleventListener.onTCPMessage on new messages.
     *
     * 서버 및 클라이언트 소켓의 기본 클래스.
     * new messages에서 calleventListener.onTCPMessage를 수신하는 수신 스레드가 들어 있습니다.
     */
    private abstract class TCPSocket extends Thread {
        // Lock for editing out and rawSocket
        // 편집 및 rawSocket용 잠금
        protected final Object rawSocketLock;
        private PrintWriter out;
        private Socket rawSocket;

        /**
         * Connect to the peer, potentially a slow operation.
         * @return Socket connection, null if connection failed.
         *
         * 피어에 연결하십시오. 잠재적으로 느린 작업 일 수 있습니다.
         * @return 소켓 연결. 연결에 실패하면 null.
         */
        public abstract Socket connect();

        /** Returns true if sockets is a server rawSocket. */
        /** 소켓이 서버 rawSocket인 경우는 true를 돌려줍니다. */
        public abstract boolean isServer();

        TCPSocket() {
            rawSocketLock = new Object();
        }

        /**
         * The listening thread.
         */
        @Override
        public void run() {
            Log.d(TAG, "Listening thread started...");

            // Receive connection to temporary variable first, so we don't block.
            // 임시 변수에 대한 연결을 먼저 받으므로 차단하지 않습니다.
            Socket tempSocket = connect();
            BufferedReader in;

            Log.d(TAG, "TCP connection established.");

            synchronized (rawSocketLock) {
                if (rawSocket != null) {
                    Log.e(TAG, "Socket already existed and will be replaced.");
                }

                rawSocket = tempSocket;

                // Connecting failed, error has already been reported, just exit.
                // 연결에 실패했습니다. 오류가 이미보고되었습니다. 종료하십시오.
                if (rawSocket == null) {
                    return;
                }

                try {
                    out = new PrintWriter(rawSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(rawSocket.getInputStream()));
                } catch (IOException e) {
                    reportError("Failed to open IO on rawSocket: " + e.getMessage());
                    return;
                }
            }

            Log.v(TAG, "Execute onTCPConnected");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "Run onTCPConnected");
                    eventListener.onTCPConnected(isServer());
                }
            });

            while (true) {
                final String message;
                try {
                    message = in.readLine();
                } catch (IOException e) {
                    synchronized (rawSocketLock) {
                        // If socket was closed, this is expected.
                        // 소켓이 닫혀있을 경우, 예상되는 작업
                        if (rawSocket == null) {
                            break;
                        }
                    }

                    reportError("Failed to read from rawSocket: " + e.getMessage());
                    break;
                }

                // No data received, rawSocket probably closed.
                // 데이터가 수신되지 않았으므로 rawSocket이 종료되었을 수 있습니다.
                if (message == null) {
                    break;
                }

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "Receive: " + message);
                        eventListener.onTCPMessage(message);
                    }
                });
            }

            Log.d(TAG, "Receiving thread exiting...");

            // Close the rawSocket if it is still open.
            // rawSocket가 아직 열려있는 경우, 닫기.
            disconnect();
        }

        /**
         * Closes the rawSocket if it is still open. Also fires the onTCPClose event.
         * rawSocket가 아직 열려있는 경우, 닫습니다. 또한 onTCPClose 이벤트를 발생시킵니다.
         */
        public void disconnect() {
            try {
                synchronized (rawSocketLock) {
                    if (rawSocket != null) {
                        rawSocket.close();
                        rawSocket = null;
                        out = null;

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                eventListener.onTCPClose();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                reportError("Failed to close rawSocket: " + e.getMessage());
            }
        }

        /**
         * Sends a message on the socket. Should only be called on the executor thread.
         * 소켓에 메시지를 보냅니다. executor 스레드에서만 호출되어야합니다.
         */
        public void send(String message) {
            Log.v(TAG, "Send: " + message);

            synchronized (rawSocketLock) {
                if (out == null) {
                    reportError("Sending data on closed socket.");
                    return;
                }

                out.write(message + "\n");
                out.flush();
            }
        }
    }


    private class TCPSocketServer extends TCPSocket {
        // Server socket is also guarded by rawSocketLock.
        // 서버 소켓도 rawSocketLock에 의해 보호됩니다.
        private ServerSocket serverSocket;

        final private InetAddress address;
        final private int port;

        public TCPSocketServer(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        /** Opens a listening socket and waits for a connection. */
        /** listening socket을 열고 연결을 대기합니다. */
        @Override
        public Socket connect() {
            Log.d(TAG, "Listening on [" + address.getHostAddress() + "]:" + Integer.toString(port));

            final ServerSocket tempSocket;
            try {
                tempSocket = new ServerSocket(port, 0, address);
            } catch (IOException e) {
                reportError("Failed to create server socket: " + e.getMessage());
                return null;
            }

            synchronized (rawSocketLock) {
                if (serverSocket != null) {
                    Log.e(TAG, "Server rawSocket was already listening and new will be opened.");
                }

                serverSocket = tempSocket;
            }

            try {
                return tempSocket.accept();
            } catch (IOException e) {
                reportError("Failed to receive connection: " + e.getMessage());
                return null;
            }
        }

        /** Closes the listening socket and calls super. */
        /** 청취 소켓을 닫고 super.disconnect()를 호출 */
        @Override
        public void disconnect() {
            try {
                synchronized (rawSocketLock) {
                    if (serverSocket != null) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                }
            } catch (IOException e) {
                reportError("Failed to close server socket: " + e.getMessage());
            }

            super.disconnect();
        }

        @Override
        public boolean isServer() {
            return true;
        }
    }


    private class TCPSocketClient extends TCPSocket {
        final private InetAddress address;
        final private int port;

        public TCPSocketClient(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        /** Connects to the peer. */
        /** 피어에 연결합니다. */
        @Override
        public Socket connect() {
            Log.d(TAG, "Connecting to [" + address.getHostAddress() + "]:" + Integer.toString(port));

            try {
                return new Socket(address, port);
            } catch (IOException e) {
                reportError("Failed to connect: " + e.getMessage());
                return null;
            }
        }

        @Override
        public boolean isServer() {
            return false;
        }
    }
}
