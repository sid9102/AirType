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

        data = ser.getFingerData()
        fm.handleKeypress(data)
        vis.loop()

        # Connect the serial port
        # create the pygame instance
        # Create the machine learner and train some data
        # Create the finger mapping trainer and tie these all together
        pass

if __name__ == "__main__":
    main()
