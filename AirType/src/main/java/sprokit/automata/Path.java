package sprokit.automata;

public class Path<Output,Input> {
	private Input input;
	private Aggregator<Output> aggregator;
	private Output output;
	private State<Output,Input> to;
	
	public Path(Input i, Aggregator<Output> a, Output o, State<Output,Input> t) {
		input = i;
		aggregator = a;
		output = o;
		to = t;
	}
	
	public boolean accepts(Input potential){
		return input.equals(potential);
	}
	
	public State<Output,Input> activate(){
		aggregator.append(output);
		return to;
	}
}