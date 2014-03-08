package sprokit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Engine {
	public static String[] LABELS = new String[]
			{"resting", "space", "left", "right",
			 "leftIndex", "leftMiddle", "leftRing", "leftPinky",
			 "rightIndex,", "rightMiddle", "rightRing","rightPinky"};
	
	private Automata automata;
	private Identifier identifier;
	
	public Engine(String f) throws IOException{
		RandomAccessFile rf = new RandomAccessFile(new File(f), "r");
		byte[] bytes = new byte[(int)rf.length()];
		rf.read(bytes);
		train(new String(bytes));
		rf.close();
	}
	
	// expects datalist in the form "<label> <number> <number> \n <label> <number> <number> \n ....."
	public void train(String datalist){
		identifier = new Identifier(datalist,"true","true");
		automata = new Automata(LABELS);
	}
	
	// expects data in the form "<number> <number> ....."
	public String send(String data){
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
	
	public static void main(String[] args) throws Exception{
		Engine e = new Engine(args[0]);
		
		Scanner ui = new Scanner(System.in);
		while(ui.hasNext()){
			System.out.println(e.send(ui.nextLine()));
		}
		
		ui.close();
	}
}
