package co.sidhant.airtype;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by sid9102 on 11/22/13.
 */
public class TrieEncoder
{
    public static int completion;
    public static String encodeData(AirTrie trie)
    {
        completion = 50;
        LinkedList<AirTrieNode> nodeQueue = new LinkedList<AirTrieNode>();
        AirTrieNode curNode = trie.root;
        nodeQueue.add(curNode);
        String result = "";
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
            result += curNode.getWord();
        }
        completion = 75;
        //TODO: encode string

        return result;
    }
}
