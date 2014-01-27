package co.sidhant.airtype;

import android.content.Context;

import org.apache.lucene.util.OpenBitSet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by sid9102 on 11/22/13.
 * Thanks to this paper on succinct encoding: http://people.eng.unimelb.edu.au/sgog/optimized.pdf
 * and this blog post: http://stevehanov.ca/blog/index.php?id=120
 */
public class EncodedTrie
{
    // A bitset of all the words assigned to the nodes, indexed breadth first
    // These words are encoded with 0x01 through 0x1a assigned to a through z,
    // 0x1b and 0x1c refer to the beginning of a word fragment and the beginning of a complete word, respectively
    private OpenBitSet encodedWords;
    // For more useful rank directory generation, this BitSet represents words with a 1 representing the beginning of a word,
    // and a 0 for each letter in the word, so 1 bit for each letter + the header bit for delimiting words.
    // As explained here: http://en.wikipedia.org/wiki/Succinct_data_structure#Examples
    private OpenBitSet wordBits;
    // A rank directory of wordBits.
    private OpenBitSet wordRank;

    // A bitSet of children for each node,
    // 9 bits per node indicating whether a child exists or not, indexed breadth first
    private OpenBitSet trieBits;
    // A base64 encoded rank directory (http://people.eng.unimelb.edu.au/sgog/optimized.pdf, page 4)
    // for O(c) rank lookup on the trie string
    private OpenBitSet trieRank;

    // The current index at which to encode, in the bitsets
    private int encodedWordsIndex;
    private int trieBitIndex;
    private int wordBitsIndex;

    // The current node we have traversed to, and its index
    private OpenBitSet curNode;
    public int curNodeIndex;
    private OpenBitSet curWord;


    private int completion;

    private static AirTypeInitActivity.TrieGenTask trieGenTask;

    private boolean debug = false;

    // This constructor means we want to load some pre-generated bitsets into memory
    public EncodedTrie(Context context) throws IOException, ClassNotFoundException {

        ObjectInputStream wordBitsStream = new ObjectInputStream(context.openFileInput("wordBits.ser"));
        ObjectInputStream encodedWordsStream = new ObjectInputStream(context.openFileInput("encodedWords.ser"));
        ObjectInputStream trieBitsStream = new ObjectInputStream(context.openFileInput("trieBits.ser"));
        ObjectInputStream wordRankStream = new ObjectInputStream(context.openFileInput("wordRank.ser"));
        ObjectInputStream trieRankStream = new ObjectInputStream(context.openFileInput("trieRank.ser"));

        long[] wordBitsL = (long[]) wordBitsStream.readObject();
        wordBits = new OpenBitSet(wordBitsL, wordBitsL.length);
        long[] encodedWordsL = (long[]) encodedWordsStream.readObject();
        encodedWords = new OpenBitSet(encodedWordsL, encodedWordsL.length);
        long[] trieBitsL = (long[]) trieBitsStream.readObject();
        trieBits = new OpenBitSet(trieBitsL, trieBitsL.length);
        long[] wordRankL = (long[]) wordRankStream.readObject();
        wordRank = new OpenBitSet(wordRankL, wordRankL.length);
        long[] trieRankL = (long[]) trieRankStream.readObject();
        trieRank = new OpenBitSet(trieRankL, trieRankL.length);

        wordBitsStream.close();
        encodedWordsStream.close();
        trieBitsStream.close();
        wordRankStream.close();
        trieRankStream.close();

        resetCurNode();
    }

    // This constructor is for generating a new encoded trie
    public EncodedTrie(AirTrie trie, AirTypeInitActivity.TrieGenTask tgTask)
    {
        trieGenTask = tgTask;
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
        curNode = getSubSet(trieBits, 0, 9);
        curNodeIndex = 0;
        setCurWord();
    }

    private OpenBitSet getSubSet(OpenBitSet bitset, int begin, int end)
    {
        return SuccinctEncoding.getSubSet(bitset, begin, end);
    }

    private void setCurWord()
    {
        int index = curNodeIndex + 1;
        int wordBegin = SuccinctEncoding.select(index, wordBits, wordRank) * 5;
        int wordEnd = SuccinctEncoding.select(index + 1, wordBits, wordRank) * 5;
        curWord = getSubSet(encodedWords, wordBegin, wordEnd);
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
        encodedWords = new OpenBitSet();
        trieBits = new OpenBitSet();
        wordBits = new OpenBitSet();

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
        trieRank = SuccinctEncoding.generateRankDirectory(trieBits);
        completion = 85;
        publishProgress();
        wordRank = SuccinctEncoding.generateRankDirectory(wordBits);
        completion = 95;
        publishProgress();
    }

    // This function writes the generated bitsets to files for later access
    public void writeBitSets(Context context) throws IOException {
        ObjectOutputStream wordBitsStream = new ObjectOutputStream(context.openFileOutput("wordBits.ser", Context.MODE_PRIVATE));
        ObjectOutputStream encodedWordsStream = new ObjectOutputStream(context.openFileOutput("encodedWords.ser", Context.MODE_PRIVATE));
        ObjectOutputStream trieBitsStream = new ObjectOutputStream(context.openFileOutput("trieBits.ser", Context.MODE_PRIVATE));
        ObjectOutputStream wordRankStream = new ObjectOutputStream(context.openFileOutput("wordRank.ser", Context.MODE_PRIVATE));
        ObjectOutputStream trieRankStream = new ObjectOutputStream(context.openFileOutput("trieRank.ser", Context.MODE_PRIVATE));

        wordBitsStream.writeObject(wordBits.getBits());
        encodedWordsStream.writeObject(encodedWords.getBits());
        trieBitsStream.writeObject(trieBits.getBits());
        wordRankStream.writeObject(wordRank.getBits());
        trieRankStream.writeObject(trieRank.getBits());

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
            int childIndex = (int) SuccinctEncoding.rank((curNodeIndex * 9) + i, trieBits, trieRank);
            curNode = getSubSet(trieBits, childIndex * 9, childIndex * 9 + 9);
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
        return SuccinctEncoding.getWord(curWord);
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
        OpenBitSet node = curNode.clone();
        int curIndex = curNodeIndex;
        while(node.get(8))
        {
            // Traverse the 9th child of each 9th child, adding them to a list
            int childIndex = (int) SuccinctEncoding.rank((curIndex * 9) + 8, trieBits, trieRank);
            node = getSubSet(trieBits, childIndex * 9, childIndex * 9 + 9);
            curIndex = childIndex;
            int wordBegin = SuccinctEncoding.select(curIndex + 1, wordBits, wordRank) * 5;
            int wordEnd = SuccinctEncoding.select(curIndex + 2, wordBits, wordRank) * 5;
            String word = SuccinctEncoding.getWord(getSubSet(encodedWords, wordBegin, wordEnd));
            result.add(word);
        }

        return result;
    }
}
