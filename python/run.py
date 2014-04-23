#!/usr/local/bin/python

from py4j.java_gateway import JavaGateway, GatewayClient
import re
import sys
import time

import AirGame as ag
import AirSerial as ais
import AirFingerMap as afm

def main():

    fm = afm.AirFingerMap("The quick brown fox jumps over the lazy dog")
    ser = ais.AirSerial()
    vis = ag.pygame_demo()

    while True:

        # Get the data from serial
        data = ser.getFingerData()

        # TODO: handle states
        if fm.mode is 'ready':
            pass

        elif fm.mode is 'tareRest':
            fm.tareRest(data)
        elif fm.mode is 'getranges':
            fm.getRanges(data)
        elif fm.mode is 'train':
            fm.trainValues(data)

        elif fm.mode is 'generate':
            fm.generatePermutations()
            
        elif fm.mode is 'airtype':
            # Normal typing mode
            finger = fm.handleKeypress(data)

            if finger == -1:
                finger = '-1'
            # if a space get the best word possible
            elif vis.hit_space:
                vis.hit_space = False
                # Cycle candidates
                if len(wordFrag) == 0:
                    print alts
                    if altIndex < len(alts):
                        paragraphText = alts[altIndex]
                        altIndex += 1
                    if altIndex >= len(alts):
                        altIndex = 0;
                else:
                    # Get the best word
                    digitWord = ''.join(wordFrag)
                    paragraphText = gw.entry_point.getWord(digitWord)
                    print paragraphText;
                    wordFrag = []
                    alts = gw.entry_point.getAlts()
                    alts.append(paragraphText)
                    print alts
            elif finger >= 0:
                finger = str(finger)
                wordFrag.append(finger)
                print ('wordfrag', wordFrag)
                digitWord = ''.join(wordFrag)
                print ('digitword', digitWord)
                partialWord = gw.entry_point.getWord(digitWord)
                alts = gw.entry_point.getAlts()
                alts.insert(0, partialWord)
                altCombined = ', '.join(alts)
                print alts
                print wordFrag


        vis.loop(fm)

        # Connect the serial port
        # create the pygame instance
        # Create the machine learner and train some data
        # Create the finger mapping trainer and tie these all together

if __name__ == "__main__":
    main()
