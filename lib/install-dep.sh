#!/bin/bash

mvn install:install-file -Dfile=JavaFTD2XX-0.2.6.jar -DgroupId=com.ftdi -DartifactId=JavaFTD2XX -Dversion=0.2.6 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=org-openide-util.jar -DgroupId=org.netbeans.platform -DartifactId=org-openide-util -Dversion=8.2 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=org-openide-util-lookup.jar -DgroupId=org.netbeans.platform -DartifactId=org-openide-util-lookup -Dversion=8.2 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=org-netbeans-api-visual.jar -DgroupId=org.netbeans.platform -DartifactId=org-netbeans-api-visual -Dversion=8.2 -Dpackaging=jar -DgeneratePom=true

