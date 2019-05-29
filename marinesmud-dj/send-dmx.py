import pysimpledmx, time, base64, json, sys

if len(sys.argv) > 1:
#  print base64.urlsafe_b64decode(sys.argv[1])
  print json.loads(base64.urlsafe_b64decode(sys.argv[1]))
  
else:
  print 'Specify json arguments'

#mydmx = pysimpledmx.DMXConnection('/dev/tty.usbmodem1411')

#mydmx.setChannel(10, 250)
#mydmx.render()

