#include <CapacitiveSensor.h>
#include <Wire.h>

/*
 * CapitiveSense Library Demo Sketch
 * Paul Badger 2008
 * Uses a high value resistor e.g. 10 megohm between send pin and receive pin
 * Resistor effects sensitivity, experiment with values, 50 kilohm - 50 megohm. Larger resistor values yield larger sensor values.
 * Receive pin is the sensor pin - try different amounts of foil/metal on this pin
 * Best results are obtained if sensor foil and wire is covered with an insulator such as paper or plastic sheet
 */

float buffer = 0;      // buffer variable for calculation
// variable used as breaker between sensor values
int oldValues[10];

#define MMA8452_ADDRESS 0x1D  // 0x1D if SA0 is high, 0x1C if low
//Define a few of the registers that we will be accessing on the MMA8452
#define OUT_X_MSB 0x01
#define XYZ_DATA_CFG  0x0E
#define WHO_AM_I   0x0D
#define CTRL_REG1  0x2A

#define GSCALE 2 // Sets full-scale range to +/-2, 4, or 8g. Used to calc real g values.

// Possible shield addresses (suffix correspond to DIP switch positions)
#define SHIELD_ADDR_OFF_OFF_OFF (0x70)
// Set the shield address here
const uint8_t shieldAddr = SHIELD_ADDR_OFF_OFF_OFF; 



CapacitiveSensor   cs_1_11 = CapacitiveSensor(A0,A11);        // 1 megohm resistor between pins A1 & A11, pin A1 is sensor pin, add wire, foil
CapacitiveSensor   cs_2_10 = CapacitiveSensor(A1,A10);        // 1 megohm resistor between pins A2 & A10, pin A2 is sensor pin, add wire, foil
CapacitiveSensor   cs_3_9 = CapacitiveSensor(A2,A9);        // 1 megohm resistor between pins A3 & A9, pin A3 is sensor pin, add wire, foil
CapacitiveSensor   cs_4_8 = CapacitiveSensor(A3,A8);        // 1 megohm resistor between pins A4 & A8, pin A4 is sensor pin, add wire, foil
//CapacitiveSensor   cs_5_7 = CapacitiveSensor(A4,A7);        // 1 megohm resistor between pins A5 & A7, pin A4 is sensor pin, add wire, foil
long values[10];
int breaker = 123;

void setup()                    
{
  Wire.begin(); 
  cs_1_11.set_CS_AutocaL_Millis(0xFFFFFFFF);     // turn off autocalibrate on channel 1 - just as an example
   cs_2_10.set_CS_AutocaL_Millis(0xFFFFFFFF);
   cs_3_9.set_CS_AutocaL_Millis(0xFFFFFFFF);
   cs_4_8.set_CS_AutocaL_Millis(0xFFFFFFFF);
//    cs_5_7.set_CS_AutocaL_Millis(0xFFFFFFFF);
   Serial.begin(9600);

}

void loop()                    
{
    long start = millis();
    
   
    values[0] =  cs_1_11.capacitiveSensor(50);
    values[1] =  cs_2_10.capacitiveSensor(50);
    values[2] =  cs_3_9.capacitiveSensor(50);
    values[3] =  cs_4_8.capacitiveSensor(50);
//    values[4] =  cs_5_7.capacitiveSensor(30);
    
    Serial.print(millis() - start);        // check on performance in milliseconds
      Wire.requestFrom(2, 20, true);
      if(Wire.available() == 20){
        for(int i = 0; i < 5; i++)
        {
          values[5 + i] = 0;
          for(int j = 0; j < 4; j++)
          {
            int x = Wire.read();
            x <<= j * 8;
            values[5 + i] += x;          
          }
        }
      }
    
    // Left hand  
    Serial.print("\t");                    // tab character for debug window spacing
    Serial.print(values[5]);                  // print sensor output 1
    Serial.print("\t");
    Serial.print(values[6]);                  // print sensor output 2
    Serial.print("\t");
    Serial.print(values[7]);                // print sensor output 3
    Serial.print("\t");
    Serial.print(values[8]);               //print sensor output 4
//    Serial.print("\t");
//    Serial.print(values[9]);               //print sensor output 4
    
    //Right hand
    Serial.print("\t");                    // tab character for debug window spacing
    Serial.print(values[0]);                  // print sensor output 1
    Serial.print("\t");
    Serial.print(values[1]);                  // print sensor output 2
    Serial.print("\t");
    Serial.print(values[2]);                // print sensor output 3
    Serial.print("\t");
    Serial.println(values[3]);               //print sensor output 4
//    Serial.print("\t");
//    Serial.println(values[4]);               //print sensor output 4
    
    
    
    
    
    //////////////////////////////////ACCELEROMETER///////////////////////////////////
//    //////////////////////////////////////////////////////////////////////////////////
//     for(int n = 0; n<2; n++){                  // SEND ACCELEROMETER REQUESTS TO I2C EXPANSION BOARD *2 PORTS*
//   
//   
//    int z;                                                    
//    z = 1<<n; 
//    Wire.beginTransmission(shieldAddr); 
//    Wire.write(z); 
//    Wire.endTransmission(); 
//    
//    int accelCount[3];  // Stores the 12-bit signed value -accelerometer
//    readAccelData(accelCount);  // Read the x/y/z adc values -accelerometer
//
//  // Now we'll calculate the accleration value into actual g's
//    float accelG[3];  // Stores the real accel value in g's
//    for (int i = 0 ; i < 3 ; i++)
//    {
//      accelG[i] = (float) accelCount[i] / ((1<<12)/(2*GSCALE));  // get actual g value, this depends on scale being set
//    }
//  
//    
//    for (int i = 0 ; i < 3 ; i++)   //print accelerometer values
//    {
//    Serial.print("\t");  // tabs in between axes
//    Serial.print(accelG[i], 4);  // Print g values
//    
//    
//    }
//  //Serial.print(n); 
//  Serial.println();
//  
//  }
  
  //////////////////////////END ACCELEROMETER///////////////////////
  /////////////////////////////////////////////////////////////////
  
  
   
    

    delay(50);                             // arbitrary delay to limit data to serial port 
}



///////////////////////ACCELEROMETER FUNCTIONS////////////////////////
//////////////////////////////////////////////////////////////////////

void readAccelData(int *destination)
{
  byte rawData[6];  // x/y/z accel register data stored here

  readRegisters(OUT_X_MSB, 6, rawData);  // Read the six raw data registers into data array

  // Loop to calculate 12-bit ADC and g value for each axis
  for(int i = 0; i < 3 ; i++)
  {
    int gCount = (rawData[i*2] << 8) | rawData[(i*2)+1];  //Combine the two 8 bit registers into one 12-bit number
    gCount >>= 4; //The registers are left align, here we right align the 12-bit integer

    // If the number is negative, we have to make it so manually (no 12-bit data type)
    if (rawData[i*2] > 0x7F)
    {  
      gCount = ~gCount + 1;
      gCount *= -1;  // Transform into negative 2's complement #
    }

    destination[i] = gCount; //Record this gCount into the 3 int array
  }
}

// Initialize the MMA8452 registers 
void initMMA8452()
{
  byte c = readRegister(WHO_AM_I);  // Read WHO_AM_I register
  if (c == 0x2A) // WHO_AM_I should always be 0x2A
  {  
    Serial.println("MMA8452Q is online...");
  }
  else
  {
    Serial.print("Could not connect to MMA8452Q: 0x");
    Serial.println(c, HEX);
    while(1) ; // Loop forever if communication doesn't happen
  }

  MMA8452Standby();  // Must be in standby to change registers

  // Set up the full scale range to 2, 4, or 8g.
  byte fsr = GSCALE;
  if(fsr > 8) fsr = 8; //Easy error check
  fsr >>= 2; // Neat trick, see page 22. 00 = 2G, 01 = 4A, 10 = 8G
  writeRegister(XYZ_DATA_CFG, fsr);

  //The default data rate is 800Hz and we don't modify it in this example code

  MMA8452Active();  // Set to active to start reading
}

// Sets the MMA8452 to standby mode. It must be in standby to change most register settings
void MMA8452Standby()
{
  byte c = readRegister(CTRL_REG1);
  writeRegister(CTRL_REG1, c & ~(0x01)); //Clear the active bit to go into standby
}

// Sets the MMA8452 to active mode. Needs to be in this mode to output data
void MMA8452Active()
{
  byte c = readRegister(CTRL_REG1);
  writeRegister(CTRL_REG1, c | 0x01); //Set the active bit to begin detection
}

// Read bytesToRead sequentially, starting at addressToRead into the dest byte array
void readRegisters(byte addressToRead, int bytesToRead, byte * dest)
{
  Wire.beginTransmission(MMA8452_ADDRESS);
  Wire.write(addressToRead);
  Wire.endTransmission(false); //endTransmission but keep the connection active

  Wire.requestFrom(MMA8452_ADDRESS, bytesToRead); //Ask for bytes, once done, bus is released by default

  while(Wire.available() < bytesToRead); //Hang out until we get the # of bytes we expect

  for(int x = 0 ; x < bytesToRead ; x++)
    dest[x] = Wire.read();    
}

// Read a single byte from addressToRead and return it as a byte
byte readRegister(byte addressToRead)
{
  Wire.beginTransmission(MMA8452_ADDRESS);
  Wire.write(addressToRead);
  Wire.endTransmission(false); //endTransmission but keep the connection active

  Wire.requestFrom(MMA8452_ADDRESS, 1); //Ask for 1 byte, once done, bus is released by default

  while(!Wire.available()) ; //Wait for the data to come back
  return Wire.read(); //Return this one byte
}

// Writes a single byte (dataToWrite) into addressToWrite
void writeRegister(byte addressToWrite, byte dataToWrite)
{
  Wire.beginTransmission(MMA8452_ADDRESS);
  Wire.write(addressToWrite);
  Wire.write(dataToWrite);
  Wire.endTransmission(); //Stop transmitting
}

/////////////////////////////END ACCELEROMETER FUNCTIONS/////////////////
/////////////////////////////////////////////////////////////////////////




