package prediction.test;

import bullib.network.Cursor;
import bullib.network.Network;

public class SensorCursor extends Cursor<State> {

	public SensorCursor(Network<State> anetwork) {
		super(anetwork);
	}

	@Override
	public double advance(double arg0) {
		return arg0 * 2.0f;
	}

}
