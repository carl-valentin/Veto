rem copy O/package in new dir
rem copy this file in new dir
rem go to new dir and run this file
rmdir /S /Q ..\VetoTemp
del Veto-1.1.msi
jpackage --verbose --name Veto --main-jar Veto.jar --type "msi" --icon veto.ico --win-dir-chooser --app-version 1.1 --vendor "Carl Valentin GmbH" --java-options '-Djava.library.path=$APPDIR' --input . --temp ../VetoTemp --resource-dir ../Veto/wixres
