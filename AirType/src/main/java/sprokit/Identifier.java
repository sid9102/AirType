package sprokit;

import java.util.ArrayList;
import java.util.LinkedList;

import libsvm.SelfOptimizingLinearLibSVM;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.SOM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.filter.normalize.NormalizeMidrange;

public class Identifier {
	private NormalizeMidrange normalizer;
	private Classifier classifier;	
	private boolean debug;
	private LinkedList<String> classes;
	
	// instantiate with a newline separated list of stringified lists of training data, <true/false>, <true/false>
	public Identifier(String datalist, boolean debug, boolean labelled){
		this.debug = debug;
		init(datalist, labelled);
	}
	
	// pass a stringified list of unnormalized, unlabeled data, to get it's label
	public Object identify(String data){
		Instance i = makeInstance(data);
		normalizer.filter(i);
		String classification = (String) classifier.classify(i);
		
		if(debug){
			System.out.println("DEBUG: Identifier.identify");
			System.out.println("       -> " + classification);
		}
		
		return classification;
	}
	
	public Object identify(ArrayList<Integer> data){
		Instance i = makeInstance(data);
		normalizer.filter(i);
		String classification = (String) classifier.classify(i);
		
		if(debug){
			System.out.println("DEBUG: Identifier.identify");
			System.out.println("       -> " + classification);
		}
		
		return classification;
	}
	
	private void init(String datalist, boolean isLabelled){
		Dataset labelled;
		
		if(debug){
			System.out.println("DEBUG: identifier.init");
			System.out.println("       * calling datalist parser");
		}
		
		if(isLabelled){
			labelled = standardParse(datalist);
		}
		else{
			labelled = clusterParse(datalist);
		}
		
		if(debug){
			System.out.println("       * exited datalist parser");
			System.out.println("       -> building classifier");
		}
		
		// build classifier
		classifier = new SelfOptimizingLinearLibSVM();
		classifier.buildClassifier(labelled);
		
		if(debug){
			System.out.println("       done");
		}
	}
	
	private Dataset clusterParse(String datalist){
		if(debug){
			System.out.println("       -> reading data");
		}
		
		// read in data, convert into dataset
		Dataset cdata = new DefaultDataset();
		for(String l : datalist.split(System.getProperty("line.separator"))){
			cdata.add(makeInstance(l));
		}
		
		if(debug){
			System.out.println("       -> building normalizer");
		}
		
		// build the normalizer
		normalizer = new NormalizeMidrange();
		normalizer.build(cdata);
		
		if(debug){
			System.out.println("       -> normalizing data");
		}
		
		// normalize the data
		normalizer.filter(cdata);
		
		if(debug){
			System.out.println("       -> clustering data");
		}
		
		// cluster the data
		Clusterer clusterer = new SOM();
		Dataset[] clustered = clusterer.cluster(cdata);
		
		if(debug){
			System.out.println("           -> clusters found: " + clustered.length);
			System.out.println("       -> labeling the data");
		}
		
		// labelling data
		classes = new LinkedList<String>();
		Dataset labelled = new DefaultDataset();
		int c = 0;
		for(Dataset d : clustered){
			classes.add("class:"+c);
			for(Instance i : d){
				i.setClassValue(classes.getLast());
				labelled.add(i);
			}
			c++;
		}
		
		if(debug){
			System.out.println("           -> classes instantiated: ");
			for(String cl : classes){
				System.out.println("               '" + cl + "'");
			}
		}
		
		return labelled;
	}
	
	private Dataset standardParse(String datalist){
		if(debug){
			System.out.println("       -> reading data");
		}
		
		// read in data, convert into dataset
		Dataset cdata = new DefaultDataset();
		for(String l : datalist.split(System.getProperty("line.separator"))){
			cdata.add(makeTrainingInstance(l));
		}
		
		if(debug){
			System.out.println("       -> building normalizer");
		}
		
		// build the normalizer
		normalizer = new NormalizeMidrange();
		normalizer.build(cdata);
		
		if(debug){
			System.out.println("       -> normalizing data");
		}
		
		// normalize the data
		normalizer.filter(cdata);
		
		return cdata;
	}
	
	// expects n stringified numerical values
	private static Instance makeInstance(ArrayList<Integer> data){
		double[] converted = new double[data.size()];
		for(int index = 0; index < converted.length; index++){
			converted[index] = (double)data.get(index);
		}
		return new DenseInstance(converted);
	}
	
	// expects n stringified numerical values
	private static Instance makeInstance(String line){
		String[] sline = line.split(" ");
		double[] converted = new double[sline.length];
		for(int index = 0; index < sline.length; index++){
			try{
				converted[index] = Double.parseDouble(sline[index]);
			}
			catch(Exception x){
				converted[index] = 0;
				throw new IllegalArgumentException(sline[index] + " could not be cast to double");
			}
		}
		return new DenseInstance(converted);
	}
	
	// expects a label name and stringified numerical values
	public static Instance makeTrainingInstance(String line){
		String[] sline = line.split(" ");
		double[] converted = new double[sline.length-1];
		for(int index = 1; index < sline.length; index++){
			try{
				converted[index-1] = Double.parseDouble(sline[index]);
			}
			catch(Exception x){
				converted[index] = 0;
				throw new IllegalArgumentException("String could not be cast to double");
			}
		}
		return new DenseInstance(converted, sline[0]);
	}
}
