import pygame

class pygame_demo():
    def __init__(self):
        size = width, height = 1920,1080
        pygame.init()
        screen = pygame.display.set_mode(size)
        pygame.display.set_icon(pygame.image.load('assets/icon32.png').convert_alpha())

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

        pygame.event.set_allowed((pygame.QUIT, pygame.KEYDOWN))

    def loop(tr):
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
                if event.key == pygame.K_ESCAPE:
                    pygame.event.post(pygame.event.Event(pygame.QUIT))
                if event.key == pygame.K_SPACE:
                    pass # TODO: this is a control incident

        # Update the index of what character we are mapping to a finger
        # TODO: this needs to be updated in the main loop
        tr.trainValues(values)

        #TODO : need to generalize the UI
        # Update pygame screen
        fm_done = train_font.render(tr.training_text[0:tr.train_idx], True, done_color)
        fm_pend = train_font.render(tr.training_text[tr.train_idx:], True, text_color)

        screen.fill(back)
        screen.blit(fm_done, (-train_font.size(tr.training_text)[0]/2 + 
            1920/2, 1080/2 +100))
        screen.blit(fm_pend, (-train_font.size(tr.training_text)[0]/2 + 1920/2 +
            train_font.size(tr.training_text[0:tr.train_idx])[0], 1080/2 +100))
        pygame.display.flip()


        # Update pygame screen
        chosenWord = font.render(paragraphText, True, text_color)
        candidates = cand_font.render(altCombined, True, text_color)

        screen.fill(back)
        screen.blit(chosenWord, (-font.size(paragraphText)[0]/2 + 1920/2, 1080/2 -150))
        screen.blit(candidates, (-cand_font.size(altCombined)[0]/2 + 1920/2, 1080/2 +100))
        pygame.display.flip()


