#!/ur/local/bin/python
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
        ser = serial.Serial('/dev/tty.usbmodem641')
    except OSError:
        print "No connection to the device could be established"
        sys.exit(0);

    size = width, height = 1920,1080
    pygame.init()
    screen = pygame.display.set_mode(size)
    pygame.display.set_icon(pygame.image.load('res/icon32.png').convert_alpha())

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

    wordFrag = []
    paragraphText = ''
    altCombined = ''
    alts = []
    altIndex = 0
    partialWord = ""
    done_pressing = True

    tr = Trainer("the quick' brown fox jumped over the lazy-dogs")

    pygame.event.set_allowed((pygame.QUIT, pygame.KEYDOWN))

    hit_space = False

    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT: 
                pygame.quit()
                sys.exit(0)
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_ESCAPE:
                    pygame.event.post(pygame.event.Event(pygame.QUIT))
                if event.key == pygame.K_SPACE:
                    hit_space = True

        values = tuple(ser.readline().split("\t"))
        values = map(int, values)
        if tr.mode is 'ready':
            for event in pygame.event.get():
                if event.type == pygame.KEYUP: 
                    tr.mode = 'tareRest'
        elif tr.mode is 'tareRest':
            # Get the average of 10 values, set this as the base
            tr.tareRest(values)
        elif tr.mode is 'tarePress':
            # Get the average of 10 values, set this as the base
            tr.tarePress(values)
    
        elif tr.mode is 'train':
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

        elif tr.mode is 'generate':
            tr.generatePermutations()
            gw.entry_point.initAirType()


        else:
            finger = tr.handleKeypress(values)

            if finger == -1:
                finger = '-1'
            # if a space get the best word possible
            elif hit_space:
                hit_space = False
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
        self.mode = 'ready'
        self.tareThresh = 10
        self.tareRestValues = None # Used for averaging the floor threshold
        self.tarePressValues = None # Used for averaging the ceiling threshold
        self.restOffsets = None
        self.pressOffsets = None
        self.pressThresh = .2

        # Custom thresholds per finger for now until I can add training for
        # individual finger ranges
        #idx 1 2 3 4 (right, index -> pinky)
        # 45 70 30 60
        #idx 4 5 6 7 (left, indez -> pinky)
        # 90 60 50 60    
        self.ranges = [45.0, 70.0, 30.0, 60.0, 90.0, 60.0, 50.0, 60.0]


        # Used for generating permutations
        self.freq_dict = {}

    def handleKeypress(self, data):
        """Determine if any finger was pressed given the data, if so prevent
        further keypresses until user returns to normal position, return the
        finger that was pressed"""

        data = [i - j for i, j in zip(data, self.restOffsets)]

        #TODO TEMP
        ratios = [ i/j for i,j in zip(data, self.ranges)]

        max_value = max(ratios)
        fingerPressed = None
        if max_value > self.pressThresh:
            ind = ratios.index(max_value)
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
                self.mode = 'generate'
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
            self.train_idx += 1

    def tareRest(self, data):
        if self.tareThresh > 0:
            if self.tareRestValues is None:
                self.tareRestValues = data
            else:
                self.tareRestValues = map(sum, zip(self.tareRestValues, data))

            self.tareThresh -= 1

        else:
            self.restOffsets = [x//10 for x in self.tareRestValues]
            print 'average', self.tareRestValues
            #self.mode = 'generate' # TODO
            self.tareTresh = 10
            self.mode = 'train'

    def tarePress(self, data):
        if self.tareThresh > 0:
            if self.tarePressValues is None:
                self.tarePressValues = data
            else:
                self.tarePressValues = map(sum, zip(self.tarePressValues, data))

            self.tareThresh -= 1
        else:
            self.pressOffsets = [x//10 for x in self.tarePressValues]
            print 'average',self.tarePressValues
            #self.mode = 'generate' # TODO
            self.mode = 'train'

    def generatePermutations(self):
        # take all words from a dictionary and generate their 'word #'
        # use the word number as a key, and a word list as the values
        # sort the values based on frequency

        # Reverse the mapping
        inv_mapping = dict( (v,k) for k in self.mapping for v in self.mapping[k] )

        # Get frequencies of words, store as a dictionary
        freq = open('freqList.csv', 'r')
        for line in freq.readlines():
            parts = line.strip().split(',')
            word = parts[2].lower()
            frequency = parts[3]
            self.freq_dict[word] = frequency
        freq.close()
        
        permutations = {}
        # Open the dictionary of words
        f = open('6of12.txt', 'r')
        for line in f.readlines():
            line = line.lower()
            line = line.strip()
            if line[-1] == '=':
                line = line[:-1]
            if re.search(r"[^A-Za-z\'-]", line) is None:
                # Generate a numberword
                result = str()
                for letter in line:
                   result += str(inv_mapping[letter])

                # Put the numberword as a key, and a list of words it can
                # represent as the value (sorted by frequency)
                result = int(result)
                if result in permutations:
                    # Values exist for this key already, so do an insertion sort
                    # based on frequency
                    permutations[result].append(line)
                    try:
                        permutations[result] = sorted(permutations[result], key=lambda k: self.freq_dict[k])
                    except KeyError:
                        pass
                else:
                    # Empty, just add as a list
                    permutations[result] = [line]
        f.close()

        # Now store the permutation dict in a file
        f = open('../permutations.txt', 'wb') 
        for numberword, words in sorted(permutations.items()):
            f.write(str(numberword)+':\n')
            for word in words:
                f.write(word+'\n')
        f.close()

        self.mode = 'airtype'

    # Key function used for sorting a list of words based on frequency
    def freqKey(self, val):
        return self.freq_dict[val]

if __name__ == "__main__":
    main()
