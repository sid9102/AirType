package bullib.network;

import bullib.network.Network;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

// Used to iterate, navigate, and manage a Network
public abstract class Cursor<T extends Serializable> {
	// shortest path, can also be used to find the shortest path to express an idea.
	// find cycles
	// remember path and backpropagate influences on weight

	private Network<T> network;
	
	private T location;
	private HashMap<T, Double> encounters;
	private LinkedList<T> history;

	public Cursor(Network<T> anetwork){
		network = anetwork;
		reset();
	}
	
	// Implement in a derived class to adjust the speed of counter advance.
	public abstract double advance(double count);

	public void reset(){
		location = network.root;
		encounters = new HashMap<T, Double>();
		history = new LinkedList<T>();
	}

	public T getLocation(){
		return location;
	}

	public double getCount(T word){
		return encounters.get(word);
	}

	public LinkedList<T> getHistory(){
		return history;
	}

	public void move(T next) throws Exception{
		if(!network.contains(location, next)){
			throw new Exception("'"+next+"' is not adjacent to location, still performing the move.");
		}
		location = next;
		record();
	}

	public void place(T destination, double weight) throws Exception{
		network.addLink(location, destination, weight);
	}

	public T moveClosest(double target){
		T minword = null;
		double mindist = Double.MAX_VALUE;
		double distance;
		for(Map.Entry<T, Weight> entry : network.getOutgoingLinks(location)){
			distance = Math.abs(target - entry.getValue().value) + encounters.get(entry.getKey());
			if(distance <= mindist){
				mindist = distance;
				minword = entry.getKey();
			}
		}
		location = minword;
		record();
		return minword;
	}

	public void placeAndMove(T destination, double weight) throws Exception{
		place(destination, weight);
		location = destination;
		record();
	}

	private void record(){
		history.addLast(location);
		
		double count = 1.0;
		if(encounters.containsKey(location)){
			count = encounters.get(location);
		}
		encounters.put(location, advance(count));
	}
}