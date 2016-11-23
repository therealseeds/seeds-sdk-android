#!/bin/bash

rm -rf aar-folder-temp
rm -rf aar-classes-temp

unzip seeds-old.aar -d aar-folder-temp
unzip aar-folder-temp/classes.jar -d aar-classes-temp
rm -rf aar-classes-temp/com/android

jar cvf aar-folder-temp/classes.jar -C aar-classes-temp/ .
jar cvf Seeds.aar -C aar-folder-temp/ .
