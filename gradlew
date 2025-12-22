#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to locate java
if [ -z "$JAVA_HOME" ]; then
  JAVA_CMD=java
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

# Determine the location of the script being executed.
# This is required to locate the lib directory relative to this script.
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

DEFAULT_JVM_OPTS=""

if [ -n "$JAVA_OPTS" ]; then
    JVM_OPTS="$JAVA_OPTS $DEFAULT_JVM_OPTS"
else
    JVM_OPTS="$DEFAULT_JVM_OPTS"
fi

exec "$JAVA_CMD" $JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
