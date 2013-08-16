#!/usr/bin/env bash

rm -rf gen-java 2>/dev/null
thrift --gen java metrics_server.thrift
thrift --gen java status_server.thrift
thrift --gen java gravity_room.thrift
thrift --gen java gravity_client.thrift
rm -rf ../java/com/hanborq/gravity/metrics/thrift 2>/dev/null
rm -rf ../java/com/hanborq/gravity/thrift 2>/dev/null
cp -r gen-java/com/hanborq/gravity/metrics/thrift ../java/com/hanborq/gravity/metrics/
cp -r gen-java/com/hanborq/gravity/thrift ../java/com/hanborq/gravity
rm -rf gen-java
