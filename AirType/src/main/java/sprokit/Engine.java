package sprokit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class Engine {
	public static String[] LABELS = new String[]
			{"resting", "space", "left", "right",
			 "leftIndex", "leftMiddle", "leftRing", "leftPinky",
			 "rightIndex,", "rightMiddle", "rightRing","rightPinky"};
	
	private Automata automata;
	private Identifier identifier;
	
	// expects datalist in the form "<label> <number> <number> \n <label> <number> <number> \n ....."
	public void train(List<List<Double>> data, boolean di, boolean ai){
		automata = new Automata(LABELS, ai);
	}
	
	// expects data in the form "<number> <number> ....."
	public String process(List<Double> data){
		String classification = (String) identifier.identify(data);
		return automata.cycle(classification);
	}
	
	public static Object deserialize(File f) throws Exception{
		FileInputStream fi = new FileInputStream(f);
		ObjectInputStream oi = new ObjectInputStream(fi);
		Object retobj = oi.readObject();
		oi.close();
		return retobj;
	}
	
	public static void serialize(Object t, File f) throws Exception{
		FileOutputStream fo = new FileOutputStream(f);
		ObjectOutputStream oo = new ObjectOutputStream(fo);
		oo.writeObject(t);
		oo.close();
	}
}
