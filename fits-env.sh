#!/bin/bash
#
# Sets up the environment for launching a FITS instance via
# either the fits.sh launcher, or the fits-ngserver.sh Nailgun server

#FITS_HOME=`dirname "$0"`
FITS_HOME=`echo "$0" | sed 's,/[^/]*$,,'`
export FITS_HOME

# Uncomment the following line if you want "file utility" to dereference and follow symlinks.
# export POSIXLY_CORRECT=1

# concatenate args and use eval/exec to preserve spaces in paths, options and args
args=""
for arg in "$@" ; do
    args="$args \"$arg\""
done

# Application classpath
APPCLASSPATH=""
JCPATH=${FITS_HOME}/lib
# Add on extra jar files to APPCLASSPATH
for i in "$JCPATH"/*.jar; do
    APPCLASSPATH="$APPCLASSPATH":"$i"
done

# all subdirectories of ${FITS_HOME}/lib/ get loaded dynamically at runtime. DO NOT add here!

# PLM - this doesn't seem to be the case 7/15/16
for lib in adltool audioinfo droid exiftool ffident fileinfo fileutility jhove mediainfo nzmetool tika
do
	JCPATH=${FITS_HOME}/lib/${lib}
    # Add on extra jar files to APPCLASSPATH
	for i in "$JCPATH"/*.jar; do
		APPCLASSPATH="$APPCLASSPATH":"$i"
	done
done
