#!/usr/bin/thrift --java

/**
 * This interface file define StatusServer.
 */

namespace java com.hanborq.gravity.thrift

enum TState {
  PENDING = 1,
  RUNNING = 2,
  PAUSED = 3,
  ABORTED = 4,
  COMPLETED = 5,
}

struct TWorkStatus {
  1: required string workId,
  2: required string room,   // from which gravity room
  3: required TState state,
  4: required double progress,
  5: required i64 errors,
}

service TStatusServer {
  oneway void reportWorkStatus(1:TWorkStatus status),
}