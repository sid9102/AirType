package sprokit.automata;
import java.util.LinkedList;

public class Aggregator<Output> {
	private LinkedList<Output> output;
	private boolean debug;
	
	public Aggregator(boolean d){
		output = new LinkedList<Output>();
		debug = d;
	}
	
	public synchronized void append(Output o){
		if(debug){
			System.out.println("Aggregator output debug: appending");
			System.out.println("     '" + o.toString() + "'");
		}
		
		output.addLast(o);
	}
	
	public synchronized boolean hasNext(){
		return output.size() > 0;
	}
	
	public synchronized Output consume(){
		return output.removeFirst();
	}
}
