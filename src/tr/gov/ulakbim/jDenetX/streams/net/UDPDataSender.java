package tr.gov.ulakbim.jDenetX.streams.net;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Sep 20, 2010
 * Time: 3:05:31 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;


/**
 * @author caglar
 */
public class UDPDataSender {
    private String HostName = "localhost";
    private int RemotePort = 4000;
    private InetAddress RemoteAddr = null;
    DatagramSocket Sock = null;
    SocketAddress SockAddr = null;

    public UDPDataSender() {
    }

    public UDPDataSender(InetAddress remoteAddr, int remotePort) {
        RemoteAddr = remoteAddr;
        RemotePort = remotePort;
        SockAddr = new InetSocketAddress(remoteAddr, remotePort);
    }

    public UDPDataSender(String host, int remotePort) {
        HostName = host;
        RemotePort = remotePort;
        SockAddr = new InetSocketAddress(host, remotePort);
    }

    public void startSocket() throws SocketException {
        if (SockAddr != null) {
            Sock = new DatagramSocket();
        } else {
            throw new NullPointerException("SockAddr can't be null for socket binding!");
        }
    }

    public void closeSocket() {
        Sock.close();
    }

    public void sendMessage(String message) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        PrintStream pOut = new PrintStream(bOut);
        pOut.print(message);
        //convert printstrema to byte array
        byte[] bArray = bOut.toByteArray();
        //Create a datagram packet, containing a maximum buffer of 256 bytes
        DatagramPacket packet = new DatagramPacket(bArray, bArray.length);
        packet.setSocketAddress(SockAddr);
        Sock.send(packet);
    }
}