dmx_frame = [0] * 512
dmx_frame[511-1] = 43

bb1 = len(dmx_frame) & 0xFF
bb2 = (len(dmx_frame) >> 8) & 0xFF
print bb1
print bb2
print ''
