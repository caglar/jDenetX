package tr.gov.ulakbim.jDenetX.db;

import javarrd.net.stamfest.rrd.RRDCommandPool;
import javarrd.net.stamfest.rrd.RRDToolService;
import org.apache.commons.lang3.ArrayUtils;
import tr.gov.ulakbim.jDenetX.evaluation.SelfOzaBoostClassificationPerformanceEvaluator;

import java.util.Enumeration;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: 10/19/11
 * Time: 9:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class RRDResultsSaver {

    protected HashMap<String, Integer> ClassesCountPool;
    protected Enumeration<String> classNames;
    protected long startTime;
    protected RRDCommandPool pool = null;
    protected RRDToolService rrdToolService = null;
    protected final int PoolSize = 20;
    protected int StepSize;
    protected boolean isRRDCreated = false;
    private final int MaxDS = 100000;

    public RRDResultsSaver(int stepSize, String baseDir) {
        StepSize = stepSize;
        pool = new RRDCommandPool(PoolSize, baseDir, null);
        rrdToolService = new RRDToolService(pool);
        this.ClassesCountPool = new HashMap<String, Integer>();
    }

    public void createRRD(String rrdFilename, int noOfClasses, Enumeration<String> classes) {
        String argsBegin[] = {
                "--step " + StepSize
        };
        classNames = classes;
        String argsDS[] = new String[noOfClasses + 3];
        int i = 0;
        while (classes.hasMoreElements()) {
            argsDS[i] = "DS:" + classes.nextElement() + ":GAUGE:" + 1 + ":" + 0 + ":" + MaxDS;
            i++;
        }
        argsDS[noOfClasses] = "RRA:MIN:0.5:12:2400";
        argsDS[noOfClasses + 1] = "RRA:MAX:0.5:12:2400";
        argsDS[noOfClasses + 2] = "RRA:AVERAGE:0.5:12:2400";
        String args[] = (String[]) ArrayUtils.addAll(argsBegin, argsDS);

        System.out.println(rrdFilename);

        for (String arg : args) {
            System.out.println(arg);
        }

        try {
            rrdToolService.create(rrdFilename, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRRDCreated = true;
    }

    public void updateRRDs(String rrdFilename, SelfOzaBoostClassificationPerformanceEvaluator sbcpe) {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        long endTime = System.currentTimeMillis();
        String arg = "N:";
        int[] instancesClassCounts = sbcpe.getInstancesClassesCount();
        if (Math.abs(startTime - endTime) >= (StepSize * 999)) {
            for (int i = 0; i < instancesClassCounts.length; i++) {
                if (instancesClassCounts[i] == 0) {
                    arg += "U:";
                } else {
                    arg += instancesClassCounts[i] + ":";
                }
            }
            arg = arg.substring(0, arg.length() - 1);
            System.out.println(arg);
            try {
                rrdToolService.update(rrdFilename, arg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sbcpe.reset();
            startTime = System.currentTimeMillis();
        }
    }

    public boolean isRRDCreated() {
        return isRRDCreated;
    }
}
