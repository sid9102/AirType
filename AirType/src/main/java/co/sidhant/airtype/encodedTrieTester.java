package co.sidhant.airtype;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.String;import java.lang.System;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Created by sid9102 on 12/10/13.
 *
 * For intensive testing of the encoded trie methods
 */
public class EncodedTrieTester
{
    public static void main(String[] args) throws FileNotFoundException
    {
        // Make the map
        Scanner s = new Scanner(new File("permutations.txt"));
        TreeMap<String, ArrayList<String>> permutationMap = new TreeMap<String, ArrayList<String>>(new AirType.stringLengthComparator());
        String curLine;
        String curKey = "";
        ArrayList<String> curList;
        while(s.hasNextLine())
        {
            curLine = s.nextLine();
            if(curLine.endsWith(":"))
            {
                curKey = curLine.substring(0, curLine.length() - 2);
                curList = new ArrayList<String>();
                curList.add(s.nextLine());
                permutationMap.put(curKey, curList);
            }
            else
            {
                curList = permutationMap.get(curKey);
                curList.add(curLine);
                permutationMap.put(curKey, curList);
            }
        }

        // Make the trie
        AirTrie curTrie = TrieMaker.makeTrie(new FingerMap(), permutationMap);
        EncodedTrie encodedTrie = new EncodedTrie(curTrie, null);
        // Comparing outputs from the encoded and normal trie
        Random rand = new Random();
        int iterations = 100;
        AirTrieNode curNode;
        for(int i = 0; i < iterations; i++)
        {
            // First, do things the normal way
            curNode = curTrie.root;
            int wordLength = rand.nextInt(21);
            String input = "";
            // Simulate keypresses
            for(int j = 0; j < wordLength; j++)
            {
                input += Integer.toString(rand.nextInt(8));
            }
            System.out.println("For input " + input + ";");

            String output = "";
            for(int j = 0; j < wordLength; j++)
            {
                int index = Integer.parseInt(input.substring(j, j + 1));
                curNode = curNode.getChild(index);
                if(curNode == null)
                    break;
                output = curNode.getWord();
                System.out.println(curNode.index + " " + output);
            }
            System.out.println("AirTrie got the word " + output + ".");

            encodedTrie.resetCurNode();
            for(int j = 0; j < wordLength; j++)
            {
                int index = Integer.parseInt(input.substring(j, j + 1));
                if(encodedTrie.goToChild(index))
                {
                    output = encodedTrie.getWord();
                    System.out.println(encodedTrie.curNodeIndex + " " + output);
                }
                else
                    break;
            }
            System.out.println("EncodedTrie got the word " + output + ".");
        }
    }

}
