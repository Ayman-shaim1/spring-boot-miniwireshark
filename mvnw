#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper - allows building without Maven installed
# ----------------------------------------------------------------------------

# Resolve project base directory (containing .mvn)
resolve_basedir() {
    _basedir="$1"
    while [ "$_basedir" != '/' ]; do
        if [ -d "$_basedir/.mvn" ]; then
            printf '%s' "$(cd "$_basedir" && pwd)"
            return 0
        fi
        _basedir="$(dirname "$_basedir")"
    done
    return 1
}

# Set JAVA_HOME on macOS if not set
if [ -z "$JAVA_HOME" ] && [ -x "/usr/libexec/java_home" ]; then
    export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null)"
fi

JAVACMD="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if ! command -v "$JAVACMD" >/dev/null 2>&1; then
    JAVACMD="$(command -v java 2>/dev/null)"
fi

if [ -z "$JAVACMD" ] || [ ! -x "$JAVACMD" ]; then
    echo "Error: Java not found. Install JDK or set JAVA_HOME." >&2
    exit 1
fi

BASE_DIR="$(resolve_basedir "$(cd "$(dirname "$0")" && pwd)")"
if [ -z "$BASE_DIR" ]; then
    echo "Error: Cannot find project base directory (.mvn)" >&2
    exit 1
fi

WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -r "$WRAPPER_JAR" ]; then
    echo "Error: maven-wrapper.jar not found at $WRAPPER_JAR" >&2
    exit 1
fi

exec "$JAVACMD" \
    ${MAVEN_OPTS:+-J$MAVEN_OPTS} \
    -classpath "$WRAPPER_JAR" \
    "-Dmaven.multiModuleProjectDirectory=$BASE_DIR" \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
