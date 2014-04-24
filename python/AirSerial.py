import serial
import sys

class AirSerial():
    def __init__(self):
        # TODO: automate connection for unix, windows is working
        # test the unix case, if not working, change the case to work

        if os.name == 'nt':
            # windows
            for i in range(256):
                if not selected:
                    try:
                        self.ser = serial.Serial(i)
                        return
                    except serial.SerialException:
                        pass
        else: # unix
            try:
                self.ser = seria.Serial('/dev/tty/usbmodem641')
            except OSError:
                print "No connection to the device could be established"
                sys.exit(0)

    def getFingerData(self):
        data = tuple(self.ser.readline().split('\t'))
        return map(int, data)

    def flush(self):
        self.ser.flush()
