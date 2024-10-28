#!/bin/sh

PROJ_HOME=`readlink -f $(dirname $0)`
YOUR_LIBS="${PROJ_HOME}"/lib			# app libraries (jars)
RES="${PROJ_HOME}"/res				# resourses
APP_NAME="MobileApplication3"			# Output jar name
MANIFEST="${PROJ_HOME}"/'Application Descriptor'
#JAVA_HOME=

