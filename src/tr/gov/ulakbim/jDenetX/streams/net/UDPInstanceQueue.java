package tr.gov.ulakbim.jDenetX.streams.net;

import weka.core.AbstractInstance;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author caglar
 */
public class UDPInstanceQueue implements Queue {

    private Queue<AbstractInstance> InstQueue;
    private Instances Structure;
    private boolean m_isClassMissing;

    public UDPInstanceQueue(boolean isClassMissing) {
        InstQueue = new LinkedList<AbstractInstance>();
        m_isClassMissing = isClassMissing;
    }

    public UDPInstanceQueue() {
        InstQueue = new LinkedList<AbstractInstance>();
        m_isClassMissing = false;
    }

    public UDPInstanceQueue (Instances structure) {
        InstQueue = new LinkedList<AbstractInstance>();
        Structure = structure;
        m_isClassMissing = false;
    }

    public void setStructure (Instances structure) {
        Structure = structure;
        m_isClassMissing = false;
    }

    @Override
    public boolean add (Object inst) {
        InstQueue.add((AbstractInstance) inst);
        return (inst != null);
    }

    public boolean addLine (String line) {
        int noOfAtts = Structure.numAttributes();
        DenseInstance inst = new DenseInstance(noOfAtts);
        inst.setDataset(Structure);
        String attrs[] = line.split(",");
        for (int i = 0; i < Structure.numAttributes(); i++) {
            if (i == (attrs.length - 1)) {
                if (!inst.classIsMissing()) {
                    inst.setClassValue(attrs[i]);
                }
            } else if (Structure.attribute(i).isNumeric()) {
                inst.setValue(Structure.attribute(i), Double.parseDouble(attrs[i]));
            } else if (Structure.attribute(i).isNominal()) {
                inst.setValue(i, Double.parseDouble(attrs[i]));
            }
        }
        if (m_isClassMissing) {
            inst.setClassMissing();
        }
        InstQueue.add(inst);
        return ((line.length() > 1) && (Structure != null));
    }

    @Override
    public boolean offer (Object inst) {
        InstQueue.add((AbstractInstance) inst);
        return (inst != null);
    }

    public boolean offerLine (String line) {
        int noOfAtts = Structure.numAttributes();
        DenseInstance inst = new DenseInstance(noOfAtts);
        inst.setDataset(Structure);
        String attrs[] = line.split(",");
        for (int i = 0; i < Structure.numAttributes(); i++) {
            if (attrs[i].equals("?")) {
                inst.setMissing(i);//Add the missing attribute
            }
            else {
                inst.setValue(i, attrs[i]);
            }
        }
        inst.setClassMissing();
        InstQueue.add(inst);
        return ((line.length() > 1) && (Structure != null));
    }

    @Override
    public Object remove() {
        return InstQueue.remove();
    }

    @Override
    public Object poll() {
        return InstQueue.poll();
    }

    @Override
    public Object element() {
        return InstQueue.element();
    }

    @Override
    public Object peek() {
        return InstQueue.peek();
    }

    @Override
    public int size() {
        return InstQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return InstQueue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return InstQueue.contains(o);
    }

    @Override
    public Iterator iterator() {
        return InstQueue.iterator();
    }

    @Override
    public Object[] toArray() {
        return InstQueue.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return InstQueue.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return InstQueue.remove(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return InstQueue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        return InstQueue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return InstQueue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        return InstQueue.retainAll(c);
    }

    @Override
    public void clear() {
        InstQueue.clear();
        Structure.clear();
    }

    public void setIsClassMissing (boolean isClassMissing) {
        this.m_isClassMissing = isClassMissing;
    }

    public boolean isClassMissing () {
        return m_isClassMissing;
    }
}