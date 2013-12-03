package co.sidhant.airtype;

import android.util.Base64;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by sid9102 on 11/22/13.
 */
public class EncodedTrie
{
    // A base64 encoded string of all the words assigned to the nodes, indexed breadth first
    // These words are encoded with 0x01 through 0x1a assigned to a through z,
    // 0x1b and 0x1c refer to the beginning of a word fragment and the beginning of a complete word, respectively
    private String words;
    // A rank directory for the words string, for 0(1) lookup of the words
    private String wordRank;
    // A base64 encoded bitmap of children for each node,
    // 9 bits per node indicating whether a child exists or not, indexed breadth first
    private String trie;
    // A base64 encoded rank directory (http://people.eng.unimelb.edu.au/sgog/optimized.pdf, page 4)
    // for O(1) rank lookup on the trie string
    private String rankDir;

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

    // Convert a node's word into a binary representation,
    // 0x01 through 0x1a represent a through z, delimited through the use of 0x1b to indicate the
    // beginning of a word fragment and 0x1c to indicate the beginning of a full word
    private String nodeConverter(AirTrieNode node)
    {
        String curWord = node.getWord();
        String wordHex;

        // Set up word delimiting, also use this bit to determine if this is a word fragment or a full word
        if(node.isEndOfWord())
        {
            wordHex = "1c";
        }
        else
        {
            wordHex = "1b";
        }
        // Generate the hex representation of these letters
        for(int i = 0; i < curWord.length(); i++)
        {
            int curLetter = curWord.charAt(i) - 'a' + 1;
            String hexCode = Integer.toHexString(curLetter);
            if(hexCode.length() == 1)
            {
                hexCode = 0 + hexCode;
            }
            wordHex += hexCode;
        }

        int len = wordHex.length();
        // convert this into bytes
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(wordHex.charAt(i), 16) << 4)
                    + Character.digit(wordHex.charAt(i+1), 16));
        }

        String result = Base64.encodeToString(data, 0);
        return result;
    }

    //TODO finish the encoding process, figure out how to decode on base64 so decoding from base64 to bytes is not needed
}
