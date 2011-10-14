package tr.gov.ulakbim.jDenetX.sampling;

import weka.core.Instance;
import weka.core.Randomizable;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Nov 22, 2010
 * Time: 10:13:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReservoirSampling implements Randomizable {

    private Reservoir mReservoir = null;
    private int mReservoirCapacity = 10000;
    private int mCurrentCursor = 0;
    private int Seed = 1;
    private Random Rndizer;

    public void setReservoirCapacity(int resSize) {
        mReservoirCapacity = resSize;
        mCurrentCursor = resSize;
    }

    private void initReservoir() {
        mReservoir = new Reservoir(mReservoirCapacity);
    }

    private void intRandomizer() {
        Rndizer = new Random(Seed);
    }

    public ReservoirSampling() {
        mReservoir = new Reservoir();
    }

    public void addInstanceToReservoir(Instance inst) {
        int i = 0;
        if (mReservoir == null) {
            initReservoir();
        }
        if (mReservoir.getCurrentResSize() < mReservoirCapacity) {
            mReservoir.addInstance(inst);
        } else {
            i = Rndizer.nextInt(mCurrentCursor);
            if (i <= mReservoirCapacity) {
                mReservoir.setInstance(i, inst);
            }
            mCurrentCursor++;
        }
    }

    public int getReservoirSize() {
        return mReservoirCapacity;
    }

    public void restart() {
        if (mReservoir != null) {
            mReservoir.cleanReservoir();
        }
        int mCurrentCursor = 0;
    }

    @Override
    public void setSeed(int seed) {
        Seed = seed;
    }

    @Override
    public int getSeed() {
        return Seed;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
