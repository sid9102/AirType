int raw = 0;           // variable to store the raw input value
int Vin = 5;           // variable to store the input voltage
float Vout = 0;        // variable to store the output voltage
float R1 = 10;         // variable to store the R1 value
float R2 = 0;          // variable to store the R2 value
float buffer = 0;      // buffer variable for calculation
// variable used as breaker between sensor values
int breaker = 255;

int values[10];
int oldValues[10];
  
void setup()
{
  Serial.begin(9600);             // Setup serial
}

void loop()
{
<<<<<<< HEAD
    values[0] = analogRead(A0);
    values[1] = analogRead(A1);
    values[2] = analogRead(A2);
    values[3] = analogRead(A3);
    values[4] = analogRead(A4);
    values[5] = analogRead(A5);
    values[6] = analogRead(A6);
    values[7] = analogRead(A7);
  
  
  
    // print out value over the serial port
    
    for(int i = 0; i < 8; i++)
    {
      Serial.write(byte(i + 1));
      Serial.write(absDiff(values[i], oldValues[i]));
      Serial.write(byte(breaker)); //end signal
      oldValues[i] = values[i];
    }
=======
  
   // TODO: eventually we should write this as bytes
   // instead of printlns to increase efficiency 
   Serial.println("break");
   Serial.println(analogRead(A0));
   Serial.println(analogRead(A1));
   Serial.println(analogRead(A2));
   Serial.println(analogRead(A3));
   Serial.println(analogRead(A4));
   Serial.println(analogRead(A5));
   Serial.println(analogRead(A6));
   Serial.println(analogRead(A7));
   Serial.println(analogRead(A8));
>>>>>>> 96cd185b8bd2901e16ffccffaf6164436ab4645a
    
    // wait for a bit to not overload the port
    delay(800);
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
