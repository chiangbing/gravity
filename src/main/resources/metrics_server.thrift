#!/usr/bin/thrift --java

/**
* This interface file defines MetricsServer.
*/

namespace java com.hanborq.gravity.metrics.thrift


struct TMetricsRecord {
    1: required i64 timestamp,
    11: optional map<string, i64> longMetrics,
    12: optional map<string, double> doubleMetrics,
    13: optional map<string, binary> binaryMetrics,
}

service TMetricsServer {
    oneway void pushMetrics(1:list<TMetricsRecord> records),
}