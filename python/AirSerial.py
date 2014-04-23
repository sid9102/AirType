import serial
import sys

class AirSerial():
    def __init__(self):
        # TODO: automate connection across unix and windows
        try:
            self.ser = serial.Serial('/dev/tty.usbmodem641')
        except OSError:
            print "No connection to the device could be established"
            sys.exit(0)

    def getFingerData(self):
        data = tuple(self.ser.readline().split('\t'))
        return map(int, data)
