int raw = 0;           // variable to store the raw input value
int Vin = 5;           // variable to store the input voltage
float Vout = 0;        // variable to store the output voltage
float R1 = 10;         // variable to store the R1 value
float R2 = 0;          // variable to store the R2 value
float buffer = 0;      // buffer variable for calculation
// variable used as breaker between sensor values
int breaker = 20;
  
void setup()
{
  Serial.begin(9600);             // Setup serial
}

void loop()
{
    raw = analogRead(A0);
    int value0 = resistance(raw);
    raw = analogRead(A1);
    int value1 = resistance(raw);
    raw = analogRead(A2);
    int value2 = resistance(raw);
    raw = analogRead(A3);
    int value3 = resistance(raw);
    raw = analogRead(A4);
    int value4 = resistance(raw);
    raw = analogRead(A5);
    int value5 = resistance(raw);
  
    // print out value over the serial port
    Serial.write(byte(1)); //prefix
    Serial.write(value0);
    Serial.write(byte(breaker)); //end signal
        
    Serial.write(byte(2));
    Serial.write(value1);
    Serial.write(byte(breaker));
    
    Serial.write(byte(3));
    Serial.write(value2);
    Serial.write(byte(breaker));
    
    Serial.write(byte(4));
    Serial.write(value3);
    Serial.write(byte(breaker));
    
    Serial.write(byte(5));
    Serial.write(value4);
    Serial.write(byte(breaker));
    
    Serial.write(byte(6));
    Serial.write(value5);
    Serial.write(byte(breaker));
    
    // wait for a bit to not overload the port
    delay(100);
}

int resistance(int raw)
{
//  Vout = (5.0 / 1023.0) * raw;    // Calculates the Voltage on th Input PIN
//  buffer = (Vin / Vout) - 1;
//  R2 = R1 / buffer;
  return 1023 - raw;
}
