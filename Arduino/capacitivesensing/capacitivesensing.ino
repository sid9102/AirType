#include <CapacitiveSensor.h>


/*
 * CapitiveSense Library Demo Sketch
 * Paul Badger 2008
 * Uses a high value resistor e.g. 10 megohm between send pin and receive pin
 * Resistor effects sensitivity, experiment with values, 50 kilohm - 50 megohm. Larger resistor values yield larger sensor values.
 * Receive pin is the sensor pin - try different amounts of foil/metal on this pin
 * Best results are obtained if sensor foil and wire is covered with an insulator such as paper or plastic sheet
 */


CapacitiveSensor   cs_0 = CapacitiveSensor(A0,A1);        // 1 megohm resistor between pins A0 & A1, pin A1 is sensor pin, add wire, foil
CapacitiveSensor   cs_1 = CapacitiveSensor(A2,A3);        // 1 megohm resistor between pins A0 & A2, pin A2 is sensor pin, add wire, foil
CapacitiveSensor   cs_2 = CapacitiveSensor(A4,A5);        // 1 megohm resistor between pins A0 & A3, pin A3 is sensor pin, add wire, foil
CapacitiveSensor   cs_3 = CapacitiveSensor(A10,A11);        // 1 megohm resistor between pins A0 & A4, pin A4 is sensor pin, add wire, foil
long values[4];
byte resolution = 100;
String startTime = String();
long time = 0;
long arduinoStart = millis();
bool setStart = true;

void setup()                    
{
   cs_0.set_CS_AutocaL_Millis(0xFFFFFFFF);     // turn off autocalibrate on channel 1 - just as an example
   cs_1.set_CS_AutocaL_Millis(0xFFFFFFFF);
   cs_2.set_CS_AutocaL_Millis(0xFFFFFFFF);
   cs_3.set_CS_AutocaL_Millis(0xFFFFFFFF);
   Serial.begin(9600);
}

void loop()                    
{
    long start = millis();
    // For synchronisation between the arduino and the computer
    while(startTime.length() < 2){
      while (Serial.available()) {
        delay(3);  //delay to allow buffer to fill 
        if (Serial.available() >0) {
          char c = Serial.read();  //gets one byte from serial buffer
          startTime += c; //makes the string readString
        } 
      }
    }
    if(setStart && startTime.length() >= 2){
      arduinoStart = millis();
      setStart = false;
      time = startTime.toInt();
    }
    values[0] =  cs_0.capacitiveSensor(resolution);
    values[1] =  cs_1.capacitiveSensor(resolution);
    values[2] =  cs_2.capacitiveSensor(resolution);
    values[3] =  cs_3.capacitiveSensor(resolution);

    Serial.print(arduinoStart - millis() + time);        // check on performance in milliseconds
    
    Serial.print("\t");                    // tab character for debug window spacing
    Serial.print(values[0]);                  // print sensor output 1
    Serial.print("\t");
    Serial.print(values[1]);                  // print sensor output 2
    Serial.print("\t");
    Serial.print(values[2]);                // print sensor output 3
    Serial.print("\t");
    Serial.println(values[3]);               //print sensor output 4

    delay(50);                             // arbitrary delay to limit data to serial port 
}
