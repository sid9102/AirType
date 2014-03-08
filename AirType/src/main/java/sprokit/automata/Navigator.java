package sprokit.automata;
import java.util.HashMap;
import java.util.LinkedList;

public class Navigator<Output, Input>{
	private HashMap<String, State<Output, Input>> states;
	private LinkedList<State<Output, Input>> locations;
	private Aggregator<Output> aggregator;
	private String start;
	private boolean debug;
	
	public Navigator(Aggregator<Output> a, boolean d){
		states = new HashMap<String, State<Output, Input>>();
		locations = new LinkedList<State<Output, Input>>();
		aggregator = a;
	}
	
	public void setStartState(String s){
		start = s;
		locations.add(states.get(start));
	}
	
	public void reset(){
		locations = new LinkedList<State<Output, Input>>();
		locations.add(states.get(start));
	}
	
	public void addState(String s){
		states.put(s, new State<Output,Input>(s));
	}
	
	public void addPath(String f, Input i, Output o, String t){
		states.get(f).addPath(i, o, aggregator, states.get(t));
	}
	
	public void input(Input input){
		LinkedList<State<Output, Input>> newlocations = new LinkedList<State<Output,Input>>();
		for(State<Output,Input> state : locations){
			newlocations.addAll(state.accepted(input));
		}
		
		if(debug){
			System.out.println("Navigator cycle debug: current active states");
			for(State<Output,Input> state : newlocations){
				System.out.println("     '"+state.getName()+"'");
			}
		}
		
		locations = newlocations;
	}
}