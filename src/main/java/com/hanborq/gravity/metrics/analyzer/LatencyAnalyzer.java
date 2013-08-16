package com.hanborq.gravity.metrics.analyzer;

import com.google.common.collect.Maps;
import com.hanborq.gravity.metrics.MetricsAnalyzer;
import com.hanborq.gravity.metrics.MetricsRecord;
import com.hanborq.gravity.metrics.utils.Histogram;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import java.util.Map;


/**
 * Analyze "time" metrics and produce latency-related analysis report.
 */
public class LatencyAnalyzer implements MetricsAnalyzer {

  private static final Log LOG = LogFactory.getLog(LatencyAnalyzer.class);

  // a map translate config time unit key to accurate value in nano seconds
  private static final Map<String, Long> SCALE_FACTORS = Maps.newHashMap();
  static {
    SCALE_FACTORS.put("s", 1000000000L);
    SCALE_FACTORS.put("ms", 1000000L);
    SCALE_FACTORS.put("us", 1000L);
    SCALE_FACTORS.put("ns", 1L);
  }

  // a map translate config time unit key to display name
  private static final Map<String,String> TIME_LABLES = Maps.newHashMap();
  static {
    TIME_LABLES.put("s", "sec");
    TIME_LABLES.put("ms", "ms");
    TIME_LABLES.put("us", "\u00B5s");
    TIME_LABLES.put("ns", "ns");
  }

  /* configurable entries */
  private static final String LATENCY_SCALE_UNIT = "latency.scale.unit";
  private static final String DEFAULT_LATENCY_SCALE_UNIT = "us";
  private static final String HISTOGRAM_BIN_SPLITTER = "latency.histogram.bin.splitter";
  private static final String DEFAULT_BIN_SPLITTER = "log10";
  private static final String HISTOGRAM_RANGE_MIN = "latency.histogram.range.min";
  private static final long DEFAULT_RANGE_MIN = 0;
  private static final String HISTOGRAM_RANGE_MAX = "latency.histogram.range.max";
  private static final long DEFAULT_RANGE_MAX = 10000000;
  private static final String HISTOGRAM_DATA_NUM = "latency.histogram.data.num";
  private static final long DEFAULT_DATA_NUM = 1000000;

  /* internal used entries */
  public static final String TIME_NS_KEY = "time.ns";
  public static final String CTX_KEY = "latency.analyzer";

  /* histogram for latency metrics */
  private Histogram histogram;

  private long scaleFactor = 1;


  public LatencyAnalyzer(Configuration conf) {
    String scaleUnit = conf.get(LATENCY_SCALE_UNIT, DEFAULT_LATENCY_SCALE_UNIT);
    if (SCALE_FACTORS.containsKey(scaleUnit)) {
      scaleFactor = SCALE_FACTORS.get(scaleUnit);
    } else {
      LOG.error("Invalid scale unit for latency: " + scaleUnit +
          ", fallback to us.");
      scaleFactor = SCALE_FACTORS.get("us");
    }

    String binSplitter = conf.get(HISTOGRAM_BIN_SPLITTER, DEFAULT_BIN_SPLITTER);
    long rangeMin = conf.getLong(HISTOGRAM_RANGE_MIN, DEFAULT_RANGE_MIN);
    long rangeMax = conf.getLong(HISTOGRAM_RANGE_MAX, DEFAULT_RANGE_MAX);
    long dataNum = conf.getLong(HISTOGRAM_DATA_NUM, DEFAULT_DATA_NUM);

    histogram = Histogram.createHistogram(
        "Latency", "Time(" + TIME_LABLES.get(scaleUnit) + ")", "Count",
        binSplitter, rangeMin, rangeMax, dataNum);
  }

  @Override
  public void onMetricsRecord(MetricsRecord mr) {
    Long latency = mr.getLongMetrics().get(TIME_NS_KEY);
    if (latency == null) {
      // not a latency metrics record
      return;
    }
    latency /= scaleFactor;

    // feed data to histogram
    boolean inRange = histogram.add(latency);
    if (!inRange) {
      LOG.info("Latency out of histogram range, latency=" + latency);
    }
  }

  public Histogram getHistogram() {
    return histogram;
  }
}
