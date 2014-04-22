package desktop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

import io.airtype.AirTrie;
import io.airtype.AirTypeIME;
import io.airtype.EncodedTrie;
import io.airtype.FingerMap;
import io.airtype.TrieMaker;
import py4j.GatewayServer;
import sprokit.Identifier;

/**
 * Created by sid9102 on 3/4/14.
 *
 * Takes numbers, gives out words.
 */

public class PythonInterface
{
    private TreeMap<String, ArrayList<String>> permutationMap;
    private EncodedTrie mEncodedTrie;
    private Identifier mIdentifier;
    private ArrayList<String> mAlts;

    public PythonInterface(){
        System.out.println("Created python gateway interface");
    }

    public EncodedTrie getEncodedTrie() {
        return mEncodedTrie;
    }

    public String getWord(String digits) {
        String result = "";
        mEncodedTrie.resetCurNode();
        for (int j = 0; j < digits.length(); j++) {
            int index = Integer.parseInt(digits.substring(j, j + 1));
            if (mEncodedTrie.goToChild(index)) {
                result = mEncodedTrie.getWord();
            } else
                break;
        }
        mAlts = mEncodedTrie.getAlts();
        System.out.println(result);
        return result;
    }

    public void mapGesture(String gesture, List<List<Double>> data){
        mIdentifier.mapGuesture(gesture, data);
    }

    public void train(List<List<Double>> datalist){
        mIdentifier.build(30, 20, datalist);
        //mEngine.train(datalist);
    }

    public String classifyData(List<Double> data){
        return mIdentifier.identify(data);
    }

    public void initAirType() throws IOException{
        // Make the map
        Scanner s = null;
        try {
            s = new Scanner(new File("permutations.txt"));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        mAlts = new ArrayList<String>();
        permutationMap = new TreeMap<String, ArrayList<String>>(new AirTypeIME.stringLengthComparator());
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
        AirTrie airTrie = TrieMaker.makeTrie(new FingerMap(false), permutationMap);
        mEncodedTrie = new EncodedTrie(airTrie, null);

        mIdentifier = new Identifier();
    }

    public ArrayList<String> getAlts(){
        return mAlts;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        GatewayServer gs = new GatewayServer(new PythonInterface());
        gs.start();
    }
}
