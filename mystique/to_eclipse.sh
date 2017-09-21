#!/bin/bash

BASEDIR=$(dirname $0)
cd $BASEDIR
echo $1
echo $2
if [ -d "build/outputs/aar/$1" ]; then
    echo "Cleaning existing directory $1"
	rm -rf build/outputs/aar/$1
fi

mkdir build/outputs/aar/$1
cd build/outputs/aar/
if [ -f "$1.aar" ]; then
    cp $1.aar $1
    cd $1
    unzip -q $1.aar
    rm $1.aar

    mkdir src libs
    mv classes.jar libs/$1.jar
    rm -f R.txt
    rm -rf jni
    cd ..

    tar -zcf $1-$2.tar.gz $1/
    rm -rf $1/
    echo "Done"
    cd $BASEDIR
else
    echo "Build not found"
    exit -1
fi


