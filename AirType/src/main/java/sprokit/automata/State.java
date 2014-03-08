package sprokit.automata;
import java.util.LinkedList;

public class State<Output,Input>{
	private String name;
	private LinkedList<Path<Output,Input>> paths;
	
	public State(String n){
		name = n;
		paths = new LinkedList<Path<Output,Input>>();
	}
	
	public String getName(){
		return name;
	}
	
	public void addPath(Input i, Output o, Aggregator<Output> a, State<Output, Input> t){
		paths.add(new Path<Output,Input>(i,a,o,t));
	}
	
	public LinkedList<State<Output,Input>> accepted(Input input){
		LinkedList<State<Output,Input>> accepted = new LinkedList<State<Output,Input>>();
		for(Path<Output,Input> path : paths){
			if(path.accepts(input)){
				accepted.add(path.activate());
			}
		}
		return accepted;
	}
}