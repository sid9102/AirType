#!/usr/local/bin/python
from py4j.java_gateway import JavaGateway, GatewayClient
import pygame
import sys
import serial
import re
import time

def main():
    gw = JavaGateway()
    ser = serial.Serial('/dev/tty.usbmodem621')

    size = width, height = 1920,1080
    pygame.init()
    screen = pygame.display.set_mode(size)
    back = 31, 44, 53
    text_color = 245, 236, 217
    font = pygame.font.Font("/Users/pfista/Library/Fonts/ProximaNova-Light.otf", 150)
    cand_font = pygame.font.Font("/Users/pfista/Library/Fonts/ProximaNova-Light.otf", 80)

    fingers = {'resting':'-1','leftIndex':'3','leftMiddle':'2','leftRing':'1',
            'leftPinky':'0', 'rightIndex':'4','rightMiddle':'5','rightRing':'6',
            'rightPinky':'7', 'space':'8'};
    fing_inv = {v:k for k, v in fingers.items()}

    for i in range(20):
        values = ser.readline() # <number> <number> ...

    wordFrag = []

    paragraphText = ''
    altCombined = ''

    oldvalues = (0, 0, 0, 0, 0, 0, 0, 0, 0)
    diffs = ()
    start = int(round(time.time() * 1000))
    alts = []
    altIndex = 0
    partialWord = ""

    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT: 
                pygame.quit()

        values = tuple(ser.readline()[:-2].split(" "))
        values = map(int, values[:-1])
        millis = int(round(time.time() * 1000)) - start
        if len(values) == 9:
            diffs = ()
            for x in range(9):
                diffs += (int(values[x]) - oldvalues[x],)
            oldvalues = values

        finger = getFinger(diffs)

        if finger is None:
            finger = '-1'
        # if a space get the best word possible
        elif finger is '8': # space
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
        else:
            wordFrag.append(finger)
            digitWord = ''.join(wordFrag)
            partialWord = gw.entry_point.getWord(digitWord)
            alts = gw.entry_point.getAlts()
            alts.insert(0, partialWord)
            altCombined = ', '.join(alts)
            print alts
            print wordFrag

        # Update pygame screen
        chosenWord = font.render(paragraphText, True, text_color)
        candidates = cand_font.render(altCombined, True, text_color)

        screen.fill(back)
        screen.blit(chosenWord, (-font.size(paragraphText)[0]/2 + 1920/2, 1080/2 -150))
        screen.blit(candidates, (-cand_font.size(altCombined)[0]/2 + 1920/2, 1080/2 +100))
        pygame.display.flip()


def getFinger(val):
    max_value = max(val)
    if val[8] > 500:
        return '8' 
    if max_value > 1000:
        ind = val.index(max_value)
        for i in val:
           if i is not ind and val.index(i) > 1000:
                return None
        return str(ind)
    return None


if __name__ == "__main__":
    main()
