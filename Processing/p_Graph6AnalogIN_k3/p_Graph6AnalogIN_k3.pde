/*
Code based on “Arduino meets Processing” Potentiometer example
>> http://webzone.k3.mah.se/projects/arduino-workshop/projects/arduino_meets_processing/instructions/poti.html

Reads 4 analog inputs and visualizes them by drawing a 2x2 grid,
using grayscale shading of each square to represent sensor value.
>> http://www.kobakant.at/DIY/?cat=347
*/

import processing.serial.*;

// definition of window size
// you can change the size of the window as you like
// the thresholdGraph will be scaled to fit
// the optimal size for the thresholdGraph is 1000 x 400
int xWidth = 1200;
int yHeight = 750;

// xPos input array, using prefix
int[] xPosArr= {0,0,0,0,0,0,0,0}; 


int[] messageArr= {0,0,0,0,0,0,0,0}; 
  
// Arrays for threshholding
int[] threshMax= {0,0,0,0,0,0,0,0}; 
int[] threshMin= {0,0,0,0,0,0,0,0}; 
  
// variables for serial connection. portname and baudrate are user specific
Serial port1;

//Set your serial port here (look at list printed when you run the application once)
String V3 = Serial.list()[0];
String portname1 = "COM22";
int baudrate = 9600;
  
int prefix = 1;
boolean myCatch = true;
int serialIN = 0;
int serialINPUT = 0; 
String buffer = ""; 
int value = 0;
int breaker = 123;

// ThresholdGraph draws grid and poti states
ThresholdGraph in;

void setup(){
  // set size and framerate
  size(xWidth, yHeight);
  frameRate(25);
  background(255);
  strokeWeight(5);
  stroke(0);
  smooth();
  strokeCap(ROUND);

  // establish serial port connection      
  port1 = new Serial(this, portname1, baudrate);
  println(Serial.list());  // print serial list

  // create DisplayItems object
  in = new ThresholdGraph();
  
  // THRESHOLD VALUES:
  // using the thresholdGraph you can determine the MIN and MAX values
  // of your sensors. Enter these here. They must lie between 0 and 1000.
  
    //MIN 
    threshMin[0] = 0;   // one
    threshMin[1] = 0;   // two
    threshMin[2] = 0;   // three
    threshMin[3] = 0;   // four
    threshMin[4] = 0;   // five
    threshMin[5] = 0;   // six
    threshMin[6] = 0;  //seven
    threshMin[7] = 0;    //eight
   
    
    //MAX 
    threshMax[0] = 999;   // one
    threshMax[1] = 999;   // two
    threshMax[2] = 999;   // three
    threshMax[3] = 999;   // four
    threshMax[4] = 999;   // five
    threshMax[5] = 999;   // six
    threshMax[6] = 999;   // seven
    threshMax[7] = 999;   // eight
}//end setup




// draw listens to serial port, draw 
void draw(){
  
  // listen to serial port and trigger serial event  
  while(port1.available() > 0){
        
        for(int i = 0; i < 8; i++)
          {
            serialIN = port1.read();
            if(serialIN == -1)
            {
              break;
            }
            if(serialIN < 9)
            {
              int val = 0;
              xPosArr[serialIN - 1] = port1.read() * 4;
            }
          }
        }
        
  // threshold serial input  
  threshHolding();  

  // draw serial input
  in.update();
}//end draw()
