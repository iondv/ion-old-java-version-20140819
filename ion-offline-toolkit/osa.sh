#! /bin/sh

# The path to the folder containing the java runtime
JAVA_HOME="/usr/lib/jvm/default-java"

CLASS_PATH=""

for jar in lib*
do
if [ -f $jar ]
then
CLASS_PATH="$CLASS_PATH:lib/$jar"
fi

USER="root"

java -jar offline-toolkit.jar -home $JAVA_HOME -cp $CLASS_PATH "$@"