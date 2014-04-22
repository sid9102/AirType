package sprokit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import libsvm.SelfOptimizingLinearLibSVM;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.clustering.AQBC;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.DensityBasedSpatialClustering;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.KMedoids;
import net.sf.javaml.clustering.SOM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.filter.normalize.NormalizeMidrange;
import py4j.GatewayServer;

public class Identifier {
	private NormalizeMidrange normalizer;
	private Classifier classifier;
	private HashMap<double[], String> gestureCounts;
	private int clusterCount;
	private int stackSize;
	private int none_index;
	private String none_classification = "None";
	
	// active variables
	private LinkedList<Integer> classificationStack;
	private double[] classificationCounts;
	
	public void build(int stackSize, int targetClusterCount, List<List<Double>> data, boolean debug){
		// convert to dataset
		Dataset ds = new DefaultDataset();
		for(List<Double> d : data){
			ds.add(makeInstance(d));
		}
		
		//build normalizer and normalize data
		normalizer = new NormalizeMidrange();
		normalizer.build(ds);
		normalizer.filter(ds);
		
		// cluster data
		DistanceMeasure dm = new DistanceMeasure() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean compare(double arg0, double arg1) {
				return arg0 < arg1;
			}

			@Override
			public double getMaxValue() {
				return Double.MAX_VALUE;
			}

			@Override
			public double getMinValue() {
				return 0.0;
			}

			@Override
			public double measure(Instance arg0, Instance arg1) {
				double distance = 0.0;
				for(Integer x : arg0.keySet()){
					distance += (arg0.get(x) - arg1.get(x)) * (arg0.get(x) - arg1.get(x));
				}
				return distance;
			}
		};
		
		Clusterer kmeans = new KMedoids(targetClusterCount, 1000, dm);
		//Clusterer kmeans = new KMeans(targetClusterCount, 1000);
		Dataset[] clusters = kmeans.cluster(ds);
		
		if(debug){
			System.out.println("build debug cluster sizes");
			int i = 0;
			for(Dataset d : clusters){
				System.out.println(" cluster: " + (i++) + ", size: " + d.size());
			}
		}
		
		// assign class values in interval [0-clusters.size())
		int ci = 0;
		Dataset lds = new DefaultDataset();
		for(Dataset c : clusters){
			for(Instance i : c){
				i.setClassValue(new Integer(ci));
				lds.add(i);
			}
			ci++;
		}
		
		// build classifier based on assigned class values
		classifier = new SelfOptimizingLinearLibSVM();
		classifier.buildClassifier(lds);
		
		// set up rolling window for meta-classification analysis
		this.stackSize = stackSize;
		gestureCounts = new HashMap<double[], String>();
		clusterCount = clusters.length + 1;
		classificationCounts = new double[clusterCount];
		none_index = clusterCount - 1; // last index corresponds to no classification
		classificationStack = new LinkedList<Integer>();
		for(int index = 0; index < stackSize; index++){
			classificationStack.addFirst(none_index);
			classificationCounts[none_index] += 1 / (double)stackSize;
		}
	}
	
	public void mapGuesture(String guesture, List<List<Double>> data, boolean debug){
		double[] counts;
		
		counts = new double[clusterCount];
		gestureCounts.put(counts,guesture);
		for(int c = 0; c < clusterCount; c++){
			counts[c] = 0.0;
		}
		for(List<Double> datum : data){
			counts[classify(datum)] += 1.0 / (double)data.size();
		}
		
		if(debug){
			System.out.println("map guesture debug");
			System.out.println("guesture: " + guesture);
			for(int index = 0; index < counts.length; index++){
				System.out.println("  " + index + ": " + counts[index]);
			}
		}
	}
	
	public int classify(List<Double> data){
		Instance i = makeInstance(data);
		normalizer.filter(i);
		Object c = classifier.classify(i);
		if(c instanceof String && ((String)c).equals(none_classification)){
			return none_index;
		}
		return (int) c;
	}
	
	public String identify(List<Double> data){
		record(classify(data));
		return identify();
	}
	
	public String identify(){
		double[] mincount = null;
		double mindist = Double.MAX_VALUE;
		for(double[] ecount : gestureCounts.keySet()){
			double edist = distance(ecount);
			if(edist <= mindist){
				mindist = edist;
				mincount = ecount;
			}
			System.out.println("      " + gestureCounts.get(ecount) + " : " + edist);
		}
		return gestureCounts.get(mincount);
	}
	
	private void record(int c) {
		classificationStack.addLast(c);
		classificationCounts[c] += 1.0 / (double)stackSize;
		classificationCounts[classificationStack.removeFirst()] -= 1.0 / (double)stackSize;
	}
	
	private double distance(double[] actual){
		double dist = 0.0;
		for(int c = 0; c < clusterCount; c++){
			dist += Math.abs(actual[c] - classificationCounts[c]);
		}
		return dist;
	}
	
	private static Instance filter(Instance i, double low, double high){
		double xv;
		for(Integer x : i.keySet()){
			xv = Math.abs(i.get(x));
			if(xv <= low || xv >= high){
				i.put(x, 0.0);
			}
		}
		return i;
	}
	
	// expects n stringified numerical values
	private static Instance makeInstance(List<Double> data){
		double[] converted = new double[data.size()];
		for(int index = 0; index < converted.length; index++){
			converted[index] = data.get(index);
		}
		return filter(new DenseInstance(converted), 2.0, 500.0);
	}
	
	// expects a label name and stringified numerical values
	public static Instance makeTrainingInstance(String label, List<Double> data){
		double[] converted = new double[data.size()];
		for(int index = 0; index < converted.length; index++){
			converted[index] = data.get(index);
		}
		return new DenseInstance(converted, label);
	}
	
	public static void main(String[] args){
		GatewayServer gs = new GatewayServer(new Identifier());
		gs.start();
	}
}
