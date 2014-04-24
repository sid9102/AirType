class AirType():
    def __init__(self, pygame):
        self.wordFrag = []
        self.candidates = []
        self.pygame = pygame

        # Stuff used by pygame
        self.word = ''
        self.alts = []
        self.alt_index = 0
        self.demo = '   Introducing AirType. Typing has changed. Forever.'
        self.demo_idx = 0

    def handleFinger(self, finger, gw):

        if self.pygame.hit_space:
            self.pygame.hit_space = False
            self.demo_idx = 0
            # Cycle candidates
            if len(self.wordFrag) == 0:
                if self.alt_index < len(self.alts):
                    self.pygame.paragraphText = self.alts[self.alt_index]
                    self.alt_index += 1
                if self.alt_index >= len(self.alts):
                    self.alt_index = 0;
            else:
                # Get the best word
                digitWord = ''.join(self.wordFrag)
                self.pygame.paragraphText = gw.entry_point.getWord(digitWord)
                self.wordFrag = []
                self.alts = gw.entry_point.getAlts()
                self.alts.append(self.pygame.paragraphText)


        elif finger >= 0:

            # Temporary Demo 
            #self.demo_idx += 1
            #self.wordFrag.append(str(finger))
            #self.pygame.altCombined = self.demo[0:self.demo_idx]
            
            digitWord = ''.join(self.wordFrag)
            self.word = gw.entry_point.getWord(digitWord)
            self.alts = gw.entry_point.getAlts()
            self.alts.insert(0, self.word)

            self.pygame.altCombined = ', '.join(self.alts)
            self.pygame.paragraphText = self.word
