package com.hanborq.gravity;

public enum WorkState {
  /** Submitted to gravity room, but not reported as PENDING yet. */
  SUBMITTED,
  /** Wait in gravity room to be launched. */
  PENDING,
  /** Running. */
  RUNNING,
  /** Paused by user. */
  PAUSED,
  /** Stop manually by user. */
  ABORTED,
  /** Complete successfully. */
  COMPLETED,
  /** Too many errors */
  ERROR,
  /** Fatal, unrecoverable error. */
  FATAL,
}