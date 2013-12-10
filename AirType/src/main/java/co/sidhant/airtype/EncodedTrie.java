package co.sidhant.airtype;

import java.util.BitSet;
import java.util.LinkedList;

/**
 * Created by sid9102 on 11/22/13.
 */
public class EncodedTrie
{
    //TODO: use openBitSet instead, http://lucene.apache.org/core/3_0_3/api/core/org/apache/lucene/util/OpenBitSet.html

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
        trieGenTask.onProgressUpdate(completion);
        // Generate the rank directory for the trie bitset
        trieRank = generateRankDirectory(trieBits);
        completion = 85;
        trieGenTask.onProgressUpdate(completion);

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
        int superBlockIndex = 0;
        long rank = 0;

        // We're allocating 64 bits for a superblock header,
        // and then another 64, for 7 9-bit data blocks. A superblock is therefore 128 bits in size.
        // This means that each data block is a jump of 256 bits in the input BitSet,
        // and each superblock is (1 + 7) * 256 bits, or 2048 bits
        int i = 0;
        while(i < bitSet.length())
        {
            // Superblock header
            long[] header = new long[1];
            header[0] = rank;
            // This bitset maintains the bits for the 7 data blocks
            BitSet dataBits = new BitSet();
            // This loop runs once per data block
            for(int j = 0; j < 7; j++)
            {
                long oldValue = rank;
                // This loop updates the rank 256 times, traversing the size of a data block
                for(int k = 0; k < 256; k++)
                {
                    if(bitSet.get(i))
                    {
                        rank++;
                    }
                    i++;
                }
                // Encode each data block
                int block = (int) (rank - oldValue);
                int bit = 1;
                for(int k = 0; k < 9; k++)
                {
                    if((bit & block) != 0)
                    {
                        dataBits.set(k + (j * 9));
                    }
                    bit <<= 1;
                }
            }

            //TODO: remove debugging assert
            if(dataBits.length() >= 64)
            {
                throw new AssertionError("oh snap that shit ain't right");
            }

            BitSet headerBits = valueOf(header);

            // Insert this superblock into the rank directory
            for(int j = 0; j < 128; j++)
            {
                if(j < 64)
                {
                    if(headerBits.get(j))
                    {
                        rankDir.set(j + (superBlockIndex * 128));
                    }
                }
                else
                {
                    if(dataBits.get(j - 64))
                    {
                        rankDir.set(j + (superBlockIndex * 128));
                    }
                }
            }

            // Update rank for the next superblock header
            for(int k = 0; k < 256; k++)
            {
                if(bitSet.get(i))
                {
                    rank++;
                }
                i++;
            }
            superBlockIndex++;
        }

        return rankDir;
    }

    // Returns a bitset from a provided long[]
    private BitSet valueOf(long[] longs)
    {
        BitSet result = new BitSet();
        for(int i = 0; i < longs.length; i++)
        {
            long bit = 1;
            for(int j = 0; j < 64; j++)
            {
                // Check the bit at j
                if((bit & longs[i]) != 0)
                {
                    result.set(j + (i * 64));
                }
                bit <<= 1;
            }
        }
        return result;
    }

    // Produces a long[] from a given bitset
    private long[] bitSetToLong(BitSet bitSet)
    {
        // figure out how many longs we need, size will always be some multiple of 64
        int size = bitSet.size() / 64;
        long[] result = new long[size];
        for(int i = 0; i < size; i++)
        {
            long bit = 1;
            for(int j = 0; j < 64; j++)
            {
                if(bitSet.get(j + (i * 64)))
                {
                    result[i] = result[i] | bit;
                }
                bit <<= 1;
            }
        }
        return result;
    }

    // Produces an int[] from a given superblock of the rank counts contained by the blocks
    private int[] superBlockCounts(BitSet superBlock)
    {
        int[] result = new int[7];
        for(int i = 0; i < 7; i++)
        {
            int bit = 1;
            int count = 0;
            for(int j = 0; j < 9; j++)
            {
                if(superBlock.get(64 + j + (i * 9)))
                {
                    count = count | bit;
                }
                bit <<= 1;
            }
            result[i] = count;
        }
        return result;
    }

    // Returns the rank at a particular index
    private long rank(int index, BitSet bitSet, BitSet rankDirectory)
    {
        long result = 0;

        // First figure out which superblock this index belongs to.
        // Each superblock represents 2048 bits
        int superBlockIndex = index / 2048;
        // Next, figure out which data block it belongs to.
        // Each data block represents 256 bits
        int dataBlockIndex = (index % 2048) / 256;
        // Finally, figure out the index within that data block.
        int bitIndex = (index % 2048) % 256;

        // Get the superblock, then get the rank of the header
        BitSet superBlock = rankDirectory.get(superBlockIndex, superBlockIndex + 128);
        result = bitSetToLong(superBlock)[0];

        //TODO: remove debugging assert
        if(superBlock.size() != 128)
        {
            throw new AssertionError("oh snap that shit ain't right, rank function edition");
        }

        // Next calculate the rank until the data block.
        int[] dataBlocks = superBlockCounts(superBlock);
        for(int i = 0; i < dataBlockIndex; i++)
        {
            result += dataBlocks[i];
        }

        // Now calculate the rank within the data block.
        // This int is the index of the first bit in the data block to which the bit's index belongs.
        int dataBlockBitIndex = (superBlockIndex * 2048) + (dataBlockIndex * 256);
        for(int i = dataBlockBitIndex; i < index; i++)
        {
            if(bitSet.get(i))
            {
                result++;
            }
        }

        // There we go, constant time rank computation!
        // The first lookup takes one rankDirectory.get call, the second takes 7 iterations of a loop
        // to get the data blocks and worst case 7 to add them all together, and then finally
        // worst case 256 bits are counted to get a rank.

        return result;
    }
}
