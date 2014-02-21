package desktop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by sid9102 on 1/24/14.
 */


public class MachineLearner
{
    public static void main(String[] args) throws IOException {
        File data = new File("out.txt");
        Scanner in = new Scanner(data);

        ArrayList<String> lines = new ArrayList<String>();

        while(in.hasNextLine())
        {
            String curLine = in.nextLine();
            lines.add(curLine);
        }
        in.close();
        File calibration = new File("training.txt");
        getThresholds(calibration);
        //System.out.println("Got thresholds: " + Arrays.toString(thresholds));
        int[] thresholds = {81, 99, 29, 56, 37, 62, 62, 98};
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

    private static void getThresholds(File calibration) throws FileNotFoundException
    {
        Scanner in = new Scanner(calibration);
        Random rand = new Random();
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
        int[] thresholds = new int[8];
        int curButton;
        int successfulRuns;
        int loop = 0;
        int maxSuccesses = 0;
        while(true)
        {
            loop++;
            successfulRuns = 0;
            boolean firstLine = true;
            curButton = 8;
            boolean failed = false;
            for(String curLine : lines)
            {
                curLine = curLine.replace("\r", "").replace("\n", "");
                if(!curLine.equals("RUN"))
                {
                    if(failed)
                       continue;
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
                        for(i = 0; i < 8 && !failed; i++)
                        {
                            diffs[i] = curRun[i] - lastRun[i];
                            lastRun[i] = curRun[i];
                            if(printDiffs)
                                System.out.format("%3d:", diffs[i]);
                            if(diffs[i] > thresholds[i])
                            {
                                curButton--;
                                //System.out.println("curButton is now " + Integer.toString(curButton));
                                if(i != curButton)
                                {
                                    //System.out.println("Found " + Integer.toString(i) + " instead");
                                    //System.out.println("Diff: " + Integer.toString(diffs[i]) + ", Thresh: " + Integer.toString(thresholds[i]));
                                    thresholds[i]++;
                                    failed = true;
                                }
                            }
                        }
                        if(printDiffs)
                            System.out.println();
                    }
                }
                else
                {
                    //System.out.println(curLine + Integer.toString(loop));
                    firstLine = true;
                    if(!failed)
                        successfulRuns++;
                    else
                        failed = false;
                    curButton = 8;
                }
            }
            if(successfulRuns > maxSuccesses)
            {
                System.out.println("Got " + successfulRuns + " correct runs, thresholds are now: " + Arrays.toString(thresholds));
                maxSuccesses = successfulRuns;
            }
            for(int i = 0; i < 8; i++)
            {
                thresholds[i] = rand.nextInt(100);
            }
        }
        //return thresholds;
    }
}
