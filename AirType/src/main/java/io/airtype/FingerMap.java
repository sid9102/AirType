package io.airtype;

import java.util.ArrayList;

/**
 * Created by sid9102 on 11/19/13.
 */
public class FingerMap {

    public ArrayList<Character>[] fingers;

    // TODO: Store this differently? More efficiently?
    public FingerMap()
    {
        fingers = new ArrayList[8];
        for(int i = 0; i < 8; i++)
        {
            fingers[i] = new ArrayList<Character>();
        }

        fingers[0].add('q');
        fingers[0].add('a');
        fingers[0].add('z');
        fingers[1].add('w');
        fingers[1].add('s');
        fingers[1].add('x');
        fingers[2].add('e');
        fingers[2].add('d');
        fingers[2].add('c');
        fingers[3].add('r');
        fingers[3].add('f');
        fingers[3].add('v');
        fingers[3].add('t');
        fingers[3].add('g');
        fingers[3].add('b');
        fingers[4].add('y');
        fingers[4].add('h');
        fingers[4].add('n');
        fingers[4].add('u');
        fingers[4].add('j');
        fingers[4].add('m');
        fingers[5].add('i');
        fingers[5].add('k');
        fingers[6].add('o');
        fingers[6].add('l');
        fingers[7].add('p');
    }

    //returns a finger for a letter
    public int getFingerFromLetter(char letter)
    {
        ArrayList<Character> curList;
        for(int i = 0; i < 8; i++)
        {
            curList = this.fingers[i];
            if(curList.contains(letter))
            {
                return i;
            }
        }

        return -1;
    }
}
