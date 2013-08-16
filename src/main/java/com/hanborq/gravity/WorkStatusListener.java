package com.hanborq.gravity;

public abstract class WorkStatusListener {

  /**
   * Listen for every work status.
   * @param workStatus work status
   */
  public void onWorkStatus(WorkStatus workStatus) {}

  /**
   * Invoke when work is reported as a pending work at remote room.
   * @param workStatus work status that report work's pending at remote room.
   */
  public void onWorkPending(WorkStatus workStatus) {}

  /**
   * Invoke when work is reported to start to run (run state change from
   * PENDING to RUNNING).
   * @param workStatus work status that report work start to run.
   */
  public void onWorkRunning(WorkStatus workStatus) {}

  /**
   * Invoke when work is reported to be paused (run state change from RUNNING
   * to PAUSED).
   * @param workStatus work status that report work is paused.
   */
  public void onWorkPaused(WorkStatus workStatus) {}

  /**
   * Invoke when work is reported to be resumed (run state change from PAUSED
   * to RUNNING).
   * @param workStatus work status that report work is resumed.
   */
  public void onWorkResumed(WorkStatus workStatus) {}

  /**
   * Invoke when work is aborted (run state change from some non-terminated
   * state to ABORTED).
   * @param workStatus work status that report work is aborted.
   */
  public void onWorkAborted(WorkStatus workStatus) {}

  /**
   * Invoke when work is completed successfully (run state change from some
   * non-terminated state to COMPLETED).
   * @param workStatus work status that report work is completed.
   */
  public void onWorkCompleted(WorkStatus workStatus) {}
}
