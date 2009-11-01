#!/bin/bash
CP=`ls lib/*|tr '\n' ':'`
JAVA_OPTS=-Xmx512m
java $JAVA_OPTS -cp $CP sk.baka.aedict.indexer.Main "$@"

