#!/bin/sh -e

echo "Downloading and updating compiler..."
if git clone https://github.com/vipaoL/j2me_compiler.git 2>/dev/null ; then
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




######## CONFIG ########
########
YOUR_LIBS=../lib       # YOUR LIBRARIES
RES=../rsc
APP=MobileApplication3   # Output jar name
MANIFEST=../manifest.mf
########
########


if [ ! -e ${MANIFEST} ] ; then
	MANIFEST=../MANIFEST.MF
	if [ ! -e ${MANIFEST} ] ; then
		MANIFEST=./manifest.mf
		if [ ! -e ${MANIFEST} ] ; then
			MANIFEST=./MANIFEST.MF
			if [ ! -e ${MANIFEST} ] ; then
				echo "No MANIFEST.MF or manifest.mf found in ./ or ../"
				exit 2
			fi
		fi
	fi
fi

LIB_DIR=${WTK_HOME}/lib
CLASSPATH=${LIB_DIR}/*
CLDCAPI=${LIB_DIR}/cldcapi11.jar
MIDPAPI=${LIB_DIR}/midpapi20.jar
PREVERIFY=${WTK_HOME}/bin/preverify
JAVAC=javac
JAR=jar

#ls ${JAVA_HOME}
#file ${JAVA_HOME}/bin/javac
#file ${JAVA_HOME}/bin/jar
#ldd ${JAVA_HOME}/bin/javac
#ls -la ${JAVA_HOME}/bin/javac

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
rm -rf ../tmpclasses/*
rm -rf ../classes/*

echo "Unpacking your libraries: " ${YOUR_LIBS}/*.jar
cd ../tmpclasses
../bin/${JAR} xf ${YOUR_LIBS}/*.jar
rm -rf META-INF


cd ${WORK_DIR}
echo "Compiling source files..."
${JAVAC} \
    -bootclasspath ${CLDCAPI}${PATHSEP}${MIDPAPI} \
    -source 1.3 \
    -target 1.3 \
    -d ../tmpclasses \
    -classpath ../tmpclasses${PATHSEP}${CLASSPATH} \
	-extdirs ../lib \
    `find ../src -name '*'.java`

echo "Preverifying class files..."

${PREVERIFY} \
    -classpath ${CLDCAPI}${PATHSEP}${MIDPAPI}${PATHSEP}${CLASSPATH}${PATHSEP}../tmpclasses \
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
