#!/bin/sh -e

echo "Downloading and updating compiler..."
if git clone https://github.com/vipaoL/j2me_compiler.git; then
echo "Done."
else
echo "Already downloaded."
fi
cd j2me_compiler
git pull
cd ..
PATHSEP=":"
JAVA_HOME=./j2me_compiler/jdk1.6.0_45
WTK_HOME=./j2me_compiler/WTK2.5.2



LIBRARY=../lib/PhysicsEngine_v135a.jar
CLASSPATH=../lib/PhysicsEngine_v135a.jar${PATHSEP}${WTK_HOME}/lib/jsr75.jar
RES=../rsc
APP=MobileApplication3
MANIFEST=../manifest.mf

LIB_DIR=${WTK_HOME}/lib
CLDCAPI=${LIB_DIR}/cldcapi11.jar
MIDPAPI=${LIB_DIR}/midpapi20.jar
PREVERIFY=${WTK_HOME}/bin/preverify
JAVAC=javac
JAR=jar

ls ${JAVA_HOME}
ls ${JAVA_HOME}/bin/javac
ls ${JAVA_HOME}/bin/jar

if [ -n "${JAVA_HOME}" ] ; then
  JAVAC=${JAVA_HOME}/bin/javac
  JAR=${JAVA_HOME}/bin/jar
fi

#
# Make possible to run this script from any directory'`
#
WORK_DIR=`dirname $0`
cd ${WORK_DIR}

echo "Creating or cleaning directories..."
mkdir -p ../tmpclasses
mkdir -p ../classes
rm -rfv ../tmpclasses/*
rm -rfv ../classes/*

echo "Compiling source files..."

${JAVAC} \
    -bootclasspath ${CLDCAPI}${PATHSEP}${MIDPAPI} \
    -source 1.3 \
    -target 1.3 \
    -d ../tmpclasses \
    -classpath ../tmpclasses${PATHSEP}${CLASSPATH} \
	-extdirs ../lib \
    `find ../src -name '*'.java`

cd ../tmpclasses

jar xf ${LIBRARY}
rm -rf META-INF

#cd ${WORK_DIR}
cd ../bin

echo "Preverifying class files..."

${PREVERIFY} \
    -classpath ${CLDCAPI}${PATHSEP}${MIDPAPI}${PATHSEP}../tmpclasses \
    -d ../classes \
    ../tmpclasses

echo "Jaring preverified class files..."
${JAR} cmf ${MANIFEST} ${APP}.jar -C ../classes .

if [ -d ${RES} ] ; then
  ${JAR} uf ${APP}.jar -C ${RES} .
fi

echo "Done!" ./${APP}.jar
#echo "Don't forget to update the JAR file size in the JAD file!!!"
#echo
