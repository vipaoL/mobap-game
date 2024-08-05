#!/bin/sh -e

PROJ_HOME="$(pwd)"
echo "Downloading and updating compiler..."
if git clone https://github.com/vipaoL/j2me_compiler.git bin/j2me_compiler 2>/dev/null ; then
	echo "Done."
else
	echo "Already downloaded."
fi
cd bin/j2me_compiler
git pull
cd "${PROJ_HOME}"
PATHSEP=":"
JAVA_HOME="${PROJ_HOME}"/bin/j2me_compiler/jdk1.6.0_45
WTK_HOME="${PROJ_HOME}"/bin/j2me_compiler/WTK2.5.2




######## CONFIG ########
########
YOUR_LIBS="${PROJ_HOME}"/lib       # YOUR LIBRARIES
RES="${PROJ_HOME}"/rsc
APP="${PROJ_HOME}"/bin/MobileApplication3.jar   # Output jar name
MANIFEST="${PROJ_HOME}"'/Application Descriptor'
########
########


if [ ! -e "${MANIFEST}" ] ; then
	MANIFEST=./MANIFEST.MF
	if [ ! -e "${MANIFEST}" ] ; then
		MANIFEST=./manifest.mf
		if [ ! -e "${MANIFEST}" ] ; then
			echo "No MANIFEST.MF or manifest.mf or Application Descriptor found in ./"
			exit 2
		fi
	fi
fi

MANIFEST_TMP="${PROJ_HOME}"/bin/manifest-tmp.mf
cp "${MANIFEST}" $MANIFEST_TMP
MANIFEST=$MANIFEST_TMP

# add commit number to manifest
COMMIT=$(git rev-parse --short HEAD)
echo Adding commit hash $COMMIT to $MANIFEST
echo Commit: $COMMIT >> "${MANIFEST}"

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
WORK_DIR=`readlink -f $(dirname $0)`
cd ${WORK_DIR}

echo "Creating or cleaning directories..."
mkdir -p ../tmpclasses
mkdir -p ../classes
rm -rf ../tmpclasses/*
rm -rf ../classes/*

echo "Unpacking your libraries: " ${YOUR_LIBS}/*.jar
cd ../tmpclasses
${JAR} xf ${YOUR_LIBS}/*.jar
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
    `find "${PROJ_HOME}"/src -name '*'.java`

echo "Preverifying class files..."

${PREVERIFY} \
    -classpath ${CLDCAPI}${PATHSEP}${MIDPAPI}${PATHSEP}${CLASSPATH}${PATHSEP}../tmpclasses \
    -d ../classes \
    ../tmpclasses

echo "Jaring preverified class files..."
${JAR} cmf "${MANIFEST}" "${APP}" -C ../classes .

if [ -d ${RES} ] ; then
  ${JAR} uf "${APP}" -C ${RES} .
fi

echo "Done!" "${APP}"
#echo "Don't forget to update the JAR file size in the JAD file!!!"
#echo
