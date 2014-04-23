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
            fm.tareRest (data)

        elif fm.mode is 'train':
            fm.trainValues(data)

        elif fm.mode is 'generate':
            fm.generatePermutations()
            
        else:
            pass


        vis.loop(fm)

        # Connect the serial port
        # create the pygame instance
        # Create the machine learner and train some data
        # Create the finger mapping trainer and tie these all together

if __name__ == "__main__":
    main()
