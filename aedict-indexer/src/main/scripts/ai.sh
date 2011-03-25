#!/bin/bash
CP=`ls lib/*|tr '\n' ':'`
# Tatoeba indexing uses quite a lot of memory on Linux 64bit
JAVA_OPTS=-Xmx1024m
java $JAVA_OPTS -cp $CP sk.baka.aedict.indexer.Main "$@"

