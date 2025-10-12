#!/usr/bin/env bash

# Create 'bin/' if not already
if [ ! -d "bin/" ]; then
  mkdir bin/
fi

javac -d bin/class/ src/main/java/com/jhontabio/minecraftreader/*.java
#java -cp bin/ MCR
jar cfm bin/jar/mcr.jar MANIFEST.MF -C bin/class/ .
java -jar bin/jar/mcr.jar
