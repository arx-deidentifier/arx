#!/bin/bash

echo "Installing Maven dependencies..."

mvn install:install-file \
  -Dfile=lib/ant/poi/poi-3.10-FINAL-20140208.jar \
  -DgroupId=com.apache.poi \
  -DartifactId=poi \
  -Dversion=3.10-FINAL \
  -Dpackaging=jar

mvn install:install-file \
  -Dfile=lib/ant/eclipse/org.eclipse.draw2d_3.10.100.201606061308.jar \
  -DgroupId=org.eclipse \
  -DartifactId=draw2d \
  -Dversion=3.10.100 \
  -Dpackaging=jar \
  -DgeneratePom=true

mvn install:install-file \
  -Dfile=lib/ant/smile/smile-1.3.1-java7.jar \
  -DgroupId=com.github.haifengl \
  -DartifactId=smile \
  -Dversion=1.3.1 \
  -Dpackaging=jar

OS=$(uname -s)
ARCH=$(uname -m)

case "$ARCH" in
  x86_64|amd64)
    MAVEN_ARCH="x86_64"
    ;;
  arm64|aarch64)
    MAVEN_ARCH="aarch64"
    ;;
  *)
    echo "Unsupported architecture: $ARCH"
    exit 1
    ;;
esac

case "$OS" in
  Linux*)
    SWT_FILE="lib/ant/eclipse/org.eclipse.swt.gtk.linux.x86_64_3.114.100.v20200604-0951.jar"
    SWT_ARTIFACTID="org.eclipse.swt.gtk.linux.x86_64"
    PROFILE="gtk-64"
    PLATFORM="Linux"
    ;;
  Darwin*)
    SWT_FILE="lib/ant/eclipse/org.eclipse.swt.cocoa.macosx.x86_64_3.114.100.v20200604-0951.jar"
    SWT_ARTIFACTID="org.eclipse.swt.cocoa.macosx.x86_64"
    PROFILE="osx-64"
    PLATFORM="macOS"
    ;;
  CYGWIN*|MINGW*|MSYS*|Windows*)
    SWT_FILE="lib/ant/eclipse/org.eclipse.swt.win32.win32.x86_64_3.114.100.v20200604-0951.jar"
    SWT_ARTIFACTID="org.eclipse.swt.win32.win32.x86_64"
    PROFILE="win-64"
    PLATFORM="Windows"
    ;;
  *)
    echo "Unsupported operating system: $OS"
    exit 1
    ;;
esac

echo "Detected platform: $PLATFORM ($ARCH)"
echo "Installing SWT library: $SWT_ARTIFACTID"

if [ ! -f "$SWT_FILE" ]; then
    echo "Error: SWT library file not found: $SWT_FILE"
    echo "Please ensure the correct SWT library for your platform is in the lib/ant/eclipse/ directory."
    exit 1
fi

mvn install:install-file \
  -Dfile="$SWT_FILE" \
  -DgroupId=org.eclipse.platform \
  -DartifactId="$SWT_ARTIFACTID" \
  -Dversion=3.114.100 \
  -Dpackaging=jar

echo "Dependencies installed successfully!"

echo "Building with profile: $PROFILE"
mvn clean package compile -P "$PROFILE" -Dcore=true -DskipTests

echo "Build completed successfully!"