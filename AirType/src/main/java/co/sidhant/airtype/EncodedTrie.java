package co.sidhant.airtype;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by sid9102 on 11/22/13.
 */
public class EncodedTrie
{
    // A base64 encoded rank directory (http://people.eng.unimelb.edu.au/sgog/optimized.pdf, page 4)
    // for O(1) rank lookup on the trie string
    private String rankDir;
    // A base64 encoded string of all the words assigned to the nodes, indexed breadth first
    // These words are encoded with 0x01 through 0x1a assigned to a through z,
    // 0x1b and 0x1c refer to the end of a word fragment and the end of a complete word, respectively
    private String words;
    // A base64 encoded bitmap of children for each node,
    // 9 bits per node indicating whether a child exists or not, indexed breadth first
    private String trie;

    public int completion;

    public EncodedTrie(AirTrie trie)
    {
        LinkedList<AirTrieNode> nodeQueue = new LinkedList<AirTrieNode>();
        AirTrieNode curNode = trie.root;
        nodeQueue.add(curNode);
        AirTrieNode curChild;
        //Breadth first search of the trie, list every string
        while(!nodeQueue.isEmpty())
        {
            curNode = nodeQueue.removeFirst();
            for(int i = 0; i < 9; i++)
            {
                curChild = curNode.getChildPrecise(i);
                if(curChild != null)
                {
                    nodeQueue.add(curChild);
                }
            }

        }
        completion = 25;
        //TODO: encode string
    }
}
