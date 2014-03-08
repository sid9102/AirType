#!/usr/local/bin/python
from py4j.java_gateway import JavaGateway, GatewayClient
import pygame
import sys
import serial
import re

def main():
    gw = JavaGateway(GatewayClient(port=25346))
    ser = serial.Serial('/dev/tty.usbmodem641')

    size = width, height = 1920, 1200
    pygame.init()
    screen = pygame.display.set_mode(size)
    back = 31, 44, 53
    text_color = 245, 236, 217
    font = pygame.font.Font("/Users/pfista/Library/Fonts/ProximaNova-Light.otf", 80)

    fingers = {'resting':'-1','leftIndex':'3','leftMiddle':'2','leftRing':'1',
            'leftPinky':'0', 'rightIndex':'4','rightMiddle':'5','rightRing':'6',
            'rightPinky':'7'};
    for i in range(20):
        values = ser.readline() # <number> <number> ...

    wordFrag = []

    paragraphText = ['word', 'list']

    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT: 
                pygame.quit()

        # Read AirType sensor data and display words
        values = ser.readline()[:-3] # <number> <number> ...
        print values
        finger = gw.entry_point.classifyData(values)
        print finger
        if finger is None:
            wordFrag.append(fingers['resting'])
            finger = fingers['resting']
        # if a space get the best word possible
        elif finger is 'space':
            digitWord = ''.join(wordFrag)
            print gw.entry_point.getWord(digitWord)
        elif finger is 'left':
            print 'go left'
        elif finger is 'right':
            print 'go right'

        # show candidates for the current word, have a way to cycle through
        alts = gw.entry_point.getAlts()

        # Update pygame screen
        if finger is not None:
            status = font.render(finger, True, text_color)

            pt = ' '.join(paragraphText)
            paragraph = font.render(str(pt), True, text_color)

            screen.fill(back)
            screen.blit(status, (-font.size(finger)[0]/2 + 1920/2, 0))
            screen.blit(paragraph, (-font.size(pt)[0]/2 + 1920/2, 100))
            pygame.display.flip()

def pint(val):
    try:
        return int(val)
    except ValueError:
        pass


if __name__ == "__main__":
    main()
