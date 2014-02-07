package prediction.test;

import java.io.File;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


import bullib.network.Weight;
import bullib.utility.Utility;
import bullib.network.Network;
import bullib.network.Cursor;


public class AssociationEngine {
	private KeypressNetwork network;
	private SensorCursor cursor;
	private static Random random = new Random();
	
	public AssociationEngine(){
		network = new KeypressNetwork(.75);
		cursor = new SensorCursor(network);
	}
	
	public void reset(String characters){
		int numchars = characters.length();
		int numstates = cursor.getHistory().size();
		double placementRate = (double)numchars / (double)numstates;
		double distance = random.nextDouble(); // start 0 node with character or not?
		int charindex = 0;
		for(State s : cursor.getHistory()){
			distance += placementRate;
			if(distance >= 1.0){
				distance -= 1.0;
				s.value = characters.charAt(charindex++);
			}
		}
		cursor.reset();
	}
	
	public void place(Data data) throws Exception{
		cursor.place(new State(cursor.getLocation()+"-->"+data.hashCode(), null), data.getValue());
	}
	
	public void move(State destination) throws Exception{
		cursor.move(destination);
	}
	
	public List<Entry<State, Weight>> adjacent(){
		List<Entry<State,Weight>> list = new LinkedList<Entry<State,Weight>>();
		list.addAll(network.getOutgoingLinks(cursor.getLocation()));
		Collections.sort(list, Utility.entryComparator);
		return list;
	}
	
	public void load(File f) throws Exception{
		network = (KeypressNetwork)Utility.deserialize(f);
		cursor = new SensorCursor(network);
	}
	
	public void save(File f) throws Exception{
		Utility.serialize(network,f);
	}
}
