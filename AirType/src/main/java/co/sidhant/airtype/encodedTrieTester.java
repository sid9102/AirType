package co.sidhant.airtype;

import net.sourceforge.sizeof.SizeOf;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.String;
import java.lang.System;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Created by sid9102 on 12/10/13.
 * <p/>
 * For intensive testing of the encoded trie methods
 */
public class EncodedTrieTester {
    public static void main(String[] args) throws FileNotFoundException {
        // Make the map
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
        int iterations = 100;
        AirTrie curTrie = TrieMaker.makeTrie(new FingerMap(), permutationMap);
        EncodedTrie encodedTrie = new EncodedTrie(curTrie, null);
        autoTester(iterations, encodedTrie, curTrie, permutationMap);
//        String input = "1403";
//        String output = "";
//        // First, do things the normal way
//        AirTrieNode curNode = curTrie.root;
//        int wordLength = input.length();
//        for(int j = 0; j < wordLength; j++)
//        {
//            int index = Integer.parseInt(input.substring(j, j + 1));
//            curNode = curNode.getChild(index);
//            if(curNode == null)
//                break;
//            output = curNode.getWord();
//            System.out.println(curNode.index + " " + output);
//        }
//        System.out.println("AirTrie got the word " + output + ".");
//
//        // Now provide the same input to encodedTrie
//        encodedTrie.resetCurNode();
//        for(int j = 0; j < wordLength; j++)
//        {
//            int index = Integer.parseInt(input.substring(j, j + 1));
//            if(encodedTrie.goToChild(index))
//            {
//                output = encodedTrie.getWord();
//                System.out.println((encodedTrie.curNodeIndex + 1) + " " + output);
//            }
//            else
//                break;
//        }
//        System.out.println("EncodedTrie got the word " + output + ".");
    }

    private static void autoTester(int iterations, EncodedTrie encodedTrie, AirTrie curTrie, TreeMap<String, ArrayList<String>> permutationMap) {
        long ETSize = 0;
        long ATSize = 0;
        for (int i = 0; i < iterations; i++) {
            curTrie = TrieMaker.makeTrie(new FingerMap(), permutationMap);
            encodedTrie = new EncodedTrie(curTrie, null);
            ATSize += SizeOf.deepSizeOf(curTrie);
            ETSize += SizeOf.deepSizeOf(encodedTrie);
        }
        ETSize /= iterations * 1000000;
        ATSize /= iterations * 1000000;
        System.out.println("AirTrie is, on average, " + ATSize + " MB.");
        System.out.println("EncodedTrie is, on average, " + ETSize + " MB.");
        // Comparing outputs from the encoded and normal trie
        Random rand = new Random();
        AirTrieNode curNode;
        long ATStart = 0;
        long ETStart = 0;
        long ETTotal = 0;
        long ATTotal = 0;
        long totalLetters = 0;
        for (int i = 0; i < iterations; i++) {
            int wordLength = rand.nextInt(21);
            totalLetters += wordLength;
            String input = "";
            // Simulate keypresses
            for (int j = 0; j < wordLength; j++) {
                input += Integer.toString(rand.nextInt(8));
            }
            //System.out.println("For input " + input + ";");

            String output = "";
            ATStart = System.nanoTime();
            // First, do things the normal way
            curNode = curTrie.root;
            for (int j = 0; j < wordLength; j++) {
                ATStart = System.nanoTime();
                int index = Integer.parseInt(input.substring(j, j + 1));
                curNode = curNode.getChild(index);
                if (curNode == null)
                    break;
                output = curNode.getWord();
                //System.out.println(curNode.index + " " + output);
            }
            //System.out.println("AirTrie got the word " + output + ".");
            ATTotal += System.nanoTime() - ATStart;

            // Now provide the same input to encodedTrie
            ETStart = System.nanoTime();
            encodedTrie.resetCurNode();
            for (int j = 0; j < wordLength; j++) {
                int index = Integer.parseInt(input.substring(j, j + 1));
                if (encodedTrie.goToChild(index)) {
                    output = encodedTrie.getWord();
                    //System.out.println(encodedTrie.curNodeIndex + " " + output);
                } else
                    break;
            }
            ETTotal += System.nanoTime() - ETStart;
            //System.out.println("EncodedTrie got the word " + output + ".");
        }

        long ETAvg = ETTotal / (totalLetters * iterations);
        long ATAvg = ATTotal / (totalLetters * iterations);
        System.out.println("AirTrie took an average of " + ATAvg + " nanoseconds per character");
        System.out.println("EncodedTrie took an average of " + ETAvg + " nanoseconds per character");
    }

}
