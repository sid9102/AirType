#!/usr/local/bin/python
import serial, pygame, random, sys, time
from pygame.locals import *
ser = serial.Serial('/dev/tty.usbmodem641')

pygame.init()
fpsClock = pygame.time.Clock()
windowSurface = pygame.display.set_mode((1920, 1080))
fontObj = pygame.font.Font('/Users/pfista/Library/Fonts/ProximaNova-Light.otf', 30)
start = int(round(time.time() * 1000))


rectangles = []
for x in range(0, 1080, 1080/8):
    rectangles.append(pygame.Rect(0, x, 1080/8, 1080/8))

def getrandomcolor():
    return pygame.Color(random.randrange(255), random.randrange(240), random.randrange(240), 255)

colors = [getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor()]

colors = [pygame.Color(207, 240, 158),
            pygame.Color(168, 219, 168),
            pygame.Color(121, 189, 154),
            pygame.Color(59, 134, 134),
            pygame.Color(11, 72, 107),
            pygame.Color(59, 134, 134),
            pygame.Color(121, 189, 154),
            pygame.Color(168, 219, 168)]



values = []
time.sleep(1)
millis = int(round(time.time() * 1000)) - start


print ser.readline()
start_values = tuple(ser.readline().split("\t"))
start_values = map(int, start_values)
print '##########', start_values

while True:
    values = tuple(ser.readline().split("\t"))
    values = map(int, values)
    values = [i - j for i,j in zip(values, start_values)]
    values = [x + 500 for x in values]
    windowSurface.fill(pygame.Color(36, 36, 36))
    if len(values) == 8:
        for x in range(8):
            rectangles[x].width = int((values[x]-200)*3.6)
            windowSurface.fill(colors[x], rectangles[x])
            textSurface = fontObj.render(str(values[x]), False, pygame.Color("white"))
            textRect = textSurface.get_rect()
            textRect.topleft = rectangles[x].topleft
            windowSurface.blit(textSurface, textRect)
    for event in pygame.event.get():
        if event.type == KEYDOWN:
            if event.key == K_ESCAPE:
                pygame.event.post(pygame.event.Event(QUIT))
        elif event.type == QUIT:
            pygame.quit()
            sys.exit(0)
    pygame.display.update()
    fpsClock.tick(80)

#1 2 3 4
# 45 70 30 60

# 4 5 6 7 (left, indez -> pinky)
# 90 60 50 60    
