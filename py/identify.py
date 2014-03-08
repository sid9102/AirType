#!/usr/local/bin/python
import serial, pygame, random, sys, time
from pygame.locals import *
ser = serial.Serial('/dev/tty.usbmodem641')

def scale(x):
    return x / 7

def getrandomcolor():
    return pygame.Color(random.randrange(255), random.randrange(240), random.randrange(240), 255)

pygame.init()
fpsClock = pygame.time.Clock()
windowSurface = pygame.display.set_mode((1200, 750))
fontObj = pygame.font.Font('freesansbold.ttf', 32)
start = int(round(time.time() * 1000))
rectangles = [pygame.Rect(0, 0, 100, 100), pygame.Rect(0, 125, 100, 100), pygame.Rect(0, 250, 100, 100), pygame.Rect(0, 375, 100, 100), pygame.Rect(0, 500, 100, 100), pygame.Rect(0, 625, 100, 100)]
colors = [getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor()]

findex = 0
fingers = ["resting","leftPinky","leftRing","leftMiddle","leftIndex","rightIndex","rightMiddle","rightRing","rightPinky"]

while findex < len(fingers):
    values = ser.readline()[:-3].split(" ")
    valuestr = ""
    for value in values:
        valuestr += " " + str(value)
    print(fingers[findex] + str(valuestr))
        
    windowSurface.fill(pygame.Color(255, 255, 255, 255))
    if len(values) == len(rectangles):
        for x in range(len(rectangles)):
            if("." in values[x]):
                rectangles[x].width = scale(float(values[x]))
                windowSurface.fill(colors[x - 1], rectangles[x - 1])
                textSurface = fontObj.render(values[x], False, pygame.Color("white"))
                textRect = textSurface.get_rect()
                textRect.topleft = rectangles[x-1].topleft
                windowSurface.blit(textSurface, textRect)

    for event in pygame.event.get():
        if event.type == KEYDOWN:
            if event.key == K_ESCAPE:
                pygame.event.post(pygame.event.Event(QUIT))
            else:
                if event.key == K_RETURN:
                    findex+=1
        elif event.type == QUIT:
            pygame.quit()
            sys.exit(0)
            
    pygame.display.update()
    fpsClock.tick(30)
