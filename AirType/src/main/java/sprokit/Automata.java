package sprokit;

import java.util.LinkedList;

import sprokit.automata.Aggregator;
import sprokit.automata.Navigator;

public class Automata {
	private Aggregator<String> aggregator;
	private Navigator<String,String> navigator;
	
	public Automata(String[] labels){
		installDefaultAutomata(labels, false);
	}
	
	public void installDefaultAutomata(String[] labels, boolean debug){
		aggregator = new Aggregator<String>(debug);
		navigator = new Navigator<String,String>(aggregator, debug);
		
		LinkedList<String> states = new LinkedList<String>();
		
		String nothing = labels[0];
		
		// create the states
		for(String label : labels){
			states.add(label);
		}
		
		// add them to the automata
		for(String state : states){
			navigator.addState(state);
		}
		
		// if the the guesture does not change, do nothing
		for(String state : states){
			navigator.addPath(state, state, "", state);
		}
		
		//if the guesture does change, output the change, and move to that state
		for(String from: states){
			for(String to: states){
				if(!from.equals(to)){
					if(!to.equals(nothing)){
						navigator.addPath(from, to, to, to);
					}
					else{
						navigator.addPath(from, to, "", to);
					}
				}
			}
		}
		
		navigator.setStartState(nothing);
	}
	
	public String cycle(String input){		
		navigator.input(input);
		
		String all = "";
		while(aggregator.hasNext()){
			all += aggregator.consume();
		}
		
		return all;
	}
}
