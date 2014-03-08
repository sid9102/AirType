#include <EasyTransfer.h>
#include <CapacitiveSensor.h>

CapacitiveSensor   cs_0_11 = CapacitiveSensor(A0,A11);        
CapacitiveSensor   cs_1_10 = CapacitiveSensor(A1,A10);        
CapacitiveSensor   cs_2_9 = CapacitiveSensor(A2,A9);        
CapacitiveSensor   cs_3_8 = CapacitiveSensor(A3,A8);
long values[8];
EasyTransfer ET; 

struct SEND_DATA_STRUCTURE{
  //put your variable definitions here for the data you want to receive
  //THIS MUST BE EXACTLY THE SAME ON THE OTHER ARDUINO
  long values[4];
};
SEND_DATA_STRUCTURE mydata;

void setup() {
//   cs_0_11.set_CS_AutocaL_Millis(0xFFFFFFFF);     // turn off autocalibrate on channel 1 - just as an example
//   cs_1_10.set_CS_AutocaL_Millis(0xFFFFFFFF);
//   cs_2_9.set_CS_AutocaL_Millis(0xFFFFFFFF);
//   cs_3_8.set_CS_AutocaL_Millis(0xFFFFFFFF);
   Serial1.begin(9600);
   Serial.begin(9600);
   ET.begin(details(mydata), &Serial1);
}

void loop() {
    mydata.values[0] =  cs_0_11.capacitiveSensor(50);
    mydata.values[1] =  cs_1_10.capacitiveSensor(50);
    mydata.values[2] =  cs_2_9.capacitiveSensor(50);
    mydata.values[3] =  cs_3_8.capacitiveSensor(50);
    
    for(int i = 0; i < 4; i++)
    {
     Serial.print(mydata.values[i]);
      Serial.print(" ");
    }
    Serial.println();
    
    ET.sendData();
    delay(100);
}
