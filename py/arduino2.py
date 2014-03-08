import serial, pygame, random, sys, time
from pygame.locals import *
ser = serial.Serial('/dev/tty.usbmodem641', 9600)

pygame.init()
fpsClock = pygame.time.Clock()
windowSurface = pygame.display.set_mode((1200, 800))
fontObj = pygame.font.Font('freesansbold.ttf', 32)
start = int(round(time.time() * 1000))


def scale(x):
    return x //7

rectangles = [pygame.Rect(0, 0, 100, 100), pygame.Rect(0, 100, 100, 100), pygame.Rect(0, 200, 100, 100), pygame.Rect(0, 300, 100, 100), pygame.Rect(0, 400, 100, 100), pygame.Rect(0, 500, 100, 100), pygame.Rect(0, 600, 100, 100), pygame.Rect(0, 700, 100, 100), ]


def getrandomcolor():
    return pygame.Color(random.randrange(255), random.randrange(240), random.randrange(240), 255)

colors = [getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor(), getrandomcolor()]

values = []
time.sleep(1)
millis = int(round(time.time() * 1000)) - start
ser.write(str(millis))
oldvalues = (0, 0, 0, 0, 0, 0, 0, 0)
diffs = ()
while True:
    values = tuple(ser.readline()[:-2].split(" "))
    values = map(int, values[:-1])
    millis = int(round(time.time() * 1000)) - start
    windowSurface.fill(pygame.Color(255, 255, 255, 255))
    if len(values) == 8:
        diffs = ()
        for x in range(8):
            diffs += (int(values[x]) - oldvalues[x],)
        oldvalues = values
        disp = diffs
        print(disp)
        for x in range(8):
            rectangles[x].width = scale(int(disp[x]))
            windowSurface.fill(colors[x], rectangles[x])
            textSurface = fontObj.render(str(disp[x]), False, pygame.Color("white"))
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
    fpsClock.tick(30)
