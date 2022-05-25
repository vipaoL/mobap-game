#!/bin/sh -e
#
#####################################
#
#
# IT DIDN'T WORK! USE IDE INSTEAD
#
#
#####################################
#
# This batch file builds and preverifies the code for the demos.
# it then packages them in a JAR file appropriately.
#

LIBS=../lib/PhysicsEngine_v135a.jar${PATHSEP}../../WTK2.5.2/lib/jsr75.jar
APP=MobileApplication3
MANIFEST=../build/manifest.mf

LIB_DIR=../../../lib
CLDCAPI=${LIB_DIR}/cldcapi11.jar
MIDPAPI=${LIB_DIR}/midpapi20.jar
PREVERIFY=../../WTK2.5.2/bin/preverify
JAVA_HOME=../../../../jdk1.6.0_45

PATHSEP=":"

JAVAC=javac
JAR=jar

if [ -n "${JAVA_HOME}" ] ; then
  JAVAC=${JAVA_HOME}/bin/javac
  JAR=${JAVA_HOME}/bin/jar
fi

#
# Make possible to run this script from any directory'`
#
cd `dirname $0`

echo "Creating directories..."
mkdir -p ../tmpclasses
mkdir -p ../classes

echo "Compiling source files..."

${JAVAC} \
    -bootclasspath ${CLDCAPI}${PATHSEP}${MIDPAPI} \
    -source 1.3 \
    -target 1.3 \
    -d ../lib \
    -classpath ${LIBS}\
    `find ../src -name '*'.java`

echo "Preverifying class files..."

${PREVERIFY} \
    -classpath ${CLDCAPI}${PATHSEP}${MIDPAPI}${PATHSEP}../tmpclasses \
    -d ../classes \
    ../tmpclasses

echo "Jaring preverified class files..."
${JAR} cmf ${MANIFEST} ${APP}.jar -C ../classes .

if [ -d ../res ] ; then
  ${JAR} uf ${APP}.jar -C ../res .
fi

echo
echo "Don't forget to update the JAR file size in the JAD file!!!"
echo
