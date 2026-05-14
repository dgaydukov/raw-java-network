import com.network.raw.tcp.socket.TcpClient;

/**
 * Use this class to imitate force connection lost or network lost so server will receive SocketException
 */
void main() {
    int serverPort = 5555;
    String serverHost = "127.0.0.1";
    new TcpClient(serverPort, serverHost, 99).run();
}