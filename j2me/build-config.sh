#!/bin/sh

SOURCES_PATHS=$(echo "${WORK_DIR}"/src "${WORK_DIR}"/lib/*/src "${WORK_DIR}"/../src)
YOUR_LIBS="${WORK_DIR}"/lib			# app libraries (jars)
RES="${WORK_DIR}"/res				# resourses
APP_NAME="MobileApplication3"			# Output jar name
MANIFEST="${WORK_DIR}"/'Application Descriptor'
#JAVA_HOME=

