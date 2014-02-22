import serial, pygame, random, sys, time
from pygame.locals import *
ser = serial.Serial('COM22', 9600)

pygame.init()
fpsClock = pygame.time.Clock()
windowSurface = pygame.display.set_mode((1200, 500))
fontObj = pygame.font.Font('freesansbold.ttf', 32)
start = int(round(time.time() * 1000))


def scale(x):
    return x // 7

rectangles = [pygame.Rect(0, 0, 100, 100), pygame.Rect(0, 125, 100, 100), pygame.Rect(0, 250, 100, 100), pygame.Rect(0, 375, 100, 100)]

def getrandomcolor():
    return pygame.Color(random.randrange(255), random.randrange(240), random.randrange(240), 255)

colors = [getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor()]

def process_serial(currtime, valdict):
    values = ser.readline()[:-2].split("\t")
    valtime = int(values[0])
    while(valtime <= currtime):
        valdict[valtime] = values
    return values

def process_keys(currtime, keydict, valdict):
    for event in pygame.event.get():
        if event.type == KEYDOWN:
            if event.key == K_ESCAPE:
                pygame.event.post(pygame.event.Event(QUIT))
            else:
                keydict[currtime] = event.key
        elif event.type == QUIT:
            assdict = generate_asoc(keydict, valdict)
            pickle.dump(assdict, open("ass.data", 'wb'))
            pygame.quit()
            sys.exit(0)
    return "No key pressed"

def generate_asoc(keydict, valdict):
    assdict = dict
    for keytime in keydict.keys():
    closestdist = 9999999
    closesttime = -1
    for valtime in valdict.keys():
        if abs(valtime - keytime) < closestdist:
            closestdist = abs(valtime - keytime)
            closesttime = valtime
    collide(assdict, keydict[keytime], valdict[closesttime])
    return assdict    

def collide(assdict, key, newvalues):
    if key in assdict:
        oldvalues = assdict[key]
        for x in range(len(oldvalues)):
            oldvalues[x] = (oldvalues[x] + int(newvalues[x])) / 2
    else:
        oldvalues = []
        for x in range(len(values)):
            oldvalues[x] = int(newvalues[x])

valdict = dict
keydict = dict
values = []
time.sleep(1)
millis = int(round(time.time() * 1000)) - start
ser.write(str(millis))
while True:
    # read shit
    millis = int(round(time.time() * 1000)) - start
    values = process_serial(millis, valdict)
    key = process_keys(millis, keydict, valdict)
    print(str(key) + " " + str(values[0]) + " " + str(millis))

    # draw shit
    windowSurface.fill(pygame.Color(255, 255, 255, 255))
    if len(values) == 5:
        for x in range(1, 5):
            rectangles[x - 1].width = scale(int(values[x]))
            windowSurface.fill(colors[x - 1], rectangles[x - 1])
            textSurface = fontObj.render(values[x], False, pygame.Color("white"))
            textRect = textSurface.get_rect()
            textRect.topleft = rectangles[x-1].topleft
            windowSurface.blit(textSurface, textRect)
    pygame.display.update()
    fpsClock.tick(30)
