package co.sidhant.airtype;

import android.content.Context;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * Created by sid9102 on 11/22/13.
 * Thanks to this paper on succinct encoding: http://people.eng.unimelb.edu.au/sgog/optimized.pdf
 * and this blog post: http://stevehanov.ca/blog/index.php?id=120
 */
public class EncodedTrie
{
    //TODO: use openBitSet instead, http://lucene.apache.org/core/3_0_3/api/core/org/apache/lucene/util/OpenBitSet.html

    // A bitset of all the words assigned to the nodes, indexed breadth first
    // These words are encoded with 0x01 through 0x1a assigned to a through z,
    // 0x1b and 0x1c refer to the beginning of a word fragment and the beginning of a complete word, respectively
    private BitSet encodedWords;
    // For more useful rank directory generation, this BitSet represents words with a 1 representing the beginning of a word,
    // and a 0 for each letter in the word, so 1 bit for each letter + the header bit for delimiting words.
    // As explained here: http://en.wikipedia.org/wiki/Succinct_data_structure#Examples
    private BitSet wordBits;
    // A rank directory of wordBits.
    private BitSet wordRank;

    // A bitSet of children for each node,
    // 9 bits per node indicating whether a child exists or not, indexed breadth first
    private BitSet trieBits;
    // A base64 encoded rank directory (http://people.eng.unimelb.edu.au/sgog/optimized.pdf, page 4)
    // for O(c) rank lookup on the trie string
    private BitSet trieRank;

    // The current index at which to encode, in the bitsets
    private int encodedWordsIndex;
    private int trieBitIndex;
    private int wordBitsIndex;

    // The current node we have traversed to, and its index
    private BitSet curNode;
    public int curNodeIndex;
    private BitSet curWord;


    private int completion;

    private static AirTypeIMESettings.TrieGenTask trieGenTask;

    private boolean debug = false;

    // This constructor means we want to load some pre-generated bitsets into memory
    public EncodedTrie(Context context) throws IOException, ClassNotFoundException {

        ObjectInputStream wordBitsStream = new ObjectInputStream(context.openFileInput("wordBits.ser"));
        ObjectInputStream encodedWordsStream = new ObjectInputStream(context.openFileInput("encodedWords.ser"));
        ObjectInputStream trieBitsStream = new ObjectInputStream(context.openFileInput("trieBits.ser"));
        ObjectInputStream wordRankStream = new ObjectInputStream(context.openFileInput("wordRank.ser"));
        ObjectInputStream trieRankStream = new ObjectInputStream(context.openFileInput("trieRank.ser"));

        wordBits = (BitSet) wordBitsStream.readObject();
        encodedWords = (BitSet) encodedWordsStream.readObject();
        trieBits = (BitSet) trieBitsStream.readObject();
        wordRank = (BitSet) wordRankStream.readObject();
        trieRank = (BitSet) trieRankStream.readObject();

        wordBitsStream.close();
        encodedWordsStream.close();
        trieBitsStream.close();
        wordRankStream.close();
        trieRankStream.close();

        resetCurNode();
    }

    // This constructor is for generating a new encoded trie
    public EncodedTrie(AirTrie trie, AirTypeIMESettings.TrieGenTask trieGenTask)
    {
        this.trieGenTask = trieGenTask;
        makeEncodedTrie(trie);
        resetCurNode();
    }

    private void publishProgress()
    {
        if(trieGenTask != null)
        {
            trieGenTask.publish(completion);
        }
    }

    // Helper function for resetting the current node to the root node
    public void resetCurNode()
    {
        curNode = trieBits.get(0, 9);
        curNodeIndex = 0;
        setCurWord();
    }

    private void setCurWord()
    {
        int index = curNodeIndex + 1;
        int wordBegin = select(index, wordBits, wordRank) * 5;
        int wordEnd = select(index + 1, wordBits, wordRank) * 5;
        curWord = encodedWords.get(wordBegin, wordEnd);
        //Log.v("getting word", "from " + wordBegin + " to" + wordEnd);
    }

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

        encodedWordsIndex = 0;
        trieBitIndex = 0;
        wordBitsIndex = 0;
        encodedWords = new BitSet();
        trieBits = new BitSet();
        wordBits = new BitSet();

        completion = 50;
        //Breadth first search of the trie, list every string
        while(!nodeQueue.isEmpty())
        {
            currentNode++;
            curNode = nodeQueue.removeFirst();
            curNode.index = (int) currentNode;
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
                publishProgress();
            }
            encodeNodeWord(curNode);
            encodeNodeWordBits(curNode);
            encodeNodeChildren(curNode);
        }

        completion = 75;
        publishProgress();
        // Generate the rank directory for the trie bitset
        trieRank = generateRankDirectory(trieBits);
        completion = 85;
        publishProgress();
        wordRank = generateRankDirectory(wordBits);
        completion = 95;
        publishProgress();
    }

    // This function writes the generated bitsets to files for later access
    public void writeBitSets(Context context) throws IOException {
        ObjectOutputStream wordBitsStream = new ObjectOutputStream(context.openFileOutput("wordBits.ser", 0));
        ObjectOutputStream encodedWordsStream = new ObjectOutputStream(context.openFileOutput("encodedWords.ser", 0));
        ObjectOutputStream trieBitsStream = new ObjectOutputStream(context.openFileOutput("trieBits.ser", 0));
        ObjectOutputStream wordRankStream = new ObjectOutputStream(context.openFileOutput("wordRank.ser", 0));
        ObjectOutputStream trieRankStream = new ObjectOutputStream(context.openFileOutput("trieRank.ser", 0));

        wordBitsStream.writeObject(wordBits);
        encodedWordsStream.writeObject(encodedWords);
        trieBitsStream.writeObject(trieBits);
        wordRankStream.writeObject(wordRank);
        trieRankStream.writeObject(trieRank);

        wordBitsStream.close();
        encodedWordsStream.close();
        trieBitsStream.close();
        wordRankStream.close();
        trieRankStream.close();
    }

    // Convert a node's word into a binary representation,
    // 0x01 through 0x1a represent a through z, delimited through the use of 0x1b to indicate the
    // beginning of a word fragment and 0x1c to indicate the beginning of a full word
    private void encodeNodeWord(AirTrieNode node)
    {
        int oldIndex = encodedWordsIndex;
        if(node.isEndOfWord())
        {
            //encode the word header, complete word, so 11100
            encodedWords.set(encodedWordsIndex, encodedWordsIndex + 3);
        }
        else
        {
            //encode the word header, complete word, so 11011
            encodedWords.set(encodedWordsIndex, encodedWordsIndex + 2);
            encodedWords.set(encodedWordsIndex + 3, encodedWordsIndex + 5);
        }
        encodedWordsIndex += 5;
        String curWord = node.getWord();
        // Encode the letters one by one, 5 bits each
        for(int i = 0; i < curWord.length(); i++)
        {
            int letter = curWord.charAt(i) - 'a' + 1;
            int bit = 1;
            for(int j = 0; j < 5; j++)
            {
                if((letter & bit) != 0)
                    encodedWords.set(encodedWordsIndex);
                encodedWordsIndex++;
                bit <<= 1;
            }
        }
        boolean debugWords = false;
        if(debug)
        {
            int check = (int) System.currentTimeMillis() % 10;
            debugWords = check == 0;
        }
        if(debugWords)
        {
            String fragment = node.isEndOfWord() ? "complete word " : "fragment of word ";
            System.out.println("encoded " + fragment + curWord + " as:");
            System.out.println("Header is:");
            for(int i = 0; i < 5; i++)
            {
                System.out.print(encodedWords.get(oldIndex + i) ? "1" : "0");
            }
            System.out.println();
            System.out.println(encodedWords.get(oldIndex + 2) ? "According to header, word is complete" : "According to header, word is a fragment");
            for(int i = 0; i < curWord.length(); i++)
            {
                System.out.print("character " + curWord.charAt(i) + " encoded as ");
                int bit = 1;
                int curLetter = 0;
                for(int j = 0; j < 5; j++)
                {
                    System.out.print(encodedWords.get(oldIndex + (i + 1) * 5 + j) ? "1" : "0");
                    if(encodedWords.get(oldIndex + (i + 1) * 5 + j))
                    {
                        curLetter |=  bit;
                    }
                    bit <<= 1;
                }
                curLetter += 0x60;
                char letter = (char) curLetter;
                System.out.println(" decoded as " + letter);
            }
        }
    }

    // This encodes the words for fast searching with a 1 to delimit words, then a 0 for each letter
    private void encodeNodeWordBits(AirTrieNode node)
    {
        // First encode a 1 to indicate the beginning of a word
        wordBits.set(wordBitsIndex);
        int oldWordBitsIndex = wordBitsIndex;
        wordBitsIndex++;
        // Then encode a 0 for each letter
        wordBitsIndex += node.getWord().length();
        if(debug)
        {
            System.out.print(node.getWord() + " encoded in wordBits as ");
            for(int j = oldWordBitsIndex; j < wordBitsIndex; j++)
            {
                System.out.print(wordBits.get(j) ? "1" : "0");
            }
            System.out.println();
            System.out.print("wordBits is now ");
            for(int j = 0; j < wordBits.length(); j++)
            {
                System.out.print(wordBits.get(j) ? "1" : "0");
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
        if(size == 0)
            size++;
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

    // Returns the rank at a particular index in the BitSet bitSet, using the rank directory rankDirectory
    private long rank(int index, BitSet bitSet, BitSet rankDirectory)
    {
        long result = 0;
        if(index == 0)
            return 1;
        // First figure out which superblock this index belongs to.
        // Each superblock represents 2048 bits
        int superBlockIndex = index / 2048;

        // Next, figure out which data block it belongs to.
        // Each data block represents 256 bits
        int dataBlockIndex = (index % 2048) / 256;
        // Finally, figure out the index within that data block.
        int bitIndex = (index % 2048) % 256;

        // Get the superblock, then get the rank of the header
        BitSet superBlock = rankDirectory.get(superBlockIndex * 128, (superBlockIndex + 1) * 128);
        long[] header = bitSetToLong(superBlock);
        result = header[0];

        // Next calculate the rank until the data block.
        int[] dataBlocks = superBlockCounts(superBlock);
        for(int i = 0; i < dataBlockIndex; i++)
        {
            result += dataBlocks[i];
        }

        // Now calculate the rank within the data block.
        // This int is the index of the first bit in the data block to which the bit's index belongs.
        int dataBlockBitIndex = (superBlockIndex * 2048) + (dataBlockIndex * 256);
        for(int i = dataBlockBitIndex; i <= index; i++)
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

    // Returns the i-th occurrence of a 1 in the BitSet, by binary searching the rank directory rankDirectory
    private int select(int i, BitSet bitSet, BitSet rankDirectory)
    {
        // Start at the middle of the bitset
        int upperLimit = bitSet.length();
        int searchIndex = upperLimit / 2;
        int oldSearchIndex = 0;
        int rank = 0;
        // Binary search!
        while(rank != i)
        {
            oldSearchIndex = searchIndex;
            rank = (int) rank(searchIndex, bitSet, rankDirectory);
            if(rank < i)
            {
                searchIndex += (upperLimit - searchIndex)/ 2;
            }
            else if(rank > i)
            {
                upperLimit = searchIndex;
                searchIndex /= 2;
            }

            // Prevent infinite searching
            if(searchIndex > bitSet.length() || (oldSearchIndex == searchIndex && rank != i))
                return -1;
        }

        // Now find the actual beginning of this word, search backwards until rank changes
        while(rank == i)
        {
            searchIndex--;
            rank = (int) rank(searchIndex, bitSet, rankDirectory);
        }
        searchIndex++;
        return searchIndex;
    }

    // Traverse to the nearest possible child if this child doesn't exist
    public boolean goToChild(int index)
    {
        boolean result = false;
        if(!goToChildPrecise(index))
        {
            //Log.v("couldn't find it", Integer.toString(index));
            int diff = 8 - index;
            if(diff < index)
                diff = index;
            for(int i = 1; i < diff && !result; i++)
            {
                if (index - i >= 0)
                {
                    if(goToChildPrecise(index - i))
                    {
                        //Log.v("got", Integer.toString(index - i) + " instead");
                        result = true;
                    }
                }
                if(index + i < 9 && !result)
                {
                    if(goToChildPrecise(index + i))
                    {
                        //Log.v("got", Integer.toString(index + i) + " instead");
                        result = true;
                    }
                }
            }
            setCurWord();
        }
        else
        {
            //Log.v("got it!", Integer.toString(index));
            result = true;
        }
        //Log.v("got word in goto", getWord());
        return result;
    }

    // Traverse to a certain child, returns true if the child exists
    public boolean goToChildPrecise(int i)
    {
        if(!curNode.get(i))
        {
            return false;
        }
        else
        {
            // Figure out where the child is
            int childIndex = (int) rank((curNodeIndex * 9) + i, trieBits, trieRank);
            curNode = trieBits.get(childIndex * 9, childIndex * 9 + 9);
            curNodeIndex = childIndex;
            setCurWord();
            return true;
        }
    }

    // Check if the current node has any children at all.
    public boolean hasChildren()
    {
        for(int i = 0; i < 8; i++)
        {
            if(curNode.get(i))
                return true;
        }
        return false;
    }

    // Default getWord, gets the current word
    public String getWord()
    {
        return getWord(curWord);
    }

    // Get a word for a provided BitSet
    public String getWord(BitSet word)
    {
        // The curWord may have trailing 0s, we need to make sure
        // to read until the end of the word,including trailing zeroes.
        String result = "";
        for(int i = 1; i < word.length() + 1; i++)
        {
            int bit = 1;
            int curLetter = 0;
            int curStart = i * 5;
            for(int j = curStart; j < curStart + 5; j++)
            {
                if(word.get(j))
                {
                    curLetter |=  bit;
                }
                bit <<= 1;
            }
            if(curLetter >= 27 || curLetter <= 0)
                return result;
            curLetter += 0x60;
            char letter = (char) curLetter;
            result += letter;
        }
        return result;
    }

    // Check if the current word is complete (5 header bits are 0x1c) or a fragment (header bits are 0x1b)
    public boolean isEndOfWord()
    {
        // The third bit of the header is 0 in a fragment, as it would have "11011" as its header,
        // while a complete word would have 11100. Return that bit.
        return curWord.get(2);
    }

    // List all alternate predictions for the candidate view
    public ArrayList<String> getAlts()
    {
        ArrayList<String> result = new ArrayList<String>();
        BitSet node = (BitSet) curNode.clone();
        int curIndex = curNodeIndex;
        while(node.get(8))
        {
            // Traverse the 9th child of each 9th child, adding them to a list
            int childIndex = (int) rank((curIndex * 9) + 8, trieBits, trieRank);
            node = trieBits.get(childIndex * 9, childIndex * 9 + 9);
            curIndex = childIndex;
            int wordBegin = select(curIndex + 1, wordBits, wordRank) * 5;
            int wordEnd = select(curIndex + 2, wordBits, wordRank) * 5;
            String word = getWord(encodedWords.get(wordBegin, wordEnd));
            result.add(word);
        }

        return result;
    }
}
