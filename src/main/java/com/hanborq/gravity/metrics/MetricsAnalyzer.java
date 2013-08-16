package com.hanborq.gravity.metrics;


/**
 * Process metrics record when received in MetricsServer.
 */
public interface MetricsAnalyzer {

    public void onMetricsRecord(MetricsRecord mr);
}
