pwm.setup(1, 100, 127)
pwm.setup(2, 100, 127)
pwm.setup(3, 100, 512)
pwm.setup(5, 100, 512)
pwm.setup(6, 100, 512)
pwm.setup(7, 100, 512)
pwm.start(1)
pwm.start(2)
pwm.start(3)
pwm.start(5)
pwm.start(6)
pwm.start(7)

srv = net.createServer(net.TCP)

srv:listen(80,function(conn)
 --co zrobic na polaczenie przychodzace
    conn:on("receive",function(conn,payload)
    if string.len(payload) > 24 then
        local v1 = tonumber(string.sub(payload, 1, 4))
        local v2 = tonumber(string.sub(payload, 5, 8))
        local v3 = tonumber(string.sub(payload, 9, 12))
        local v4 = tonumber(string.sub(payload, 13, 16))
        local v5 = tonumber(string.sub(payload, 17, 20))
        local v6 = tonumber(string.sub(payload, 21, 24))
        
        --print(payload)
        if v1 ~= nil and v2 ~= nil and v3 ~= nil and v4 ~= nil and v5 ~= nil and v6 ~= nil then
            --print(v1..", "..v2..", "..v3..", "..v4..", "..v5..", "..v6)
            pwm.setduty(1, math.max(0, math.min(v1, 1023)))
            pwm.setduty(2, math.max(0, math.min(v2, 1023)))
            pwm.setduty(3, math.max(0, math.min(v3, 1023)))
            pwm.setduty(5, math.max(0, math.min(v4, 1023)))
            pwm.setduty(6, math.max(0, math.min(v5, 1023)))
            pwm.setduty(7, math.max(0, math.min(v6, 1023)))
            
            conn:send("200 OK\r\n")
        else
            conn:send("400 Bad request'r'n")
        end
   else
        conn:send("400 Bad request\r\n")
   end
 end)
 --co po wyslaniu danych
 conn:on("sent",function(conn)
   --print("Sent, closing")
   conn:close()
 end)
end)

