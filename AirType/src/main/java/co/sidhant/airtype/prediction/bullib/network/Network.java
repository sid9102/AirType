package bullib.network;

import bullib.network.Weight;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

// Representation of a network accessed via unique generic nodes. No error checking, exceptions fall through.
public abstract class Network<T extends Serializable> implements Serializable{
	private static final long serialVersionUID = 1L;
	public final T root;
	private int maxlinks;
	private HashMap<T, HashMap<T, Weight>> graph;
	private HashMap<T, HashSet<T>> callback;

	public Network(T aroot, int max){
		root = aroot;
		maxlinks = max;
		graph = new HashMap<T, HashMap<T, Weight>>();	
		callback = new HashMap<T, HashSet<T>>();
		
		graph.put(root, new HashMap<T, Weight>());
		callback.put(root, new HashSet<T>());
	}

	// implement in a derived class to adjust the collision function for path weights.
	protected abstract void collide(Weight weight, double addition);
	
	// implement in a derived class to choose which connection to remove
	protected abstract Collection<T> prune(Set<Map.Entry<T, Weight>> link);

	public synchronized boolean contains(T location){
		return graph.containsKey(location);
	}
	
	public synchronized boolean contains(T location, T destination){
		if(!contains(location)){
			return false;
		}
		return graph.get(location).containsKey(destination);
	}
	
	public synchronized Set<T> getNodes(){
		return graph.keySet();
	}
	
	public synchronized Set<T> getIncomingLinks(T location){
		return callback.get(location);
	}

	public synchronized Set<Map.Entry<T, Weight>> getOutgoingLinks(T location){
		return graph.get(location).entrySet();
	}

	public synchronized void addLink(T location, T destination, double addition) throws Exception{
		// Grab the set of nodes adjacent to location. Add location and instantiate the set if it doesn't exist.
		HashMap<T, Weight> adjacent;
		if(graph.containsKey(location)){
			adjacent = graph.get(location);
		}
		else{
			adjacent = new HashMap<T, Weight>();
			graph.put(location, adjacent);
		}

		// Grab and modify the weight for the path to the destination node. Add destination and instantiate the weight if it doesn't exist.
		if(adjacent.containsKey(destination)){
			Weight weight = adjacent.get(destination);
			collide(weight, addition);
			checkWeight(weight, addition, location);
		}
		else{
			checkPrune(adjacent, location);
			adjacent.put(destination, new Weight(addition));
		}
		
		// Add the destination and its map of links if it doesn't exist.
		if(!graph.containsKey(destination)){
			graph.put(destination, new HashMap<T, Weight>());
		}
		
		registerCallback(location, destination);
	}
	
	private void checkPrune(HashMap<T, Weight> adjacent, T location){
		if(adjacent.size() + 1 > maxlinks){
			for(T r : prune(getOutgoingLinks(location))){
				adjacent.remove(r);
				callback.get(r).remove(location);
				// if the node is alienated, remove it
				if(callback.get(r).size() == 0){
					graph.remove(r);
					callback.remove(r);
				}
			}
		}
	}
		
	private void checkWeight(Weight weight, double addition, T location) throws Exception{
		// salvage weight, and scale all other weights
		if(weight.value == Double.POSITIVE_INFINITY){
			weight.value = Double.MAX_VALUE;
			for(Weight w : graph.get(location).values()){
				w.value /= 2;
				if(w.value == Double.NEGATIVE_INFINITY){
					w.value = 0.0;
				}
			}
			// attempt the collision again
			collide(weight, addition);
		}
		else if(weight.value == Double.NEGATIVE_INFINITY){
			weight.value = Double.MIN_VALUE;
			for(Weight w : graph.get(location).values()){
				w.value /= 2;
				if(w.value == Double.NEGATIVE_INFINITY){
					w.value = 0.0;
				}
			}
			// attempt the collision again
			collide(weight, addition);
		}
		
		// ensure the weight is still valid
		if(weight.value == Double.POSITIVE_INFINITY || weight.value == Double.NEGATIVE_INFINITY || weight.value == Double.NaN){
			throw new Exception("Weight "+weight+" at location "+location+" failed it's validity check. The previous collision made it invalid");
		}
	}
	
	private void registerCallback(T location, T destination){
		HashSet<T> back;
		if(callback.containsKey(destination)){
			back = callback.get(destination);
			back.add(location);
		}
		else{
			back = new HashSet<T>();
			back.add(location);
			callback.put(destination, back);
		}
		if(!callback.containsKey(location)){
			callback.put(location, new HashSet<T>());
		}
	}
}
