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

#define OUT_G1 3
#define OUT_R1 5
#define OUT_B1 6
#define OUT_B2 9
#define OUT_R2 10
#define OUT_G2 11

#define CHAN_R1 38
#define CHAN_G1 39
#define CHAN_B1 40
#define CHAN_R2 41
#define CHAN_G2 42
#define CHAN_B2 43

#define RedDefaultLevel1   254
#define GreenDefaultLevel1 150
#define BlueDefaultLevel1  5

#define RedDefaultLevel2   254
#define GreenDefaultLevel2 100
#define BlueDefaultLevel2  10


//255,150,5
void setup () {
  DMXSerial.init(DMXReceiver);
  
  // set some default values
  DMXSerial.write(1, 80);
  DMXSerial.write(2, 0);
  DMXSerial.write(3, 0);
  
  // enable pwm outputs
  pinMode(OUT_R1,   OUTPUT);
  pinMode(OUT_G1, OUTPUT);
  pinMode(OUT_B1,  OUTPUT);
  pinMode(OUT_R2,   OUTPUT);
  pinMode(OUT_G2, OUTPUT);
  pinMode(OUT_B2,  OUTPUT);
  analogWrite(OUT_R1,   RedDefaultLevel1);
  analogWrite(OUT_G1, GreenDefaultLevel1);
  analogWrite(OUT_B1,  BlueDefaultLevel1);
  analogWrite(OUT_R2,   RedDefaultLevel2);
  analogWrite(OUT_G2, GreenDefaultLevel2);
  analogWrite(OUT_B2,  BlueDefaultLevel2);
  pinMode(13,  OUTPUT);
}

unsigned int r1, r2, g1, g2, b1, b2;
unsigned int hue1 = 150;
unsigned int hue2 = 160;
unsigned long l = 0;
void loop() {
  // Calculate how long no data backet was received
  unsigned long lastPacket = DMXSerial.noDataSince();
  
  if (lastPacket < 2000) {
    // read recent DMX values and set pwm levels 
    analogWrite(OUT_R1, DMXSerial.read(CHAN_R1));
    analogWrite(OUT_G1, DMXSerial.read(CHAN_G1));
    analogWrite(OUT_B1, DMXSerial.read(CHAN_B1));
    analogWrite(OUT_R2, DMXSerial.read(CHAN_R2));
    analogWrite(OUT_G2, DMXSerial.read(CHAN_G2));
    analogWrite(OUT_B2, DMXSerial.read(CHAN_B2));
    digitalWrite(13, LOW);  
  } else {
    if(l % 180 == 0) {
      HSBToRGB(hue1, 255, 255, &r1, &g1, &b1);
      HSBToRGB(hue2, 255, 255, &r2, &g2, &b2);
      b1 /= 2;
      b2 /= 2;
      hue1++;if(hue1 > 255) hue1 = 0;
      hue2++;if(hue2 > 255) hue2 = 0;
    }
    
    analogWrite(OUT_R1,   r1);
    analogWrite(OUT_G1, g2);
    analogWrite(OUT_B1,  b1);
    analogWrite(OUT_R2,   r2);
    analogWrite(OUT_G2, g2);
    analogWrite(OUT_B2,  b2);
    digitalWrite(13, HIGH);
    l++;
  } // if

}

void HSBToRGB(
    unsigned int inHue, unsigned int inSaturation, unsigned int inBrightness,
    unsigned int *oR, unsigned int *oG, unsigned int *oB )
{
    if (inSaturation == 0)
    {
        // achromatic (grey)
        *oR = *oG = *oB = inBrightness;
    }
    else
    {
        unsigned int scaledHue = (inHue * 6);
        unsigned int sector = scaledHue >> 8; // sector 0 to 5 around the color wheel
        unsigned int offsetInSector = scaledHue - (sector << 8);  // position within the sector         
        unsigned int p = (inBrightness * ( 255 - inSaturation )) >> 8;
        unsigned int q = (inBrightness * ( 255 - ((inSaturation * offsetInSector) >> 8) )) >> 8;
        unsigned int t = (inBrightness * ( 255 - ((inSaturation * ( 255 - offsetInSector )) >> 8) )) >> 8;

        switch( sector ) {
        case 0:
            *oR = inBrightness;
            *oG = t;
            *oB = p;
            break;
        case 1:
            *oR = q;
            *oG = inBrightness;
            *oB = p;
            break;
        case 2:
            *oR = p;
            *oG = inBrightness;
            *oB = t;
            break;
        case 3:
            *oR = p;
            *oG = q;
            *oB = inBrightness;
            break;
        case 4:
            *oR = t;
            *oG = p;
            *oB = inBrightness;
            break;
        default:    // case 5:
            *oR = inBrightness;
            *oG = p;
            *oB = q;
            break;
        }
    }
}
