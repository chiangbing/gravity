package com.hanborq.gravity;

import com.hanborq.gravity.metrics.MetricsSource;


/**
 * WorkRunner is responsible for running work in GravityRoom.
 */
public interface WorkRunner extends MetricsSource {

  /**
   * Start this runner.
   */
  public void start();

  /**
   * Pause this runner, which can be resumed with {@link #resume()}
   */
  public void pause();

  /**
   * Resume this runner.
   * Do nothing when runner is not paused.
   */
  public void resume();

  /**
   * Stop this runner.
   * Do nothing when runner is already terminated.
   */
  public void stop();

  /**
   * Get status.
   * @return runner status.
   */
  public WorkState getState();

  /**
   * Get running progress, in percentage.
   * @return progress
   */
  public double getProgress();
}
