import pysimpledmx, time
mydmx = pysimpledmx.DMXConnection('/dev/tty.usbmodem1411')

#mydmx.setChannel(10, 250)
#mydmx.render()

#chNum = int(10)
#i = int(2)
#while 1:
#  mydmx.setChannel(chNum, 255)
#  mydmx.render()
#  print("Setting channel {} to 255".format(chNum))
  
#  chNum += 1
#  if chNum > 511:
#    chNum = 1
    
#  time.sleep(0.5);


while 1:
  
  for i in range(0,255,40):
    mydmx.setChannel(2, 255-i)
    mydmx.setChannel(3, 255-i)
    mydmx.setChannel(4, 255-i)
    mydmx.setChannel(9, i)
    mydmx.setChannel(10, i)
    mydmx.setChannel(11, i)
    mydmx.render()
    time.sleep(0.01)
  
  for i in range(0, 255,10):
    mydmx.setChannel(11, 255-i)
    mydmx.render()
    time.sleep(0.005)

