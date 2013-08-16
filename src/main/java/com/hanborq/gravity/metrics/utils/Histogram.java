package com.hanborq.gravity.metrics.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Histogram.
 */
public class Histogram {


  /**
   * SplitBinAlgorithm define the behaviour how histogram is split into list
   * of consecutive bins.
   */
  public static interface SplitBinAlgorithm {
    public List<DataBin> splitBins(long min, long max, long size);
  }

  /**
   * Split histogram to a list of uniform-sized bins.
   */
  public static class UniformBinSplitter implements SplitBinAlgorithm {
    @Override
    public List<DataBin> splitBins(long min, long max, long size) {
      List<DataBin> bins = Lists.newArrayList();

      long binNum = (long) Math.ceil(Math.log(size) + 1);
      long binSize = (long) Math.ceil((double)(max - min) / binNum);
      long lower = min;

      for (int i = 0; i < binNum; ++i) {
        bins.add(new DataBin(lower, binSize));
        lower += binSize;
      }

      return bins;
    }
  }

  /**
   * Split histogram to a list of 10-based logarithm scaled bins.
   */
  public static class Log10BinSplitter implements SplitBinAlgorithm {

    @Override
    public List<DataBin> splitBins(long min, long max, long size) {
      List<DataBin> bins = Lists.newArrayList();

      // the display histogram is set to width of 960px, set binNum to 320 so
      // that each bin is 2px wide.
      long binNum = 480L;
      // set min to zero cause lots of problem when do logarithm calculation
      long lower = Math.max(1, min);
      double exp = Math.max(0.1, Math.log10(min));
      double expDelta = (Math.log10(max) - exp) / binNum;

      for (long i = 0; i < binNum; ++i) {
        exp += expDelta;
        long width = (long) (Math.pow(10, exp) - lower);
        bins.add(new DataBin(lower, width));
        lower += width;
      }

      return bins;
    }
  }


  /* Built-in bin splitters. */
  private static final Map<String, SplitBinAlgorithm> SPLITTERS = Maps.newHashMap();
  static {
    SPLITTERS.put("uniform", new UniformBinSplitter());
    SPLITTERS.put("log10", new Log10BinSplitter());
  }

  /* name of histogram */
  private String name;

  /* x-axis tag name */
  private String xTag;

  /* y-axis tag name */
  private String yTag;

  /* bins of histogram */
  private List<DataBin> bins = Lists.newArrayList();

  /* a temporary key to be used in indexing bin for item */
  private DataBin locateBin = new DataBin();

  /* the minimum seen value */
  private long min = Long.MAX_VALUE;

  /* the maximum seen value */
  private long max = Long.MIN_VALUE;

  /* sum of all values (to calculate average) */
  private long sum = 0;

  /* number of values (to calculate average) */
  private long totalCount = 0;

  /* percentiles */
  private Percentile percentile = new Percentile();

  /**
   * Create a histogram.
   * @param binSplitter bin splitter.
   * @param rangeMin estimated minimum value that the histogram accepts.
   * @param rangeMax estimated maximum value that the histogram accepts.
   * @param estimatedSize estimated maximum number of data points.
   * @return new histogram instance.
   */
  public static Histogram createHistogram(String name,
                                          String xTag,
                                          String yTag,
                                          String binSplitter,
                                          long rangeMin,
                                          long rangeMax,
                                          long estimatedSize) {
    SplitBinAlgorithm splitter = SPLITTERS.get(binSplitter);
    if (splitter == null) {
      try {
        // not a built-in one, try to treat as a class name
        Class<?> clazz = Class.forName(binSplitter);
        splitter = (SplitBinAlgorithm) clazz.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Create histogram failed", e);
      }
    }

    List<DataBin> bins = splitter.splitBins(rangeMin, rangeMax, estimatedSize);
    return new Histogram(name, xTag, yTag, bins);
  }


  /**
   * Create a histogram with a list of continuous sorted bins.
   * @param bins sorted bins.
   */
  public Histogram(String name,
                   String xTag,
                   String yTag,
                   List<DataBin> bins) {
    this.name = name;
    this.xTag = xTag;
    this.yTag = yTag;
    this.bins = bins;
  }

  /*
   * This ordering is only used in add(Long item) to search which bin
   * the to-be-added should fall in. The comparison is simple: when one bin A
   * is ahead of another B(no overlap), then A < B; when A and B is overlapped,
   * then A == B.
   * Given a bin and an Long item which considered as a bin with width=1,
   * if the bin == the item, it also means the item falls into the bin.
   */
  private static Ordering<DataBin> binIndexOrdering = new Ordering<DataBin>() {
    @Override
    public int compare(DataBin left, DataBin right) {
      if (left.getUpper() <= right.getLower()) {
        return -1;
      } else if (left.getLower() >= right.getUpper()) {
        return 1;
      } else {
        // if two bins are overlap, considered them equal
        return 0;
      }
    }
  };

  /**
   * Add a value to histogram.
   * @param value value.
   * @return true if a value is added successfully to one of bins,
   *        or false vice vera.
   */
  public boolean add(Long value) {
    // track min & max values
    if (value < min) {
      min = value;
    }
    if (value > max) {
      max = value;
    }

    // track sum and count for average
    sum += value;
    ++totalCount;

    // update percentile
    percentile.add(value);

    // add value to bins
    locateBin.setLower(value);
    locateBin.setWidth(1);
    int idx = binIndexOrdering.binarySearch(bins, locateBin);
    if (idx < 0) {
      // bin not found, should not happen
      return false;
    } else {
      bins.get(idx).increaseCount();
      return true;
    }
  }

  /**
   * Get bins.
   * @return bins.
   */
  public List<DataBin> getBins() {
    return bins;
  }

  /**
   * Get the up-to-date minimum value.
   * @return the minimum value.
   */
  public long getMin() {
    return min;
  }

  /**
   * Get the up-to-date maximum value.
   * @return the maximum value.
   */
  public long getMax() {
    return max;
  }

  /**
   * Get the up-to-date average of all values.
   * @return average value.
   */
  public double getAverage() {
    return ((double)sum) / totalCount;
  }

  /**
   * Get the up-to-date count of all values.
   * @return total count of all values.
   */
  public long getTotalCount() {
    return totalCount;
  }

  /**
   * Get histogram as JSON.
   * @return JSON format of histogram.
   */
  public String toJSON() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    LinkedHashMap<String, Object> rootNode = Maps.newLinkedHashMap();

    rootNode.put("name", name);
    rootNode.put("xTag", xTag);
    rootNode.put("yTag", yTag);

    // add min, max, average
    rootNode.put("totalCount", getTotalCount());
    rootNode.put("min", getMin());
    rootNode.put("max", getMax());
    rootNode.put("average", getAverage());

    // add frequencies for drawing bars
    ArrayList<Object> binList = Lists.newArrayList();
    for (DataBin bin : getBins()) {
      LinkedHashMap<String, Object> binNode = Maps.newLinkedHashMap();
      binNode.put("lower", bin.getLower());
      binNode.put("upper", bin.getUpper());
      binNode.put("count", bin.getCount());
      binList.add(binNode);
    }
    rootNode.put("bins", binList);

    // add 25%, 50%, 75%, 99% percentiles
    ArrayList<Double> percents = Lists.newArrayList(0.25d, 0.5d, 0.75d, 0.99d);
    List<Long> percentiles = percentile.getPercentiles(percents);
    assert percentiles.size() == percents.size();

    ArrayList<Object> percentileList = Lists.newArrayList();
    for (int i = 0; i < percents.size(); ++i) {
      LinkedHashMap<String, Object> percentileNode = Maps.newLinkedHashMap();
      percentileNode.put("percent", percents.get(i));
      percentileNode.put("position", percentiles.get(i));
      percentileList.add(percentileNode);
    }
    rootNode.put("percentiles", percentileList);

    return mapper.writeValueAsString(rootNode);
  }

}
