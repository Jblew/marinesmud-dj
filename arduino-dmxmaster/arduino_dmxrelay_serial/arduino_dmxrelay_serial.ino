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
#define OUT_A 3
#define OUT_B 4
#define OUT_C 5
#define OUT_D 6

#define CHAN_A 50
#define CHAN_B 51
#define CHAN_C 52
#define CHAN_D 53

#define RELAY_ON LOW
#define RELAY_OFF HIGH


//255,150,5
void setup () {
  pinMode(OUT_A,   OUTPUT);
  pinMode(OUT_B, OUTPUT);
  pinMode(OUT_C,  OUTPUT);
  pinMode(OUT_D,   OUTPUT);

  digitalWrite(OUT_A, RELAY_OFF);
  digitalWrite(OUT_B, RELAY_OFF);
  digitalWrite(OUT_C, RELAY_OFF);
  digitalWrite(OUT_D, RELAY_OFF);
  
  DMXSerial.init(DMXReceiver);
  
  // set some default values
  DMXSerial.write(1, 80);
  DMXSerial.write(2, 0);
  DMXSerial.write(3, 0);
  
  
  
  
  pinMode(13,  OUTPUT);
}

void loop() {
  // Calculate how long no data backet was received
  unsigned long lastPacket = DMXSerial.noDataSince();
  
  if (lastPacket < 1000) {
    digitalWrite(OUT_A, (DMXSerial.read(CHAN_A) > 127)? RELAY_ON : RELAY_OFF);
    digitalWrite(OUT_B, (DMXSerial.read(CHAN_B) > 127)? RELAY_ON : RELAY_OFF);
    digitalWrite(OUT_C, (DMXSerial.read(CHAN_C) > 127)? RELAY_ON : RELAY_OFF);
    digitalWrite(OUT_D, (DMXSerial.read(CHAN_D) > 127)? RELAY_ON : RELAY_OFF);
    digitalWrite(13, LOW);  
  } else {
    digitalWrite(OUT_A,   RELAY_OFF);
    digitalWrite(OUT_B, RELAY_OFF);
    digitalWrite(OUT_C,  RELAY_OFF);
    digitalWrite(OUT_D,   RELAY_OFF);
    digitalWrite(13, HIGH);
  } // if

}

// End.
