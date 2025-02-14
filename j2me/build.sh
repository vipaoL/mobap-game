#!/bin/sh -e

# Make it possible to run this script from any directory'`
WORK_DIR=`readlink -f $(dirname $0)`
cd ${WORK_DIR}

. ./build-config.sh

echo "Downloading and updating build tools..."
J2ME_BUILD_TOOLS="$(pwd)"/bin/j2me-build-tools
if [ ! -e "${J2ME_BUILD_TOOLS}" ] ; then
  git clone https://github.com/vipaoL/j2me-build-tools.git "${J2ME_BUILD_TOOLS}"
  echo "Done."
else
  echo "Already downloaded."
fi

set +e
cd bin/j2me-build-tools && git pull
set -e

cd "${WORK_DIR}"

if [ ! -e "${MANIFEST}" ] ; then
  echo
  echo "${MANIFEST} is not found in $(pwd)"
  exit 2
fi

MANIFEST_TMP="${WORK_DIR}"/bin/manifest-tmp.mf
cat "${MANIFEST}" > $MANIFEST_TMP
MANIFEST=$MANIFEST_TMP

# add commit hash to the manifest
COMMIT=$(git rev-parse --short HEAD)
echo
echo Adding commit hash $COMMIT to $MANIFEST
echo "Commit: ${COMMIT}" >> "${MANIFEST}"

J2ME_CLASSPATH_DIR=${J2ME_BUILD_TOOLS}/lib
CLASSPATH=${J2ME_CLASSPATH_DIR}/*
CLDCAPI=${J2ME_CLASSPATH_DIR}/cldc11.jar
MIDPAPI=${J2ME_CLASSPATH_DIR}/midp21.jar
PREVERIFY=${J2ME_BUILD_TOOLS}/bin/preverify
JAVAC=javac
JAR=jar

echo
if [ ! -n "${JAVA_HOME}" ] ; then
  # let's assume you have openjdk-8 installed
  JAVA_HOME="$(dirname $(dirname $(readlink -f $(which javac))))"
fi

if [ -d "${JAVA_HOME}" ] ; then
  JAVAC=${JAVA_HOME}/bin/javac
  JAR=${JAVA_HOME}/bin/jar
else
  echo "Error: java is not found:"
  file "${JAVA_HOME}"
fi

echo "Java: ${JAVA_HOME}"
"${JAVA_HOME}"/bin/java -version

echo
echo "Cleaning tmp directories..."
mkdir -p bin/tmpclasses
mkdir -p bin/classes
rm -rf bin/tmpclasses/*
rm -rf bin/classes/*

cd bin/tmpclasses
LIB_JARS="${YOUR_LIBS}/*.jar"
echo $LIB_JARS
echo "Unpacking your libraries: ${LIB_JARS}"
${JAR} xf ${LIB_JARS}
rm -rf META-INF

cd ${WORK_DIR}
echo
echo "Compiling source files..."
PATHSEP=":"
${JAVAC} \
    -Xlint:-options \
    -bootclasspath ${CLDCAPI}${PATHSEP}${MIDPAPI} \
    -source 1.3 \
    -target 1.3 \
    -d bin/tmpclasses \
    -classpath bin/tmpclasses${PATHSEP}${CLASSPATH} \
	-extdirs ../lib \
    `find ${SOURCES_PATHS} -name '*'.java`

echo
echo "Preverifying class files..."
PREVERIFY_CLASSPATH="${CLDCAPI}${PATHSEP}${MIDPAPI}${PATHSEP}${CLASSPATH}${PATHSEP}bin/tmpclasses"
${PREVERIFY} \
    -classpath "${PREVERIFY_CLASSPATH}" \
    -d bin/classes \
    bin/tmpclasses

echo
echo "Jaring preverified class files..."
APP="${WORK_DIR}"/bin/"${APP_NAME}".jar
${JAR} cmf "${MANIFEST}" "${APP}" -C bin/classes .

echo
if [ -d "${RES}" ] ; then
  echo "Adding resources: ${RES}"
  ${JAR} uf "${APP}" -C "${RES}" .
else
  echo "Resource folder "${RES}" not found, skipping..."
fi

echo
echo "Done!" "${APP}"

