package co.sidhant.airtype;

/**
 * Created by sid9102 on 11/19/13.
 */
public class AirTrie {
    //extra root for succinct compression
    private AirTrieNode root0;

    public AirTrieNode root;


    public AirTrie(){

        root0 = new AirTrieNode();
        root = new AirTrieNode();
        root0.setChild(root, 0);
    }
}
