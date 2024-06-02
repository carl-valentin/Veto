rem copy O/package in new dir
rem copy this file in new dir
rem go to new dir and run this file
jpackage --verbose --name Veto --main-jar Veto.jar --type "msi" --icon veto.ico --win-dir-chooser --win-menu --win-menu-group "Veto" --win-shortcut --app-version 1.1 --vendor "Carl Valentin GmbH" --java-options '-Djava.library.path=$APPDIR' --input . --temp ../temp --resource-dir ../Veto/wixres
