package prediction;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Pattern;

import bullib.utility.Utility;
import bullib.network.Cursor;
import bullib.network.Network;
import bullib.network.Weight;

public class NextWord{
	private Network<String> network;
	private Cursor<String> cursor;
	
	public NextWord(){
		network = new WordPairNetwork();
		cursor = new StandardCursor(network);
	}
	
	// moves to before the beginning of a sentence
	public void reset(){
		cursor.reset();
	}
	
	// adds a link from your current location to word
	public void place(String word, double frequency) throws Exception{
		// throws if the network is unable to resolve the weight, this is unlikely to impossible
		cursor.place(word, frequency);
	}
	
	// moves to picked word
	public void move(String picked) throws Exception{
		// throws if the picked word is not in the set of adjacent words
		cursor.move(picked);
	}
	
	// returns a sorted list of adjacent words and their weight (larger is more probable)
	public List<Entry<String, Weight>> adjacent(){
		List<Entry<String,Weight>> list = new LinkedList<Entry<String,Weight>>();
		list.addAll(network.getOutgoingLinks(cursor.getLocation()));
		Collections.sort(list, Utility.entryComparator);
		return list;
	}
	
	// loads the network from a file
	public void load(File f) throws Exception{
		network = (WordPairNetwork)Utility.deserialize(f);
		cursor = new StandardCursor(network);
	}
	
	// saves the network to a file
	public void save(File f) throws Exception{
		Utility.serialize(network,f);
	}
	
	// trains the network on a large body of text
	public void train(File f) throws Exception{
		Scanner words;
		Scanner sentences = new Scanner(Utility.load(f));
		while(sentences.hasNext()){
			cursor.reset();
			words = new Scanner(sentences.nextLine());
			String from = words.next();
			double value = Double.parseDouble(words.next());
			String to = words.next();
			cursor.place(from,0.0);
			cursor.move(from);
			cursor.place(to,value);
		}
		
		sentences.close();
	}
	
//	public void runTests() throws Exception{
//		System.out.println("Adding A, B, B");
//		place("A");
//		place("B");
//		place("B");
//		System.out.println("    Passed? "+(network.contains("A") && network.contains("B")));
//
//		System.out.println("Move A");
//		move("A");
//		System.out.println("    Passed? "+(cursor.getLocation().equals("A")));
//		reset();
//
//		System.out.println("Move C throws exception");
//		boolean e = false;
//		try{
//			move("C");
//		}
//		catch(Exception x){
//			e = true;
//		}
//		System.out.println("    Passed? "+(e));
//		reset();
//
//		System.out.println("Link A, B");
//		move("A");
//		place("B");
//		System.out.println("    Passed? "+(network.contains("A","B")));
//		reset();
//
//		System.out.println("Link A, C");
//		move("A");
//		place("C");
//		System.out.println("    Passed? "+(network.contains("A","C") && network.contains("C")));
//		reset();
//
//		System.out.println("Save and load");
//		save(new File("tst.nwn"));
//		load(new File("tst.nwn"));
//		System.out.println("    Passed? "+
//				(network.contains("A") && network.contains("B") && network.contains("C")
//				 && network.contains("A","B") && network.contains("A","C"))
//		);
//	}
}