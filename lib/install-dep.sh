#!/bin/bash

mvn install:install-file -Dfile=TarsosDSP-2.4-bin.jar -DgroupId=jorensix \
    -DartifactId=TarsosDSP -Dversion=2.4 -Dpackaging=jar

sudo cp rxtx-2.1-7-bins-r2/RXTXcomm.jar /Library/Java/Extensions
sudo cp rxtx-2.1-7-bins-r2/Mac_OS_X/librxtxSerial.jnilib /Library/Java/Extensions

