package bullib.librarian;

import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.io.Serializable;

public abstract class Librarian<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private HashMap<T, Double> definedWords;
	private HashMap<T, Double> randomWords;
	private transient Random random;

	public Librarian(){
		definedWords = new HashMap<T, Double>();
		randomWords = new HashMap<T, Double>();
		random = new Random();
	}

	public abstract double definePhrase(List<T> phrase);
	
	protected abstract List<T> getAssociations(T word);

	public synchronized double defineWord(T word){
		double value = -1.0;
		if(definedWords.containsKey(word)){
			value = definedWords.get(word);
		}
		else{
			value = dictionaryHash(word);
			definedWords.put(word, value);
			randomWords.remove(word);
		}
		return value;
	}
	
	private double dictionaryHash(T word){
		double value = 0.0;
		List<T> associations = getAssociations(word);
		for(T association : getAssociations(word)){
			value += definitionHash(association) / (associations.size() + 1.0);
		}
		return value;
	}

	private double definitionHash(T word){
		double value = 0.0;
		List<T> associations = getAssociations(word);
		for(T association : associations){
			if(definedWords.containsKey(association)){
				value += definedWords.get(association) / (associations.size() + 1.0);
			}
			else if(randomWords.containsKey(word)){
				value += randomWords.get(word) / (associations.size() + 1.0);
			}
			else{
				double rand = random.nextDouble() * Double.MAX_VALUE;
				randomWords.put(word, rand);
				value += rand / (associations.size() + 1.0);
			}
		}
		return value;
	}
}