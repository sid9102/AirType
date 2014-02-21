import serial, pygame, random, sys, time
from pygame.locals import *
ser = serial.Serial('COM22', 9600)

pygame.init()
fpsClock = pygame.time.Clock()
windowSurface = pygame.display.set_mode((1200, 500))
fontObj = pygame.font.Font('freesansbold.ttf', 32)
start = int(round(time.time() * 1000))


def scale(x):
    return x // 7

rectangles = [pygame.Rect(0, 0, 100, 100), pygame.Rect(0, 125, 100, 100), pygame.Rect(0, 250, 100, 100), pygame.Rect(0, 375, 100, 100)]


def getrandomcolor():
    return pygame.Color(random.randrange(255), random.randrange(240), random.randrange(240), 255)

colors = [getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor()]

values = []
time.sleep(1)
millis = int(round(time.time() * 1000)) - start
ser.write(str(millis))
while True:
    values = ser.readline()[:-2].split("\t")
    millis = int(round(time.time() * 1000)) - start
    print(str(values[0]) + " " + str(millis))
    windowSurface.fill(pygame.Color(255, 255, 255, 255))
    if len(values) == 5:
        for x in range(1, 5):
            rectangles[x - 1].width = scale(int(values[x]))
            windowSurface.fill(colors[x - 1], rectangles[x - 1])
            textSurface = fontObj.render(values[x], False, pygame.Color("white"))
            textRect = textSurface.get_rect()
            textRect.topleft = rectangles[x-1].topleft
            windowSurface.blit(textSurface, textRect)
    for event in pygame.event.get():
        if event.type == KEYDOWN:
            if event.key == K_ESCAPE:
                pygame.event.post(pygame.event.Event(QUIT))
        elif event.type == QUIT:
            pygame.quit()
            sys.exit(0)
    pygame.display.update()
    fpsClock.tick(30)