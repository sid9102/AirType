import pygame

class pygame_demo():
    def __init__(self):
        size = width, height = 1920,1080
        pygame.init()
        self.screen = pygame.display.set_mode(size)
        pygame.display.set_icon(pygame.image.load('assets/icon32.png').convert_alpha())

        # colors
        self.back = 31, 44, 53
        self.text_color = 245, 236, 217
        self.done_color = 255, 94, 64

        # Fonts
        self.font = pygame.font.Font("/Users/pfista/Library/Fonts/ProximaNova-Light.otf", 150)
        self.cand_font = pygame.font.Font("/Users/pfista/Library/Fonts/ProximaNova-Light.otf", 80)
        self.train_font = pygame.font.Font("/Users/pfista/Library/Fonts/ProximaNova-Light.otf", 60)

        # Set a limit to how fast to refresh?
        clock = pygame.time.Clock()
        clock.tick(60)

        pygame.event.set_allowed((pygame.QUIT, pygame.KEYDOWN))

    def loop(self, tr):
        """ This is the main loop where pygame will draw its assets.  It should
        only be reading data from a Trainer class and updating the state on the
        screen. There are only a few keypreses pygame should handle that will
        update state information in Trainer. Other than that, it shouldn't do
        much controlling, except for drawing the view """
        for event in pygame.event.get():
            if event.type == pygame.QUIT: 
                pygame.quit()
                sys.exit(0)
            if event.type == pygame.KEYDOWN:
                if tr.mode is'ready':
                    tr.mode = 'tareRest'
                if event.key == pygame.K_ESCAPE:
                    pygame.event.post(pygame.event.Event(pygame.QUIT))
                if event.key == pygame.K_SPACE:
                    pass # TODO: this is a control incident

        if tr.mode is 'train':
            #TODO : need to generalize the UI
            # Update pygame screen
            fm_done = self.train_font.render(tr.training_text[0:tr.train_idx], True, self.done_color)
            fm_pend = self.train_font.render(tr.training_text[tr.train_idx:], True, self.text_color)

            self.screen.fill(self.back)
            self.screen.blit(fm_done, (-self.train_font.size(tr.training_text)[0]/2 + 
                1920/2, 1080/2 +100))
            self.screen.blit(fm_pend, (-self.train_font.size(tr.training_text)[0]/2 + 1920/2 +
                self.train_font.size(tr.training_text[0:tr.train_idx])[0], 1080/2 +100))
            pygame.display.flip()
        elif tr.mode is 'airtype':
            # Update pygame screen
            chosenWord = self.font.render(paragraphText, True, text_color)
            candidates = self.cand_font.render(altCombined, True, text_color)

            self.screen.fill(self.back)
            self.screen.blit(chosenWord, (-self.font.size(paragraphText)[0]/2 + 1920/2, 1080/2 -150))
            self.screen.blit(candidates, (-self.cand_font.size(altCombined)[0]/2 + 1920/2, 1080/2 +100))
            pygame.display.flip()
