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
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.filter.normalize.NormalizeMidrange;
import py4j.GatewayServer;

public class Identifier {
	private NormalizeMidrange normalizer;
	private Classifier classifier;
	private HashMap<String, double[]> guestureCounts;
	private int clusterCount;
	private int none_index;
	private String none_classification = "None";
	
	// active variables
	private LinkedList<Integer> classificationStack;
	private double[] classificationCounts;
	
	public void build(int stackSize, int targetClusterCount, List<List<Double>> data){
		// convert to dataset
		Dataset ds = new DefaultDataset();
		for(List<Double> d : data){
			ds.add(makeInstance(d));
		}
		
		// build normalizer and normalize data
		normalizer = new NormalizeMidrange();
		normalizer.build(ds);
		normalizer.filter(ds);
		
		// cluster data
		Clusterer kmeans = new KMeans(targetClusterCount, 1000);
		Dataset[] clusters =  kmeans.cluster(ds);
		
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
		guestureCounts = new HashMap<String, double[]>();
		clusterCount = clusters.length + 1;
		classificationCounts = new double[clusterCount];
		none_index = clusterCount - 1; // last index corresponds to no classification
		classificationStack = new LinkedList<Integer>();
		for(int index = 0; index < stackSize; index++){
			classificationStack.addFirst(none_index);
		}
	}
	
	public void mapGuesture(String guesture, List<List<Double>> data){
		double[] counts;
		
		if(guestureCounts.containsKey(guesture)){
			counts = guestureCounts.get(guesture);
			for(List<Double> datum : data){
				int c = classify(datum);
				counts[c] = counts[c] + (1.0 / (double)data.size());
			}
			for(int c = 0; c < counts.length; c++){
				counts[c] /= 2;
			}
		}
		else{
			counts = new double[clusterCount];
			guestureCounts.put(guesture,counts);
			for(int c = 0; c < clusterCount; c++){
				counts[c] = 0.0;
			}
			for(List<Double> datum : data){
				counts[classify(datum)] =  1.0 / (double)data.size();
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
		String guesture = "None";
		double gdist = Double.MAX_VALUE;
		for(String pg : guestureCounts.keySet()){
			double pgdist = distance(guestureCounts.get(pg));
			if(pgdist <= gdist){
				gdist = pgdist;
				guesture = pg;
			}
			System.out.println("      " + pg + " : " + pgdist);
		}
		return guesture;
	}
	
	private void record(int c){
		classificationStack.addLast(c);
		classificationCounts[c] += 1.0;
		classificationCounts[classificationStack.removeFirst()] -= 1.0;
	}
	
	private double distance(double[] actual){
		double dist = 0.0;
		for(int c = 0; c < clusterCount; c++){
			dist += Math.abs(actual[c] - classificationCounts[c]);
		}
		return dist;
	}
	
	// expects n stringified numerical values
	private static Instance makeInstance(List<Double> data){
		double[] converted = new double[data.size()];
		for(int index = 0; index < converted.length; index++){
			converted[index] = data.get(index);
		}
		return new DenseInstance(converted);
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
