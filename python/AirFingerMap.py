class AirFingerMap():
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
        self.pressThresh = .4

        # Custom thresholds per finger for now until I can add training for
        # individual finger ranges
        #idx 1 2 3 4 (right, index -> pinky)
        # 45 70 30 60
        #idx 4 5 6 7 (left, indez -> pinky)
        # 90 60 50 60    
        self.ranges = [30.0, 40.0, 50.0, 70.0, 70.0, 50.0, 40.0, 30.0]

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
                print 'Set initial tare values'
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
