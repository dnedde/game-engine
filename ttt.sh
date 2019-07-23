#!/bin/bash

JAVA_HOME=$(/usr/libexec/java_home)
$JAVA_HOME/bin/javac *.java && $JAVA_HOME/bin/java com.github.davenedde.gameengine.TicTacToe
