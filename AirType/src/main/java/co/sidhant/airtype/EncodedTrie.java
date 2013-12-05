package co.sidhant.airtype;

import android.util.Base64;

import java.util.BitSet;
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

    // A bitset of the encoded words
    private BitSet wordBits;

    // The current index at which to encode, in the bitset
    private int wordBitIndex;

    private int completion;

    public static SettingsActivity.TrieGenTask trieGenTask;

    public void makeEncodedTrie(AirTrie trie)
    {
        LinkedList<AirTrieNode> nodeQueue = new LinkedList<AirTrieNode>();
        AirTrieNode curNode = trie.root;
        nodeQueue.add(curNode);
        AirTrieNode curChild;
        //TODO: fix hardcoded number
        float totalNodes = 1499062;
        float currentNode = 0;
        wordBitIndex = 0;
        completion = 50;
        //Breadth first search of the trie, list every string
        while(!nodeQueue.isEmpty())
        {
            currentNode++;
            curNode = nodeQueue.removeFirst();
            for(int i = 0; i < 9; i++)
            {
                curChild = curNode.getChildPrecise(i);
                if(curChild != null)
                {
                    nodeQueue.add(curChild);
                }
            }
            int curCompletion = (int) ((currentNode / totalNodes) * 25) + 50;
            if(curCompletion > completion)
            {
                completion = curCompletion;
                trieGenTask.onProgressUpdate(completion);
            }
            encodeNode(curNode);
        }
        completion = 75;
        //TODO: encode string
    }

    // Convert a node's word into a binary representation,
    // 0x01 through 0x1a represent a through z, delimited through the use of 0x1b to indicate the
    // beginning of a word fragment and 0x1c to indicate the beginning of a full word
    private void encodeNode(AirTrieNode node)
    {
        if(node.isEndOfWord())
        {
            //encode the word header, complete word, so 11100
            wordBits.set(wordBitIndex, wordBitIndex + 2, true);
        }
        else
        {
            //encode the word header, complete word, so 11011
            wordBits.set(wordBitIndex, wordBitIndex + 1, true);
            wordBits.set(wordBitIndex + 3, wordBitIndex + 4, true);
        }
        wordBitIndex += 5;
        String curWord = node.getWord();
        // Encode the letters one by one, 5 bits each
        for(int i = 0; i < curWord.length(); i++)
        {
            int letter = curWord.charAt(i) - 'a' + 1;
            String letterBinary = Integer.toBinaryString(letter);
            for(int j = 0; j < 5; j++)
            {
                if(letterBinary.charAt(j) == '1')
                    wordBits.set(wordBitIndex, true);
                wordBitIndex++;
            }
        }
    }

    //TODO finish the encoding process, figure out how to decode on base64 so decoding from base64 to bytes is not needed
}
