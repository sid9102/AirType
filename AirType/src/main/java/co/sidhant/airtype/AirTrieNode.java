package co.sidhant.airtype;

/**
 * Created by sid9102 on 11/19/13.
 */
public class AirTrieNode
{
    private AirTrieNode[] children;
    private char letter;
    private String word;
    private boolean endOfWord;

    // For indexing the nodes breadth first
    public int index;

    public AirTrieNode()
    {
        children = new AirTrieNode[9];
        endOfWord = false;
    }

    public void setEndOfWord()
    {
        this.endOfWord = true;
    }

    public boolean isEndOfWord()
    {
        return this.endOfWord;
    }

    public void setWord(String word)
    {
        this.word = word;
    }

    public String getWord()
    {
        return this.word;
    }

    public void setLetter(char curChar)
    {
        this.letter = curChar;
    }

    public char getLetter()
    {
        return this.letter;
    }

    public void setChild(AirTrieNode child, int index)
    {
        this.children[index] = child;
    }

    public AirTrieNode getChild(int index)
    {
        AirTrieNode result = this.children[index];
        if(result != null)
        {
            return result;
        }
        else // return the closest child instead
        {
            int diff = 8 - index;
            if(diff < index)
                diff = index;
            for(int i = 1; i < diff; i++)
            {
                if (index - i >= 0)
                {
                    result = this.children[index - i];
                    if(result != null)
                    {
                        return result;
                    }
                }

                if(index + i < 9)
                {
                    result = this.children[index + i];
                    if(result != null)
                        return result;
                }
            }
        }
        return null;
    }

    // For constructing the trie, we don't want the nearest child
    public AirTrieNode getChildPrecise(int index)
    {
        return this.children[index];
    }
}
