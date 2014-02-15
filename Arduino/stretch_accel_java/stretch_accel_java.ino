#include <Wire.h>
float buffer = 0;      // buffer variable for calculation
// variable used as breaker between sensor values
String breaker = "END";
int values[10];
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
#define SHIELD_ADDR_OFF_OFF_ON (0x74) 
#define SHIELD_ADDR_OFF_ON_OFF (0x72) 
#define SHIELD_ADDR_OFF_ON_ON (0x76) 
#define SHIELD_ADDR_ON_OFF_OFF (0x71) 
#define SHIELD_ADDR_ON_OFF_ON (0x75) 
#define SHIELD_ADDR_ON_ON_OFF (0x73) 
#define SHIELD_ADDR_ON_ON_ON (0x77) 
// Set the shield address here
const uint8_t shieldAddr = SHIELD_ADDR_OFF_OFF_OFF; 

void setup()
{
  Serial.begin(9600);             // Setup seria
  
  Wire.begin(); //Join the bus as a master
  
  for(int i = 0; i<2; i++){
    int y; 
    y = 1<<i; 
    Wire.beginTransmission(shieldAddr); 
    Wire.write(y); 
    Wire.endTransmission(); 
    initMMA8452(); //Test and intialize the MMA8452
  
  }
}

void loop()
{
    values[0] = analogRead(A7);
    values[1] = analogRead(A6);
    values[2] = analogRead(A5);
    values[3] = analogRead(A4);
    values[4] = analogRead(A3);
    values[5] = analogRead(A2);
    values[6] = analogRead(A1);
    values[7] = analogRead(A0);
    

    //Prints the time in ms since the program started running.
    Serial.println("TIME");
    Serial.println(millis());
    Serial.println("ENDTIME");    
    // print out value over the serial port    
    for(int i = 0; i < 8; i++)
    {
      Serial.println(i + 1);
      Serial.println(abs(values[i]));
      Serial.println(breaker); //end signal
      oldValues[i] = values[i];
    }
    
 for(int n = 0; n<2; n++){
   
   
    int z;
    z = 1<<n; 
    Wire.beginTransmission(shieldAddr); 
    Wire.write(z); 
    Wire.endTransmission(); 
    
    int accelCount[3];  // Stores the 12-bit signed value -accelerometer
    readAccelData(accelCount);  // Read the x/y/z adc values -accelerometer

  // Now we'll calculate the accleration value into actual g's
  float accelG[3];  // Stores the real accel value in g's
  for (int i = 0 ; i < 3 ; i++)
  {
    accelG[i] = (float) accelCount[i] / ((1<<12)/(2*GSCALE));  // get actual g value, this depends on scale being set
  }
  
    
    for (int i = 0 ; i < 3 ; i++)   //print accelerometer values
  {
    Serial.print(accelG[i], 4);  // Print g values
    Serial.print("\t");  // tabs in between axes
    
  }
  Serial.print(n); 
  Serial.println();
  
  }

    
    // wait for a bit to not overload the port
    delay(100);
}

int absDiff(int value, int oldValue)
{
  int result = value - oldValue;
  if(result < 0)
  {
    return 0;
  }
  return result * 10;
}

byte abs_byte(int value)
{
  value = abs(value);
  value >>= 2;
  return byte(value);
}

///////Accelerometer code

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
