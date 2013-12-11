package co.sidhant.airtype;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Created by sid9102 on 11/22/13.
 */
public class TrieRunnable implements Runnable
{
    // Make sure the tree is passed from this thread back to the main thread
    private AirType instance;

    public void setAirType(AirType at)
    {
        this.instance = at;
    }
    @Override
    public void run() {
        AssetManager am = instance.getApplicationContext().getAssets();
        InputStream is = null;
        try {
            is = am.open("permutations.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        TreeMap<String, ArrayList<String>> permutationMap = new TreeMap<String, ArrayList<String>>(new AirType.stringLengthComparator());
        Scanner s = new Scanner(is);
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
    }
}
