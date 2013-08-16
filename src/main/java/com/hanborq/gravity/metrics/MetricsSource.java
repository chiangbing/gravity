package com.hanborq.gravity.metrics;

/**
 * A source that can be pulled metrics from.
 */
public interface MetricsSource {
    public void pullMetrics(MetricsCollector collector);
}
