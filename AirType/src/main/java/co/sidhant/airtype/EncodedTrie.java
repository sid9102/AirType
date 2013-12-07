package co.sidhant.airtype;

import java.util.BitSet;
import java.util.LinkedList;

/**
 * Created by sid9102 on 11/22/13.
 */
public class EncodedTrie
{
    // A bitset of all the words assigned to the nodes, indexed breadth first
    // These words are encoded with 0x01 through 0x1a assigned to a through z,
    // 0x1b and 0x1c refer to the beginning of a word fragment and the beginning of a complete word, respectively
    private BitSet wordBits;
    // A rank directory for the words string, for 0(1) lookup of the words
    private BitSet wordRank;
    // A bitSet of children for each node,
    // 9 bits per node indicating whether a child exists or not, indexed breadth first
    private BitSet trieBits;
    // A base64 encoded rank directory (http://people.eng.unimelb.edu.au/sgog/optimized.pdf, page 4)
    // for O(1) rank lookup on the trie string
    private BitSet trieRank;

    // The current index at which to encode, in the bitsets
    private int wordBitIndex;
    private int trieBitIndex;

    private int completion;

    public static SettingsActivity.TrieGenTask trieGenTask;

    public void makeEncodedTrie(AirTrie trie)
    {
        //TODO: use producer consumer pattern on this queue to speed up encoding
        LinkedList<AirTrieNode> nodeQueue = new LinkedList<AirTrieNode>();
        AirTrieNode curNode = trie.root;
        nodeQueue.add(curNode);
        AirTrieNode curChild;
        //TODO: fix hardcoded number
        float totalNodes = 1500000;
        float currentNode = 0;

        wordBitIndex = 0;
        trieBitIndex = 0;
        wordBits = new BitSet();
        trieBits = new BitSet();

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
            encodeNodeWord(curNode);
            encodeNodeChildren(curNode);
        }
        completion = 75;
    }

    // Convert a node's word into a binary representation,
    // 0x01 through 0x1a represent a through z, delimited through the use of 0x1b to indicate the
    // beginning of a word fragment and 0x1c to indicate the beginning of a full word
    private void encodeNodeWord(AirTrieNode node)
    {
        if(node.isEndOfWord())
        {
            //encode the word header, complete word, so 11100
            wordBits.set(wordBitIndex, wordBitIndex + 2);
        }
        else
        {
            //encode the word header, complete word, so 11011
            wordBits.set(wordBitIndex, wordBitIndex + 1);
            wordBits.set(wordBitIndex + 3, wordBitIndex + 4);
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
                    wordBits.set(wordBitIndex);
                wordBitIndex++;
            }
        }
    }

    //Encode a node's children, 1 if a child exists, 0 if not
    private void encodeNodeChildren(AirTrieNode node)
    {
        for(int i = 0; i < 9; i++)
        {
            if(node.getChildPrecise(i) != null)
            {
                trieBits.set(trieBitIndex);
            }
            trieBitIndex++;
        }
    }

    // Generate a rank directory for the given BitSet
    // A rank directory is a directory of the number of 1's until that point in a BitSet
    // useful for constant time searching of a byte array
    private BitSet generateRankDirectory(BitSet bitSet)
    {
        BitSet rankDir = new BitSet();
        long rank = 0;

        // We're allocating 64 bits for a superblock header,
        // and then another 64, for 8 8-bit data blocks.
        // This means that each data block is a jump of 256 bits in the input BitSet,
        // and each superblock is (1 + 8) * 256 bits, or 2304 bits
        for(long i = 0; i < bitSet.length(); i+= 256)
        {
            
        }

        return rankDir;
    }
}
