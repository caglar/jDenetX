package tr.gov.ulakbim.jDenetX.streams;

import tr.gov.ulakbim.jDenetX.core.InputStreamProgressMonitor;
import tr.gov.ulakbim.jDenetX.core.InstancesHeader;
import tr.gov.ulakbim.jDenetX.core.ObjectRepository;
import tr.gov.ulakbim.jDenetX.options.*;
import tr.gov.ulakbim.jDenetX.streams.net.UDPStreamReceiver;
import tr.gov.ulakbim.jDenetX.tasks.TaskMonitor;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Sep 20, 2010
 * Time: 3:08:54 PM
 * To change this template use File | Settings | File Templates.
 */


public class UDPStream extends AbstractOptionHandler implements
        InstanceStream {

    @Override
    public String getPurposeString() {
        return "A stream read from UDP.";
    }

    private static final long serialVersionUID = 1L;

    public FileOption arffFileOption = new FileOption("arffFileHeader", 'f',
            "ARFF file header to load.", null, "arff", false);

    public IntOption portOption = new IntOption(
            "portNumber",
            'p',
            "Port number that the socket will be created at and will be bind",
            2500, 1, 65555);

    public IntOption packetSizeOption = new IntOption(
            "packetSizeOption",
            'a',
            "Size of each packet in bytes",
            1024, 8, Integer.MAX_VALUE); //Min length is 8, since this is the size of header 

    public FlagOption checkHostOption = new FlagOption(
            "checkHost",
            't',
            "Do you want to match the host that the packets coming from the host you specify.");

    public StringOption hostOption = new StringOption(
            "host",
            'h',
            "host name or ip that the socket will be created at",
            "-1");

    public IntOption classIndexOption = new IntOption(
            "classIndex",
            'c',
            "Class index of data. 0 for none or -1 for last attribute in file.",
            -1, -1, Integer.MAX_VALUE);

    public IntOption socketTimeoutOption = new IntOption(
            "socketTimeout",
            's',
            "socket timeout in ms (Enter 0 for no timeout)",
            5000, 0, Integer.MAX_VALUE);

    public IntOption windowSizeOption = new IntOption(
            "windowSize",
            'w',
            "Size of the window that the instances will be stored before processing",
            1000, 1, Integer.MAX_VALUE);

    public IntOption maxInstancesOption = new IntOption(
            "maxInstances",
            'm',
            "limit in max number of instances (Enter 0 for no limit)",
            1000000, 0, Integer.MAX_VALUE);

    protected Instances instances; //Queue of instances

    protected Instances activeInstances; //Active Queue of instances

    protected Reader fileReader;

    protected boolean endOfStream;

    protected Instance lastInstanceRead;

    protected int numInstancesRead;

    protected InputStreamProgressMonitor fileProgressMonitor;

    protected UDPStreamReceiver uStreamReceiver;

    public UDPStream() {
    }

    public UDPStream(String arffFileName, int classIndex) {
        this.arffFileOption.setValue(arffFileName);
        this.classIndexOption.setValue(classIndex);
        restart();
    }

    @Override
    public void prepareForUseImpl(TaskMonitor monitor,
                                  ObjectRepository repository) {
        restart();
    }

    public InstancesHeader getHeader() {
        return new InstancesHeader(this.instances);
    }

    public long estimatedRemainingInstances() {
        double progressFraction = 0.0;
        try {
            if (this.fileProgressMonitor != null || this.fileProgressMonitor.available() > 0) {
                progressFraction = this.fileProgressMonitor
                        .getProgressFraction();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if ((progressFraction > 0.0) && (this.numInstancesRead > 0)) {
            return (long) ((this.numInstancesRead / progressFraction) - this.numInstancesRead);
        }
        return -1;
    }

    public boolean hasMoreInstances() {
        if (this.endOfStream) {
            uStreamReceiver.closeSocket();
        }
        return !this.endOfStream;
    }

    public Instance nextInstance() {
        Instance prevInstance = this.lastInstanceRead;
        this.endOfStream = !recieveNextInstanceFromStream();
        return prevInstance;
    }

    public boolean isRestartable() {
        return true;
    }

    public void restart() {
        try {
            if (this.fileReader != null) {
                this.fileReader.close();
            }
            if (this.uStreamReceiver != null) {
                if (this.uStreamReceiver.isSockBound()) {
                    this.uStreamReceiver.closeSocket();
                }
            }

            InputStream fileStream = new FileInputStream(this.arffFileOption
                    .getFile());

            this.fileProgressMonitor = new InputStreamProgressMonitor(
                    fileStream);

            this.fileReader = new BufferedReader(new InputStreamReader(
                    this.fileProgressMonitor));
            ArffReader arff = new ArffReader(fileReader, 0);

            this.instances = arff.getStructure();
            this.activeInstances = arff.getStructure();

            if (this.classIndexOption.getValue() < 0) {
                this.instances
                        .setClassIndex(this.instances.numAttributes() - 1);
                this.activeInstances
                        .setClassIndex(this.activeInstances.numAttributes() - 1);
            } else if (this.classIndexOption.getValue() > 0) {
                this.instances
                        .setClassIndex(this.classIndexOption.getValue() - 1);
                this.activeInstances
                        .setClassIndex(this.classIndexOption.getValue() - 1);
            }

            this.numInstancesRead = 0;
            this.lastInstanceRead = null;
            uStreamReceiver = new UDPStreamReceiver(portOption.getValue(), packetSizeOption.getValue());
            uStreamReceiver.openSocket(socketTimeoutOption.getValue());
            if (checkHostOption.isSet()) {
                if (hostOption.getValue() != null && hostOption.getValue().length() > 7) {
                    InetAddress address = InetAddress.getByName(hostOption.getValue());
                    uStreamReceiver.putHostConstraint(address);
                } else {
                    InetAddress address = InetAddress.getByName(hostOption.getValue());
                    uStreamReceiver.putHostConstraint(address);
                }
            }
            this.endOfStream = !recieveNextInstanceFromStream();
        } catch (IOException ioe) {
            throw new RuntimeException("UDPStream restart failed.", ioe);
        }
    }

    protected boolean recieveNextInstanceFromStream() {
        if (uStreamReceiver == null) {
            throw new NullPointerException("recieveNextInstanceFromStream: UDP Data Receiver object should not be empty");
        }
        if ((this.maxInstancesOption.getValue() == 0) || (this.numInstancesRead < maxInstancesOption.getValue())) {
            if (uStreamReceiver.isSockBound()) {
                try {
                    String message = uStreamReceiver.getPacketData();
                    if (message != null && message.length() > 0) {
                        addInstance(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.getAnonymousLogger().log(Level.WARNING, "An IOException occured!");
                }
            }
            if (activeInstances.size() > 0) {
                this.lastInstanceRead = this.activeInstances.instance(0);
                this.activeInstances.delete(); //keep instances clean
                this.numInstancesRead++;
            } else if (instances.size() > 0) {
                this.lastInstanceRead = this.instances.instance(0);
                this.instances.delete(); // keep instances clean
                this.numInstancesRead++;
            }
            if (uStreamReceiver.isSockBound()) {
                return true;
            }
        }
        return false;
    }

    protected void addInstance(String mess) {
        if (!mess.toLowerCase().startsWith("@data") || !mess.toLowerCase().startsWith("@relation") || !mess.toLowerCase().startsWith("%")) {
            if (instances != null) {
                if (instances.size() < windowSizeOption.getValue()) {
                    addInstanceToInstances(mess, instances);
                } else {
                    activeInstances = instances;
                    instances.clear();
                    addInstanceToInstances(mess, instances);
                }
            } else {
                throw new NullPointerException("instances can't be null.");
            }
        }
    }

    protected void addInstanceToInstances(String mess, Instances insts) {
        String[] tokens = mess.split(",");
        Instance inst = new DenseInstance(insts.numAttributes());
        inst.setDataset(insts);
        ArrayList<Attribute> attList = new ArrayList<Attribute>();
        attList = Collections.list(inst.enumerateAttributes());
        for (Attribute attr : attList) {
            if (attr.isNumeric()) {
                inst.setValue(attr, Double.parseDouble(tokens[attr.index()].trim()));
            } else {
                inst.setValue(attr, tokens[attr.index()].trim());
            }
        }
        if (mess.length() == attList.size() + 1) {
            inst.setClassValue(tokens[attList.size()].trim());
        }
        insts.add(inst);
    }

    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }
}