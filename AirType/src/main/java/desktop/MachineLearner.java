package desktop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by sid9102 on 1/24/14.
 */


public class MachineLearner
{
    public static void main(String[] args) throws IOException {
        File data = new File("exaggeratedhello.txt");
        Scanner in = new Scanner(data);

        ArrayList<String> lines = new ArrayList<String>();

        while(in.hasNextLine())
        {
            String curLine = in.nextLine();
            lines.add(curLine);
        }
        in.close();
        File calibration = new File("exaggerated.txt");
        int[] thresholds = getThresholds(calibration);
        System.out.println("Got thresholds: " + Arrays.toString(thresholds));
        int[] curRun = new int[8];
        int[] lastRun = new int[8];
        boolean firstLine = true;
        for(String curLine : lines)
        {
            curLine = curLine.replace("\r", "").replace("\n", "");
            if(!curLine.equals("RUN"))
            {
                StringTokenizer tokenizer = new StringTokenizer(curLine,":");
                int i = 0;
                while(tokenizer.hasMoreTokens())
                {
                    curRun[i] = Integer.parseInt(tokenizer.nextToken());
                    i++;
                }

                if(firstLine)
                {
                    System.arraycopy(curRun, 0, lastRun, 0, 8);
                    firstLine = false;
                }
                else
                {
                    int[] diffs = new int[8];
                    for(i = 0; i < 8; i++)
                    {
                        diffs[i] = curRun[i] - lastRun[i];
                        lastRun[i] = curRun[i];
                        //System.out.format("%3d:", diffs[i]);
                        if(diffs[i] > thresholds[i])
                        {
                            System.out.println("Event on " + Integer.toString(8 - i));
                        }
                    }
                    //System.out.println();
                }
            }
            else
            {
                System.out.println(curLine);
                firstLine = true;
            }
        }
    }

    private static int[] getThresholds(File calibration) throws FileNotFoundException
    {
        Scanner in = new Scanner(calibration);
        ArrayList<String> lines = new ArrayList<String>();
        boolean printDiffs = false;
        while(in.hasNextLine())
        {
            String curLine = in.nextLine();
            lines.add(curLine);
        }
        in.close();
        int[] curRun = new int[8];
        int[] lastRun = new int[8];
        int[] thresholds = {5, 26, 25, 10, 21, 5, 9, 6};
        int curButton;
        boolean threshholdChanged = true;
        while(threshholdChanged)
        {
            threshholdChanged = false;
            boolean firstLine = true;
            curButton = 8;
            runLoop:
            for(String curLine : lines)
            {
                curLine = curLine.replace("\r", "").replace("\n", "");
                if(!curLine.equals("RUN"))
                {
                    StringTokenizer tokenizer = new StringTokenizer(curLine,":");
                    int i = 0;
                    while(tokenizer.hasMoreTokens())
                    {
                        curRun[i] = Integer.parseInt(tokenizer.nextToken());
                        i++;
                    }
                    if(firstLine)
                    {
                        System.arraycopy(curRun, 0, lastRun, 0, 8);
                        firstLine = false;
                    }
                    else
                    {
                        int[] diffs = new int[8];
                        for(i = 0; i < 8; i++)
                        {
                            diffs[i] = curRun[i] - lastRun[i];
                            lastRun[i] = curRun[i];
                            if(printDiffs)
                                System.out.format("%3d:", diffs[i]);
                            if(diffs[i] > thresholds[i])
                            {
                                curButton--;
                                System.out.println("curButton is now " + Integer.toString(curButton));
                                if(i != curButton)
                                {
                                    thresholds[i]++;
                                    threshholdChanged = true;
                                    break runLoop;
                                }
                            }
                        }
                        if(printDiffs)
                            System.out.println();
                    }
                }
                else
                {
                    System.out.println(curLine);
                    firstLine = true;
                    curButton = 8;
                }
            }
        }
        return thresholds;
    }
}
