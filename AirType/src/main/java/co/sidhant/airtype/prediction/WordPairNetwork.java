package co.sidhant.airtype.prediction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import bullib.network.Network;
import bullib.network.Weight;
import bullib.utility.Utility;

public class WordPairNetwork extends Network<String>{
	private static final long serialVersionUID = 1L;
	

	public WordPairNetwork() {
		// "root" is the root node's value
		// 25 is the number of outgoing links that prompt a call to prune
		super("root", 25);
	}

	@Override
	protected void collide(Weight arg0, double arg1) {
		arg0.value += arg1;
	}

	@Override
	protected Collection<String> prune(Set<Entry<String, Weight>> arg0) {
		HashSet<String> removals = new HashSet<String>();
		LinkedList<Entry<String,Weight>> links = new LinkedList<Entry<String,Weight>>();
		links.addAll(arg0);
		Collections.sort(links, Utility.entryComparator);
		for(int index = 0; index < 5; index++){
			removals.add(links.get(index).getKey());
		}
		return removals;
	}

}
