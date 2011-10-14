package org.cglr.fsdc;

import tr.gov.ulakbim.jDenetX.classifiers.HoeffdingOptionTreeNB;
import tr.gov.ulakbim.jDenetX.classifiers.HoeffdingOptionTreeNBAdaptive;
import tr.gov.ulakbim.jDenetX.classifiers.HoeffdingTreeNBAdaptive;
import tr.gov.ulakbim.jDenetX.options.ClassOption;
import weka.classifiers.meta.MOA;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.io.IOException;

public class Test {

    //private static ArffLoader loader;

    public static DataSource getDataSource(String fileName) throws Exception {
        DataSource source = new DataSource(fileName);
        return source;
    }

    public static Instances getInstances(String fileName) throws Exception {
        DataSource source = getDataSource(fileName);
        Instances data = source.getDataSet();
        return data;
    }

    public static ArffLoader getLoader(String fileName) throws IOException {
        ArffLoader loader = new ArffLoader();
        loader.setFile(new File(fileName));
        return loader;
    }

    public static Instances loadData(String fileName) throws IOException {
        // load data
        ArffLoader loader = getLoader(fileName);
        Instances structure = loader.getStructure();
        structure.setClassIndex(structure.numAttributes() - 1);
        return structure;
    }

    public static void test1() throws Exception {
        System.out.println("=======================");
        System.out.println("Starting test1");
        HoeffdingOptionTreeNBAdaptive hotNBAdap = new HoeffdingOptionTreeNBAdaptive();
        //hotNBAdap.splitCriterionOption(ClassOption.)
        String trainingFile = "/home/caglar/Documents/Datasets/GN3_TrafficClassification/gtvs/Day1.TCP.arff";
        String testFile = "/home/caglar/Documents/Datasets/GN3_TrafficClassification/gtvs/Day2.TCP.arff";
        //Instances training = loadData(trainingFile);
        int i = 0;
        Instance current;
        //ArffLoader trainingLoader = getLoader(trainingFile);
        MOA moa = new MOA();
        ClassOption classifierOption = new ClassOption("classOption", 'c', "classifier_to_use", HoeffdingOptionTreeNBAdaptive.class, "HoeffdingOptionTreeNBAdaptive");
        moa.setClassifier(classifierOption);
        //System.out.println(moa.listOptions());
        //System.out.println(classifierOption.getRequiredType());
        /*ArffLoader trainingLoader = new ArffLoader();
          trainingLoader.setFile(new File(trainingFile));
          Instances trainingStructure = trainingLoader.getStructure();
          trainingStructure.setClassIndex(trainingStructure.numAttributes() - 1);*/
        Instances trainingStructure = getInstances(trainingFile);
        trainingStructure.setClassIndex(trainingStructure.numAttributes() - 1);
        System.out.println("Number of instances " + trainingStructure.numInstances());
        moa.buildClassifier(trainingStructure);
        //IntOption gracePeriodOption = new IntOption();
        //System.out.println(trainingLoader.getNextInstance(trainingStructure));
        /*hotNBAdap.splitCriterionOption = new ClassOption("splitCriterion", 's', "Split criterion to use.", SplitCriterion.class, "InfoGainSplitCriterion");
          while((current = (Instance) trainingLoader.getNextInstance(trainingStructure)) != null){
              System.out.println( "Index is: " + i + "\n" );
              //System.out.println(current.toString());
              hotNBAdap.trainOnInstanceImpl(current);
              i++;
          }*/

        ArffLoader testLoader = new ArffLoader();
        testLoader.setFile(new File(testFile));
        Instances testStructure = testLoader.getStructure();
        testStructure.setClassIndex(testStructure.numAttributes() - 1);

        double category = 0.0;
        int correctlyClassified = 0;
        int totalNumberOfInstances = 0;
        double ratio = 0.0;
        while ((current = (Instance) testLoader.getNextInstance(testStructure)) != null) {
            category = moa.classifyInstance(current);
            //System.out.println("Current Class index: "+current.classValue());
            //System.out.println("Classified class: "+category);
            if (category == current.classValue()) {
                correctlyClassified++;
            }
            totalNumberOfInstances++;
        }
        ratio = ((double) correctlyClassified / (double) totalNumberOfInstances) * 100;
        System.out.println("No of Correctly classified " + correctlyClassified);
        System.out.println("No of instances " + totalNumberOfInstances);
        System.out.println("Ratio is " + ratio);
    }

    public static void test2() throws Exception {
        System.out.println("=======================");
        System.out.println("Starting test2");
        String trainingFile = "/home/caglar/Documents/Datasets/GN3_TrafficClassification/gtvs/Day1.TCP.arff";
        String testFile = "/home/caglar/Documents/Datasets/GN3_TrafficClassification/gtvs/Day2.TCP.arff";
        Instance current;
        MOA moa = new MOA();
        ClassOption classifierOption = new ClassOption("classOption", 'c', "classifier_to_use", HoeffdingTreeNBAdaptive.class, "HoeffdingTreeNBAdaptive");
        moa.setClassifier(classifierOption);
        Instances trainingStructure = getInstances(trainingFile);
        trainingStructure.setClassIndex(trainingStructure.numAttributes() - 1);
        System.out.println("Number of instances " + trainingStructure.numInstances());
        moa.buildClassifier(trainingStructure);

        ArffLoader testLoader = new ArffLoader();
        testLoader.setFile(new File(testFile));
        Instances testStructure = testLoader.getStructure();
        testStructure.setClassIndex(testStructure.numAttributes() - 1);

        double category = 0.0;
        int correctlyClassified = 0;
        int totalNumberOfInstances = 0;
        double ratio = 0.0;
        while ((current = (Instance) testLoader.getNextInstance(testStructure)) != null) {
            category = moa.classifyInstance(current);
            if (category == current.classValue()) {
                correctlyClassified++;
            }
            totalNumberOfInstances++;
        }
        ratio = ((double) correctlyClassified / (double) totalNumberOfInstances) * 100;
        System.out.println("No of Correctly classified " + correctlyClassified);
        System.out.println("No of instances " + totalNumberOfInstances);
        System.out.println("Ratio is " + ratio);
    }

    public static void test3() throws Exception {
        System.out.println("=======================");
        System.out.println("Starting test3\n");
        String trainingFile = "/home/caglar/Documents/Datasets/GN3_TrafficClassification/gtvs/Day1.TCP.arff";
        String testFile = "/home/caglar/Documents/Datasets/GN3_TrafficClassification/gtvs/Day2.TCP.arff";
        Instance current;
        MOA moa = new MOA();
        ClassOption classifierOption = new ClassOption("classOption", 'c', "classifier_to_use", HoeffdingOptionTreeNB.class, "HoeffdingOptionTreeNB");
        moa.setClassifier(classifierOption);
        Instances trainingStructure = getInstances(trainingFile);
        trainingStructure.setClassIndex(trainingStructure.numAttributes() - 1);
        System.out.println("Number of instances " + trainingStructure.numInstances());
        moa.buildClassifier(trainingStructure);

        ArffLoader testLoader = new ArffLoader();
        testLoader.setFile(new File(testFile));
        Instances testStructure = testLoader.getStructure();
        testStructure.setClassIndex(testStructure.numAttributes() - 1);

        double category = 0.0;
        int correctlyClassified = 0;
        int totalNumberOfInstances = 0;
        double ratio = 0.0;
        while ((current = (Instance) testLoader.getNextInstance(testStructure)) != null) {
            category = moa.classifyInstance(current);
            if (category == current.classValue()) {
                correctlyClassified++;
            }
            totalNumberOfInstances++;
        }
        ratio = ((double) correctlyClassified / (double) totalNumberOfInstances) * 100;
        System.out.println("No of Correctly classified " + correctlyClassified);
        System.out.println("No of instances " + totalNumberOfInstances);
        System.out.println("Ratio is " + ratio);
    }

    public static void main(String args[]) {
        try {
            test1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}