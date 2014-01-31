package co.sidhant.airtype.prediction;

import bullib.network.Cursor;
import bullib.network.Network;

public class StandardCursor extends Cursor<String>{

	public StandardCursor(Network<String> anetwork) {
		super(anetwork);
	}

	@Override
	public double advance(double arg0) {
		return arg0 * 2;
	}

}
