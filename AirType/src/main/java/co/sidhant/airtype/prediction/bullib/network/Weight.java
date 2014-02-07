package bullib.network;

import java.io.Serializable;

// Represents the weight of a path, has fields to represent the value and the number of additions.
public class Weight implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public double size;
	public double value;

	public Weight(double avalue){
		size = 1.0;
		value = avalue;
	}

	public Weight(){
		size = 0.0;
		value = 0.0;
	}
	
	public int compareTo(Weight other){
		return (int)this.value - (int)other.value;
	}

	// suggested way to perform a collision in a derived Network class
	public void equicollide(double addition){
		value = (value * size++ + addition) / size;
	}
}