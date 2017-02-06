// - - - - -
// DmxSerial - A hardware supported interface to DMX.
// DmxSerialRecv.ino: Sample DMX application for retrieving 3 DMX values:
// address 1 (red) -> PWM Port 9
// address 2 (green) -> PWM Port 6
// address 3 (blue) -> PWM Port 5
// 
// Copyright (c) 2011-2015 by Matthias Hertel, http://www.mathertel.de
// This work is licensed under a BSD style license. See http://www.mathertel.de/License.aspx
// 
// Documentation and samples are available at http://www.mathertel.de/Arduino
// 25.07.2011 creation of the DmxSerial library.
// 10.09.2011 fully control the serial hardware register
//            without using the Arduino Serial (HardwareSerial) class to avoid ISR implementation conflicts.
// 01.12.2011 include file and extension changed to work with the Arduino 1.0 environment
// 28.12.2011 changed to channels 1..3 (RGB) for compatibility with the DmxSerialSend sample.
// 10.05.2012 added some lines to loop to show how to fall back to a default color when no data was received since some time.
// - - - - -

#include <DMXSerial.h>

// Constants for demo program

#define OUT_R 9
#define OUT_G 10
#define OUT_B 11

#define CHAN_R 20
#define CHAN_G 21
#define CHAN_B 22

#define RedDefaultLevel   255
#define GreenDefaultLevel 253
#define BlueDefaultLevel  38

void setup () {
  DMXSerial.init(DMXReceiver);
  
  // set some default values
  DMXSerial.write(1, 80);
  DMXSerial.write(2, 0);
  DMXSerial.write(3, 0);
  
  // enable pwm outputs
  pinMode(OUT_R,   OUTPUT); // sets the digital pin as output
  pinMode(OUT_G, OUTPUT);
  pinMode(OUT_B,  OUTPUT);
  pinMode(13,  OUTPUT);
}

long l = 0;
void loop() {
  // Calculate how long no data backet was received
  unsigned long lastPacket = DMXSerial.noDataSince();
  
  if (lastPacket < 5000) {
    // read recent DMX values and set pwm levels 
    analogWrite(OUT_R,   DMXSerial.read(CHAN_R));
    analogWrite(OUT_G, DMXSerial.read(CHAN_G));
    analogWrite(OUT_B,  DMXSerial.read(CHAN_B));  
    digitalWrite(13, LOW);  
  } else {
    // Show pure red color, when no data was received since 5 seconds or more.
    analogWrite(OUT_R,   RedDefaultLevel);
    analogWrite(OUT_G, GreenDefaultLevel);
    analogWrite(OUT_B,  BlueDefaultLevel);
    digitalWrite(13, HIGH);
  } // if

  l++;
}

// End.
