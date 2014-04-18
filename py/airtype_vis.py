#!/usr/local/bin/python
from py4j.java_gateway import JavaGateway, GatewayClient
import pygame
import sys
import serial
from serial import SerialException
import re
import time

def main():
    gw = JavaGateway()
    try:
        ser = serial.Serial('/dev/tty.usbmodem621')
    except OSError:
        print "No connection to the device could be established"
        sys.exit(0);
        
    size = width, height = 1920,1080
    pygame.init()
    screen = pygame.display.set_mode(size)

    # colors
    back = 31, 44, 53
    text_color = 245, 236, 217
    done_color = 255, 94, 64

    # Fonts
    font = pygame.font.Font("/Users/pfista/Library/Fonts/ProximaNova-Light.otf", 150)
    cand_font = pygame.font.Font("/Users/pfista/Library/Fonts/ProximaNova-Light.otf", 80)
    train_font = pygame.font.Font("/Users/pfista/Library/Fonts/ProximaNova-Light.otf", 60)

    # Set a limit to how fast to refresh?
    clock = pygame.time.Clock()
    clock.tick(60)

    fingers = {'resting':'-1','leftIndex':'3','leftMiddle':'2','leftRing':'1',
            'leftPinky':'0', 'rightIndex':'4','rightMiddle':'5','rightRing':'6',
            'rightPinky':'7', 'space':'8'};
    fing_inv = {v:k for k, v in fingers.items()}
    

    wordFrag = []
    paragraphText = ''
    altCombined = ''
    alts = []
    altIndex = 0
    partialWord = ""
    state = 'train'
    done_pressing = True

    tr = Trainer("the quick brown fox jumped over the lazy dog")

    pygame.event.set_allowed((pygame.QUIT, pygame.KEYDOWN))

    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT: 
                pygame.quit()

        values = tuple(ser.readline().split("\t"))
        values = map(int, values)
    
        if tr.active:
            # Update the index of what character we are mapping to a finger
            tr.trainValues(values)

            # Update pygame screen
            fm_done = train_font.render(tr.training_text[0:tr.train_idx], True, done_color)
            fm_pend = train_font.render(tr.training_text[tr.train_idx:], True, text_color)

            screen.fill(back)
            screen.blit(fm_done, (-train_font.size(tr.training_text)[0]/2 + 
                1920/2, 1080/2 +100))
            screen.blit(fm_pend, (-train_font.size(tr.training_text)[0]/2 + 1920/2 +
                train_font.size(tr.training_text[0:tr.train_idx])[0], 1080/2 +100))
            pygame.display.flip()

        else:
            finger = tr.handleKeypress(values)
            print finger

            if finger == -1:
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
            elif finger >= 0:
                finger = str(finger)
                wordFrag.append(finger)
                print ('wordfrag', wordFrag)
                digitWord = ''.join(wordFrag)
                print ('digitword', digitWord)
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

class Trainer():
    def __init__(self, training_text):
        self.press_done = True
        self.train_idx = 0
        self.training_text = training_text
        self.mapping = {}
        self.active = True

    def handleKeypress(self, data):
        """ Determine if any finger was pressed given the data, if so prevent
        further keypresses until user returns to normal position, return the
        finger that was pressed """

        max_value = max(data)
        fingerPressed = None
        if max_value > 300:
            ind = data.index(max_value)
            fingerPressed = ind

        if fingerPressed is not None:
            if self.press_done:
                self.press_done = False
                return fingerPressed
            elif not self.press_done:
                return -1
        else:
            self.press_done = True
        return -1

    def trainValues(self, values):
        """ Given input of finger thresholds, determine if the user made a
        keypress and associate the current character with a finger """
        finger_idx = self.handleKeypress(values)
        if finger_idx >= 0:
            # Check bounds
            if self.train_idx >= len(self.training_text):
                self.active = False
                return 
            # Skip spaces TODO: this won't handle consecutive spaces
            if self.training_text[self.train_idx] is ' ':
                self.train_idx+=1

            # Now associate this with a finger
            if finger_idx in self.mapping.keys():
                self.mapping[finger_idx].add(self.training_text[self.train_idx])
            else:
                self.mapping[finger_idx] = set([self.training_text[self.train_idx]])

            print self.mapping

            # increment the index
            self.train_idx+=1

if __name__ == "__main__":
    main()
