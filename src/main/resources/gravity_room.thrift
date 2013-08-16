#!/usr/bin/thrift --java

/**
* This interface filename defines the controlling API for Runners.
*/

namespace java com.hanborq.gravity.thrift

typedef string Id

struct TWork {
  1: required Id id,
  2: required string clazz,
  3: required map<string, string> params,
  4: required string clientHost,
  5: required string metricsServerHost,
  6: required i32 metricsServerPort,
  7: required string statusServerHost,
  8: required i32 statusServerPort,
  11: optional string description,
}

service TGravityRoom {
  void startWork(1:TWork work),
  void stopWork(1:Id work_id),
  void pauseWork(1:Id work_id),
  void resumeWork(1:Id work_id),
}