#!/bin/sh
# copy O/package in new dir
# copy this file in new dir
# go to new dir and run this file
rm -rf ../VetoTemp
rm veto_1.1-1_amd64.deb
jpackage --verbose --name Veto --main-jar Veto.jar --type "deb" --icon icon.png --app-version 1.1 --vendor "Carl Valentin GmbH" --java-options '-Djava.library.path=$APPDIR' --input . --temp ../VetoTemp --resource-dir ../Veto/jpack-res-lx
