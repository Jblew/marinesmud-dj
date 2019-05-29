--init.lua
deviceId = "MMDJ_ES2"
deviceGroup = "235.0.2.3" --235.0.2.1 is iam send service
iamGroup = '235.0.2.1'
wifiSSID = "TeofileNovum"
wifiPasswd = "fovea@costalis"

print("---- CONFIG ----")
print("   DeviceId: "..deviceId)
print("   DeviceGroup: "..deviceGroup)
print("   IamGroup: "..iamGroup)
print("   WIFI_SSID: "..wifiSSID)
print("----------------")
print("Setting up WIFI...")
wifi.setmode(wifi.STATION)
--modify according your wireless router settings
wifi.sta.config(wifiSSID ,wifiPasswd)
wifi.sta.connect()

sendsock = 0
respsock = 0
recvsock = 0
mysock = 0


pwm.setup(1, 100, 63)
pwm.setup(2, 100, 512)
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

tmr.alarm(1, 2000, tmr.ALARM_AUTO, 
function() 
    if wifi.sta.getip()== nil then 
        print("IP unavaiable, Waiting...") 
    else 
        tmr.stop(1)
        ip = wifi.sta.getip()
        print("Config done, IP is "..ip)
        onGotIp(ip)
    end 
end
)


function onGotIp(ip)
    net.multicastJoin(ip, deviceGroup)
    --sendUDPsock = net.createUDPSocket()
    sendsock = net.createUDPSocket()
    recvsock = net.createUDPSocket()
    respsock = net.createUDPSocket()
    --mysock = net.createConnection(net.UDP, 0)
    print("Conf onreceive")
    recvsock:on("receive", 
    function(sck, payload)
        local response = processPayload(payload)
        respsock:send(7134, deviceGroup, response)
    end
    )
    recvsock:listen(7134, deviceGroup)
    
    local multicastSendTimer = tmr.create()
    multicastSendTimer:register(3000, tmr.ALARM_SEMI,
    function()
        sendMyIp(multicastSendTimer)
        wifi.sta.getip()
    end
    )
    multicastSendTimer:start()
 
end

function sendMyIp(multicastSendTimer) 
    ip = wifi.sta.getip()
    sendsock:send(7134, iamGroup, "iam "..ip.."@"..deviceId.."@"..deviceGroup,
    function()
        print("iam "..ip.."@"..deviceId.."@"..deviceGroup..", time: "..tmr.time().."s")
        multicastSendTimer:start()
    end)
end


srv = net.createServer(net.TCP, 1)

srv:listen(90,function(conn)
    conn:on("receive",function(conn,payload)
        local response = processPayload(payload)
        --conn:send(response)
    end)
end)


function processPayload(payload)
    if string.len(payload) > 6 then
            local v1 = tonumber(string.sub(payload, 1, 2), 32)
            local v2 = tonumber(string.sub(payload, 3, 4), 32)
            local v3 = tonumber(string.sub(payload, 5, 6), 32)
            --local v4 = tonumber(string.sub(payload, 7, 8), 32)
            --local v5 = tonumber(string.sub(payload, 9, 10), 32)
            --local v6 = tonumber(string.sub(payload, 11, 12), 32)
            --local packetNum = tonumber(string.sub(payload, 13, 14), 32)

            print(payload)
            if v1 ~= nil and v2 ~= nil and v3 ~= nil then
                pwm.setduty(1, math.max(0, math.min(v1, 1023)))
                pwm.setduty(2, math.max(0, math.min(v2, 1023)))
                pwm.setduty(3, math.max(0, math.min(v3, 1023)))
                --pwm.setduty(5, math.max(0, math.min(v4, 1023)))
                --pwm.setduty(6, math.max(0, math.min(v5, 1023)))
                --pwm.setduty(7, math.max(0, math.min(v6, 1023)))

                --print(v1..", "..v2..", "..v3)   
                print()             
                return "200 OK \r\n"
            else
                return "401 Not a number\r\n"
            end
        else
            return "402 Too short\r\n"
        end
    return "300 Unknown response\r\n"
end