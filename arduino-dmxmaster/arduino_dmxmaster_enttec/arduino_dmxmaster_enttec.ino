/*==============================================================================

Copyright (c) 2013 Soixante circuits

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
==============================================================================*/

#include <DmxSimple.h>

#define OUT_R 9
#define OUT_G 10
#define OUT_B 11

#define CHAN_R 1
#define CHAN_G 2
#define CHAN_B 3

#define DMX_PRO_HEADER_SIZE 4
#define DMX_PRO_START_MSG 0x7E
#define DMX_START_CODE 0
#define DMX_START_CODE_SIZE 1
#define DMX_PRO_SEND_PACKET 6 // "periodically send a DMX packet" mode
#define DMX_PRO_END_SIZE 1
#define DMX_PRO_END_MSG 0xE7
#define DMX_PRO_SEND_SIZE_LSB 10
#define DMX_PRO_SEND_SIZE_MSB 11
unsigned char state;
unsigned int dataSize;
unsigned int channel;

void setup() {
  Serial.begin(57600);
  //Serial.begin(9600);
  //Serial.begin(250000);
  // change the TX pin according to the DMX shield you're using
  DmxSimple.usePin(2);
  state = DMX_PRO_END_MSG;
  
  pinMode(OUT_R, OUTPUT);
  pinMode(OUT_G, OUTPUT);
  pinMode(OUT_B, OUTPUT);
  analogWrite(OUT_R, 127);
  analogWrite(OUT_G, 127);
  analogWrite(OUT_B,127);
}

void loop() {

  unsigned char c;

  while(true) {
  while(!Serial.available());
  c = Serial.read();
  if (c == DMX_PRO_START_MSG && state == DMX_PRO_END_MSG){
    state = c;
  }
  else if (c == DMX_PRO_SEND_PACKET && state == DMX_PRO_START_MSG){
    state = c;
  }
  else if (state == DMX_PRO_SEND_PACKET ){
    dataSize = c & 0xff;
    state = DMX_PRO_SEND_SIZE_LSB;
  }
  else if (state == DMX_PRO_SEND_SIZE_LSB){
    dataSize += (c << 8) & 0xff00;
    state = DMX_PRO_SEND_SIZE_MSB;
  }
  else if ( c == DMX_START_CODE && state == DMX_PRO_SEND_SIZE_MSB){
    state = c;
    channel=1;
  }
  else if ( state == DMX_START_CODE && channel < dataSize){
    DmxSimple.write(channel, c);
    if(channel == CHAN_R) analogWrite(OUT_R, 255-c);
    if(channel == CHAN_G) analogWrite(OUT_G, 255-c);
    if(channel == CHAN_B) analogWrite(OUT_B, 255-c);
    channel++;
  }
  else if ( state == DMX_START_CODE && channel == dataSize && c == DMX_PRO_END_MSG){
    state = c;
  }
  }
}





