#include <Wire.h>
#include <CapacitiveSensor.h>




CapacitiveSensor   cs_1_11 = CapacitiveSensor(A0,A11);        // 1 megohm resistor between pins A1 & A11, pin A1 is sensor pin, add wire, foil
CapacitiveSensor   cs_2_10 = CapacitiveSensor(A1,A10);        // 1 megohm resistor between pins A2 & A10, pin A2 is sensor pin, add wire, foil
CapacitiveSensor   cs_3_9 = CapacitiveSensor(A2,A9);        // 1 megohm resistor between pins A3 & A9, pin A3 is sensor pin, add wire, foil
CapacitiveSensor   cs_4_8 = CapacitiveSensor(A3,A8);        // 1 megohm resistor between pins A4 & A8, pin A4 is sensor pin, add wire, foil
CapacitiveSensor   cs_5_7 = CapacitiveSensor(A4,A7);        // 1 megohm resistor between pins A5 & A7, pin A4 is sensor pin, add wire, foil
long values[5];
int breaker = 123;


void setup()
{
  Wire.begin(2);                // join i2c bus with address #2
  Wire.onRequest(requestEvent); // register event
  

    
}

void loop()
{

    values[0] =  cs_1_11.capacitiveSensor(30);
    values[1] =  cs_2_10.capacitiveSensor(30);
    values[2] =  cs_3_9.capacitiveSensor(30);
    values[3] =  cs_4_8.capacitiveSensor(30);
    values[4] =  cs_5_7.capacitiveSensor(30);
   
    Serial.print("\t");                    // tab character for debug window spacing
    Serial.print(values[0]);                  // print sensor output 1
    Serial.print("\t");
    Serial.print(values[1]);                  // print sensor output 2
    Serial.print("\t");
    Serial.print(values[2]);                // print sensor output 3
    Serial.print("\t");
    Serial.print(values[3]);               //print sensor output 4
    Serial.print("\t");
    Serial.println(values[4]);               //print sensor output 4

    
    delay(50);
}

    void requestEvent()
{
  
   //long start = millis();
    unsigned char buf[sizeof(long) * 5];
   for(int i = 0; i < 5; i++)
 {
    long x = values[i];
    memcpy(buf + (i * 4),&x,sizeof(long));
 }  
 Wire.write(buf, 20);
     //Wire.write("hello ");
}
// function that executes whenever data is requested by master
// this function is registered as an event, see setup()

