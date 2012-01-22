package tr.gov.ulakbim.jDenetX.streams.net;

import weka.core.AbstractInstance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author caglar
 */
public class UDPStreamReader implements Runnable {

    private int PortNo;
    private int PacketSize;
    private UDPInstanceQueue m_UDPInstQueue;
    private UDPStreamReceiver m_UDPStreamReceiver;
    private String m_arffHeader;
    private Instances Structure;
    private int m_Timeout;
    private boolean m_LoopStarted;
    private Thread t;
    private boolean m_isClassMissing;
    private boolean m_Suspendend;

    public UDPStreamReader (int portNo, int packetSize, int timeout, String arffHeader, boolean isClassMissing)
    {
        PortNo = portNo;
        PacketSize = packetSize;
        m_UDPStreamReceiver = new UDPStreamReceiver(portNo, packetSize);
        m_arffHeader = arffHeader;
        m_UDPInstQueue = new UDPInstanceQueue();
        m_Timeout = timeout;
        m_LoopStarted = false;
        m_isClassMissing = isClassMissing;
        m_Suspendend = false;
        parseStructure();

        m_UDPInstQueue.setStructure(Structure);
        m_UDPInstQueue.setIsClassMissing(isClassMissing);
        t = new Thread(this);
        t.start();
    }

    public UDPStreamReader (int portNo, int packetSize, int timeout, String arffHeader)
    {
        PortNo = portNo;
        PacketSize = packetSize;
        m_UDPStreamReceiver = new UDPStreamReceiver(portNo, packetSize);
        m_arffHeader = arffHeader;
        m_UDPInstQueue = new UDPInstanceQueue();
        m_Timeout = timeout;
        m_LoopStarted = false;
        m_isClassMissing = true;
        parseStructure();

        m_UDPInstQueue.setStructure(Structure);
        m_UDPInstQueue.setIsClassMissing(m_isClassMissing);
        t = new Thread(this);
        t.start();
    }

    public UDPStreamReader (int portNo, int timeout, String arffHeader)
    {
        PortNo = portNo;
        PacketSize = 1024;
        m_UDPStreamReceiver = new UDPStreamReceiver(portNo, PacketSize);
        m_arffHeader = arffHeader;
        m_UDPInstQueue = new UDPInstanceQueue();
        m_Timeout = timeout;
        m_LoopStarted = false;
        m_isClassMissing = true;
        parseStructure();
        m_UDPInstQueue.setStructure(Structure);
        m_UDPInstQueue.setIsClassMissing(m_isClassMissing);
        t = new Thread(this);
        t.start();
    }

    public UDPStreamReader (int timeout, String arffHeader)
    {
        PortNo = 9999;
        PacketSize = 1024;
        m_UDPStreamReceiver = new UDPStreamReceiver(PortNo, PacketSize);
        m_arffHeader = arffHeader;
        m_UDPInstQueue = new UDPInstanceQueue();
        m_Timeout = timeout;
        m_LoopStarted = false;
        m_isClassMissing = true;
        parseStructure();

        m_UDPInstQueue.setStructure(Structure);
        m_UDPInstQueue.setIsClassMissing(m_isClassMissing);
        t = new Thread(this);
        t.start();
    }

    private void parseStructure()
    {
        try {
            BufferedReader bReader = new BufferedReader(new FileReader(m_arffHeader));
            ArffReader arffReader = new ArffReader(bReader);
            Structure = arffReader.getStructure();
            if (!m_isClassMissing)
                Structure.setClassIndex(Structure.numAttributes() - 1);
        } catch (IOException IOEx) {
            Logger.getLogger(UDPStreamReader.class.getName()).log(Level.SEVERE, null, IOEx);
        }
    }

    public int getPacketSize() {
        return PacketSize;
    }

    public void setPacketSize (int PacketSize) {
        this.PacketSize = PacketSize;
    }

    public int getPortNo() {
        return PortNo;
    }

    public void setPortNo (int PortNo) {
        this.PortNo = PortNo;
    }

    protected  UDPInstanceQueue getUDPInstQueue() {
        return m_UDPInstQueue;
    }

    protected void setUDPInstQueue (UDPInstanceQueue UDPInstQueue) {
        this.m_UDPInstQueue = UDPInstQueue;
    }

    public void setArffHeader (String m_arffHeader) {
        this.m_arffHeader = m_arffHeader;
    }

    public void putHostConstraint (InetAddress remoteAddr) {
        m_UDPStreamReceiver.putHostConstraint(remoteAddr);
    }

    public AbstractInstance  getInstanceFromQueue() {
        return (AbstractInstance) m_UDPInstQueue.peek();
    }

    public void setIsClassMissing (boolean isClassMissing) {
        this.m_isClassMissing = isClassMissing;
        m_UDPInstQueue.setIsClassMissing(isClassMissing);
        parseStructure();
    }

    public void startUDPLoop () throws Exception {
        m_LoopStarted = true;
        try {
            m_UDPStreamReceiver.openSocket(m_Timeout);
        } catch (SocketException ex) {
            Logger.getLogger(UDPStreamReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        for(;;){
            //if (m_UDPStreamReceiver.isSockBound())
            {
                String message = null;
                try {
                    message = m_UDPStreamReceiver.getPacketData();
                   // System.out.println(message);
                } catch (SocketException ex) {
                    Logger.getLogger(UDPStreamReader.class.getName()).log(Level.SEVERE, null, ex);

                } catch (IOException ex) {
                    Logger.getLogger(UDPStreamReader.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (message == null) {
                        System.out.println("Queue size" + m_UDPInstQueue.size());
                    }
                }
                if (message != null && message.length() > 0) {
//                    System.out.println("Message is: " + message);
                    if(!m_UDPInstQueue.addLine(message)) {
                        throw new Exception("Couldn't add the row to the queue\n");
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            if (!m_LoopStarted) {
                startUDPLoop();
            } else {
                System.err.println("Loop has already started,");
            }
        } catch (Exception ex) {
            Logger.getLogger(UDPStreamReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}