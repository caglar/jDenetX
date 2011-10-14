package tr.gov.ulakbim.jDenetX.streams;

import tr.gov.ulakbim.jDenetX.core.InstancesHeader;
import tr.gov.ulakbim.jDenetX.core.ObjectRepository;
import tr.gov.ulakbim.jDenetX.core.VotedInstance;
import tr.gov.ulakbim.jDenetX.core.VotedInstancePool;
import tr.gov.ulakbim.jDenetX.options.AbstractOptionHandler;
import tr.gov.ulakbim.jDenetX.tasks.TaskMonitor;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Iterator;

public class CotrainInstanceStream extends AbstractOptionHandler implements InstanceStream {

    private static final long serialVersionUID = 1L;

    protected Instances instances;

    private VotedInstancePool VotedInstances = new VotedInstancePool();

    protected Instance lastInstanceRead;

    protected int numInstancesRead;

    protected static boolean hitEndOfTree = false;

    protected long sizeOfTree;

    protected static Iterator<VotedInstance> vitIterator;

    public CotrainInstanceStream(VotedInstancePool votedInstancePool) {
        this.VotedInstances = votedInstancePool;
        sizeOfTree = VotedInstances.getSize();
        numInstancesRead = 0;
        vitIterator = VotedInstances.iterator();
        restart();
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
                                     ObjectRepository repository) {
        restart();
    }

    @Override
    public long estimatedRemainingInstances() {
        return (long) (this.sizeOfTree - this.numInstancesRead);
    }

    @Override
    public InstancesHeader getHeader() {
        return new InstancesHeader(this.instances);
    }

    @Override
    public boolean hasMoreInstances() {
        return vitIterator.hasNext();
    }

    @Override
    public boolean isRestartable() {
        return true;
    }

    @Override
    public Instance nextInstance() {
        numInstancesRead++;
        return vitIterator.next().getInstance();
    }

    public long getStreamSize() {
        return this.sizeOfTree;
    }

    @Override
    public void restart() {
        sizeOfTree = VotedInstances.getSize();
        numInstancesRead = 0;
        vitIterator = VotedInstances.iterator();
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
    }

}
