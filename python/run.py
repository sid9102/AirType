#!/usr/local/bin/python

from py4j.java_gateway import JavaGateway, GatewayClient

import re
import sys
import time

import AirGame as ag
import AirSerial as ais
import AirFingerMap as afm
import AirType as airtype

def main():

    fm = afm.AirFingerMap("The quick brown fox jumps over the lazy dog")
    ser = ais.AirSerial()
    vis = ag.pygame_demo()
    air = airtype.AirType(vis)

    gw = JavaGateway()

    while True:

        # Get the data from serial
        data = ser.getFingerData()
        print data

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
            gw.entry_point.initAirType()
            
        elif fm.mode is 'airtype':
            # Normal typing mode
            finger = fm.handleKeypress(data)
            air.handleFinger(finger, gw)

        vis.loop(fm)

if __name__ == "__main__":
    main()
