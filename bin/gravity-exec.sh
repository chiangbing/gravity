#!/usr/bin/env bash

this="${BASH_SOURCE-$0}"
bin=$(cd -P -- "$(dirname -- "$this")" && pwd -P)
script="$(basename -- "$this")"
this="$bin/$script"

help() {
    echo <<EOF
gravity-exec.sh <sub command> [arguments ...]
EOF
}

# Read configurations
[ -f "$bin"/gravity-config.sh ] && . "$bin"/gravity-config.sh

GRAVITY_JAR=$(ls "$bin"/../gravity-*.jar)
if [ -z "$GRAVITY_JAR" ]; then
    echo "Error: gravity jar not found." >&2
    exit 1
fi

# add jars to classpath
GRAVITY_LIB="$bin"/../lib
GRAVITY_CP=$GRAVITY_JAR
for f in "$GRAVITY_LIB"/*.jar; do
    GRAVITY_CP="$GRAVITY_CP":$f
done

# add conf dir to classpath
GRAVITY_CP="$bin"/../conf:"$GRAVITY_CP"

# add webapps to classpath
if [ -d "$bin"/../webapps ]; then
    GRAVITY_CP="$bin"/..:"$GRAVITY_CP"
fi

if [[ "$1" == "-h" ]]; then
    help
    exit 0
elif [[ "$1" == "-classpath" ]]; then
    echo "$GRAVITY_CP"
    exit 0
fi

cmd=$1
shift
case $cmd in
    client)
        CLASSNAME="com.hanborq.gravity.GravityClient";
        ;;
    classpath)
        echo $GRAVITY_CP;
        exit 0;
        ;;
    room)
        CLASSNAME="com.hanborq.gravity.GravityRoom";
        ;;
    *)
        help; exit 0;;
esac

# run it!
if [ "$_GRAVITY_DAEMON_DETACHED" == "true" ]; then
    unset _GRAVITY_DAEMON_DETACHED
    touch $_GRAVITY_DAEMON_OUT
    nohup $JAVA -classpath "$GRAVITY_CP" $CLASSNAME $@ > "$_GRAVITY_DAEMON_OUT" 2>&1 </dev/null &
    echo $! > "$_GRAVITY_DAEMON_PIDFILE"
    sleep 1
    head "$_GRAVITY_DAEMON_OUT"
else
    $JAVA -classpath "$GRAVITY_CP" $CLASSNAME $@
fi

