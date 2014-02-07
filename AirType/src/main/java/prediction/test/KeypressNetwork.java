package prediction.test;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import bullib.network.Network;
import bullib.network.Weight;

public class KeypressNetwork extends Network<State>{
	private static final long serialVersionUID = 1L;
	private double addfraction;
	
	public KeypressNetwork(double afrac) {
		super(new State("root", null), Integer.MAX_VALUE);
		
		addfraction = afrac;
		if(addfraction < 0.0 || addfraction > 1.0){
			throw new IllegalArgumentException("Addition fraction must be between 0 and 1, inclusive");
		}
	}

	@Override
	protected void collide(Weight arg0, double arg1) {
		arg0.value = (arg0.value / arg0.size++) + arg1 / arg0.size;
	}

	@Override
	protected Collection<State> prune(Set<Entry<State, Weight>> arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
