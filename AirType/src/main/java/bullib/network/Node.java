package bullib.network;

import java.io.Serializable;

// An object used to encapsulate T values for use in Network. Equality and hashing consider the id alone
public class Node<T extends Serializable> implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public final String id;
	public T value;

	public Node(String aid, T avalue){
		id = aid;
		value = avalue;
	}

	@Override public int hashCode(){
		return id.hashCode();
	}

	@Override public boolean equals(Object other){
		if(!(other instanceof Node<?>)){
			return false;
		}
		return ((Node<?>)other).id.equals(this.id);
	}

	@Override public String toString(){
		return id;
	}
}