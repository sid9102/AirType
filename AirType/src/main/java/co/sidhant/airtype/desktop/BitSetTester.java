package co.sidhant.airtype.desktop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

import co.sidhant.airtype.AirTrie;
import co.sidhant.airtype.AirTypeIME;
import co.sidhant.airtype.EncodedTrie;
import co.sidhant.airtype.FingerMap;
import co.sidhant.airtype.OpenEncodedTrie;
import co.sidhant.airtype.TrieMaker;

/**
 * Created by sid9102 on 1/25/14.
 */
public class BitSetTester
{
    public static void main(String[] args) throws FileNotFoundException
    {
        Scanner s = new Scanner(new File("permutations.txt"));
        TreeMap<String, ArrayList<String>> permutationMap = new TreeMap<String, ArrayList<String>>(new AirTypeIME.stringLengthComparator());
        String curLine;
        String curKey = "";
        ArrayList<String> curList;
        while (s.hasNextLine()) {
            curLine = s.nextLine();
            if (curLine.endsWith(":")) {
                curKey = curLine.substring(0, curLine.length() - 1);
                curList = new ArrayList<String>();
                curList.add(s.nextLine());
                permutationMap.put(curKey, curList);
            } else {
                curList = permutationMap.get(curKey);
                curList.add(curLine);
                permutationMap.put(curKey, curList);
            }
        }

        // Make the trie
        AirTrie curTrie = TrieMaker.makeTrie(new FingerMap(), permutationMap);

        int iterations = 1;

        long totalTime = 0;

        long time;
        EncodedTrie encodedTrie = null;
        for(int i = 0; i < iterations; i++)
        {
            time = System.nanoTime();
            encodedTrie = new EncodedTrie(curTrie, null);
            time = System.nanoTime() - time;
            totalTime += time;
        }

        long avgTime = totalTime / iterations;

        long eTrieTime = avgTime;

        System.out.println("EncodedTrie took an average of " + avgTime + " ns to generate.");

        totalTime = 0;
        OpenEncodedTrie openEncodedTrie = null;
        for (int i = 0; i < iterations; i++)
        {
            time = System.nanoTime();
            openEncodedTrie = new OpenEncodedTrie(curTrie, null);
            time = System.nanoTime() - time;
            totalTime += time;
        }

        avgTime = totalTime / iterations;

        System.out.println("OpenEncodedTrie took an average of " + avgTime + " ns to generate.");
        System.out.println("OpenEncodedTrie took " + getPercentage(eTrieTime, avgTime) + "% as long as EncodedTrie.");
        System.out.println();

        String input = "262233674227406633074521";
        int wordLength = input.length();
        String output = "";

        time = System.nanoTime();
        encodedTrie.resetCurNode();
        for(int j = 0; j < wordLength; j++)
        {
            int index = Integer.parseInt(input.substring(j, j + 1));
            if(encodedTrie.goToChild(index))
            {
                output = encodedTrie.getWord();
                System.out.println((encodedTrie.curNodeIndex + 1) + " " + output);
            }
            else
                break;
        }
        time = System.nanoTime() - time;
        System.out.println("EncodedTrie got the word " + output + ", and took " + time + " ns.");
        eTrieTime = time;

        time = System.nanoTime();
        openEncodedTrie.resetCurNode();
        for(int j = 0; j < wordLength; j++)
        {
            int index = Integer.parseInt(input.substring(j, j + 1));
            if(openEncodedTrie.goToChild(index))
            {
                output = openEncodedTrie.getWord();
                System.out.println((openEncodedTrie.curNodeIndex + 1) + " " + output);
            }
            else
                break;
        }
        time = System.nanoTime() - time;
        System.out.println("OpenEncodedTrie got the word " + output + ", and took " + time + " ns.");
        System.out.println("OpenEncodedTrie took " + getPercentage(eTrieTime, time) + "% as long as EncodedTrie.");
        System.out.println();

        totalTime = 0;
        for(int i = 0; i < iterations; i++)
        {
            time = System.nanoTime();
            try {
                encodedTrie.writeBitSets();
            } catch (IOException e) {
                e.printStackTrace();
            }
            time = System.nanoTime() - time;
            totalTime += time;
        }
        avgTime = totalTime / iterations;
        System.out.println("EncodedTrie took " + avgTime + " ns to write.");
        eTrieTime = avgTime;

        totalTime = 0;
        for(int i = 0; i < iterations; i++)
        {
            time = System.nanoTime();
            try {
                openEncodedTrie.writeBitSets();
            } catch (IOException e) {
                e.printStackTrace();
            }
            time = System.nanoTime() - time;
            totalTime += time;
        }
        avgTime = totalTime / iterations;

        System.out.println("OpenEncodedTrie took " + avgTime + " ns to write.");
        System.out.println("OpenEncodedTrie took " + getPercentage(eTrieTime, avgTime) + "% as long as EncodedTrie.");

        totalTime = 0;
        for(int i = 0; i < iterations; i++)
        {
            time = System.nanoTime();
            try {
                encodedTrie = new EncodedTrie();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            time = System.nanoTime() - time;
            totalTime += time;
        }
        avgTime = totalTime / iterations;
        System.out.println("EncodedTrie took " + avgTime + " ns to read.");
        eTrieTime = avgTime;

        totalTime = 0;
        for(int i = 0; i < iterations; i++)
        {
            time = System.nanoTime();
            try {
                openEncodedTrie = new OpenEncodedTrie();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            time = System.nanoTime() - time;
            totalTime += time;
        }
        avgTime = totalTime / iterations;
        System.out.println("OpenEncodedTrie took " + avgTime + " ns to read.");
        System.out.println("OpenEncodedTrie took " + getPercentage(eTrieTime, avgTime) + "% as long as EncodedTrie.");
    }

    private static long getPercentage(long eTrieTime, long time)
    {
        return (long) (((float) time / (float) eTrieTime) * 100);
    }
}
