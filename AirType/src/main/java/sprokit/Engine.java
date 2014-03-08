package sprokit;

public class Engine {
	public static String[] LABELS = new String[]
			{"resting","leftIndex","leftMiddle","leftRing","leftPinky",
			 "rightIndex,","rightMiddle","rightRing","rightPinky"};
	
	private Automata automata;
	private Identifier identifier;
	
	// expects datalist in the form "<label> <number> <number> \n <label> <number> <number> \n ....."
	public void train(String datalist){
		identifier = new Identifier(datalist,"false","false");
		automata = new Automata(LABELS);
	}
	
	// expects data in the form "<number> <number> ....."
	public String getOutput(String data){
		String classification = (String) identifier.identify(data);
		return automata.cycle(classification);
	}
}
