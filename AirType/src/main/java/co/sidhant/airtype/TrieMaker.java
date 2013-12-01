package co.sidhant.airtype;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by sid9102 on 11/19/13.
 * This class makes the trie for the first time
 */
public class TrieMaker
{
    public static AirTrie makeTrie(FingerMap fmap, TreeMap<String, ArrayList<String>> permutationMap)
    {
        AirTrie mTrie = new AirTrie();

        java.util.Iterator<Map.Entry<String, ArrayList<String>>> it = permutationMap.entrySet().iterator();
        ArrayList<String> curList;
        String curWord = "";
        AirTrieNode curNode = mTrie.root;
        while(it.hasNext())
        {
            curNode = mTrie.root;
            Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>) it.next();
            curList = pairs.getValue();
            curWord = curList.get(0);
            for(int i = 0; i < curWord.length(); i++)
            {
                int childIndex = fmap.getFingerFromLetter(curWord.toLowerCase().charAt(i));
                if(curNode.getChildPrecise(childIndex) == null)
                {
                    curNode.setChild(new AirTrieNode(), childIndex);
                    curNode = curNode.getChildPrecise(childIndex);
                    curNode.setLetter(curWord.charAt(i));
                    curNode.setWord(curWord.substring(0, i + 1));
                }
                else
                {
                    curNode = curNode.getChildPrecise(childIndex);
                }
            }
            curNode.setWord(curWord);
            curNode.setEndOfWord();
            if(curList.size() > 1)
            {
                for(int i = 1; i < curList.size(); i++)
                {
                    curNode.setChild(new AirTrieNode(), 8);
                    curNode = curNode.getChildPrecise(8);
                    curNode.setWord(curList.get(i));
                }
            }
        }
        return mTrie;
    }

}
