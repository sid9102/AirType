import serial, pygame, random, sys, time, os, py4j
from py4j.java_gateway import JavaGateway, GatewayClient
from pygame.locals import *
from serial.tools import list_ports

rectangles = [] #ct(0, 625, 100, 100) left top width height
colors = []
usediffs = True
sdata = "Serial data not yet assigned."
ser = []
rectheight = 50
rectpad = 10
rectloc = rectpad
olddata = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
                
def serial_ports():
    if os.name == 'nt':
        # windows
        for i in range(256):
            try:
                s = serial.Serial(i)
                s.close()
                yield 'COM' + str(i + 1)
            except serial.SerialException:
                pass
    else:
        # unix
        for port in list_ports.comports():
            yield port[0]

def getdata():
    global sdata, ser
    sdata = ser.readline()
    return sdata

def peekdata():
    global sdata
    return sdata

def scale(x):
    return abs(x) * 300

def getrandomcolor():
    return pygame.Color(random.randrange(255), random.randrange(240), random.randrange(240), 255)

def installrectangle():
    global rectangles, rectloc, rectheight, rectpad
    rectangles.append(pygame.Rect(0,rectloc,0,rectheight))
    rectloc += rectheight + rectpad
    colors.append(getrandomcolor())

def installorget(ri):
    global rectangles
    if ri < len(rectangles):
        return rectangles[ri]
    else:
        installrectangle()
        return installorget(ri)

def acquire_port(baud):
    global ser
    ports = list(serial_ports())
    for port in ports:
        try:
            ser = serial.Serial(port, baud)
            print("selected port " + str(port))
            return
        except Exception:
            print(str(port) + " is not accessible")

def parsedata(doprint):
    global olddata, usediffs
    data = []

    for r in range(1):
        if(doprint):
            print("Processing data: " + peekdata())
        for s in peekdata().split("\t"):
            try:
                data.append(float(s))
            except ValueError:
                data.append(float(0))
                print("Value error detected, inputting 0, will invalidate results.")
        getdata()
    
    if doprint:
        print("Parsed data: " + str(data))

    if usediffs:
        for x in range(len(data)):
            s = olddata[x]
            olddata[x] = data[x]
            data[x] = data[x] - s
        
    return data

def renderdata(windowSurface, fontObj, data):
    global rectangles, colors
    for x in range(len(data)):
        installorget(x).width = scale(data[x])
        windowSurface.fill(colors[x], rectangles[x])
        textSurface = fontObj.render(str(data[x]), False, pygame.Color("white"))
        textRect = textSurface.get_rect()
        textRect.topleft = rectangles[x].topleft
        windowSurface.blit(textSurface, textRect)

def takeinput(pygame, state):
    for event in pygame.event.get():
        if event.type == KEYDOWN:
            if event.key == K_ESCAPE:
                pygame.event.post(pygame.event.Event(QUIT))
            else:
                if event.key == K_RETURN:
                    if state == "stable":
                        state = "collect"
                        print("collecting")
                    elif state == "collect":
                        print("building")
                        state = "build"
                    elif state == "map":
                        print("classsifying")
                        state = "classify"
        elif event.type == QUIT:
            pygame.quit()
            sys.exit(0)
    return state
             
def main():
    datas = []
    state = "stable"
    skip = ""
    ci = 0
    r = 0
    rs = 25
    cmap = ["None","f1","f2","f3","f4"]
    
    #initialization
    gateway = JavaGateway(auto_convert=True)
    acquire_port(115200)
    pygame.init()
    fpsClock = pygame.time.Clock()
    windowSurface = pygame.display.set_mode((1200, 750))
    fontObj = pygame.font.Font('freesansbold.ttf', 32)

    #main
    ser.write("a")
    while True:
        windowSurface.fill(pygame.Color(255, 255, 255, 255))

        data = parsedata(False)
        if state == "collect":
            datas.append(data)
        elif state == "build":
            state = "map"
            gateway.entry_point.build(10,20,datas)
        elif state == "map":
            if r == 0:
                print("perform guesture: " + cmap[ci])
                datas = []
            elif r == rs:
                print("....mapping")
                print(str(gateway.entry_point.mapGuesture(cmap[ci],datas)))
                r = -1
                ci = ci + 1
            if ci == len(cmap):
                state = "classify"
            datas.append(data)
            r = r+1
        elif state == "classify":
            stuff = gateway.entry_point.identify(data)
            if not "None" in stuff:
                print(stuff)
        renderdata(windowSurface, fontObj, data)
        state = takeinput(pygame, state)

        pygame.display.update()
        fpsClock.tick(30)
        ser.flushInput()

main()
