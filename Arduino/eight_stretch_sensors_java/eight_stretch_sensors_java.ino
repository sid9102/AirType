float buffer = 0;      // buffer variable for calculation
// variable used as breaker between sensor values
String breaker = "END";
int values[10];
int oldValues[10];
unsigned long time;
boolean go = false;  
void setup()
{
  Serial.begin(9600);             // Setup serial
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
    //time = millis(); // Store the current time since the program began
  
    String data = "";
    //Prints the time in ms since the program started running.
    if (Serial.available() > 0)
    {
      go = !go;
      Serial.read();
    }
//    data.concat(time);
//    data.concat(":");
    // print out value over the serial port    
    for(int i = 0; i < 8; i++)
    {
      data.concat(abs(values[i]));
      if (i != 7){ 
        data.concat(":");
      }
      oldValues[i] = values[i];
    }
    if(go){
    Serial.println(data);
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
