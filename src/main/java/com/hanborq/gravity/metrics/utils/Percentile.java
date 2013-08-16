package com.hanborq.gravity.metrics.utils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Trace percentile for frequencies.
 */
public class Percentile {

  private TreeSet<DataBin> binSet = Sets.newTreeSet();

  /* Max DataBin to hold since we don't want to consume too many memory.
   * If too much DataBins are used, merge them into half amount. Thus, the
   * final result may be in a amount of capacity/2 ~ capacity.
   * The default capacity is 200,000, we want to keep a size of
   * 100,000 ~ 200,000, which should be able to do a precise enough percentile
   * calculation. */
  private int capacity = 200000;

  /* total count of value items */
  private int totalCount = 0;

  /* A recycle DataBin to locate position in binSet for new data value. */
  private DataBin locateBin = new DataBin();


  /**
   * Add a value to the calculated date set.
   * @param value new value.
   */
  public synchronized void add(Long value) {
    ++totalCount;
    locateBin.setLower(value);
    locateBin.setWidth(1);

    // use floor to index a potential fit bin
    DataBin floor = binSet.floor(locateBin);
    if (floor != null && floor.equals(locateBin)) {
      // hit!
      binSet.remove(floor);
      floor.increaseCount();
      binSet.add(floor);
    } else {
      // no, we have to create a new bin
      if (ensureCapacity()) {
        binSet.add(new DataBin(value, 1, 1));
      } else {
        // the binSet has been changed, re-locate value
        floor = binSet.floor(locateBin);
        if (floor != null && floor.equals(locateBin)) {
          binSet.remove(floor);
          floor.increaseCount();
          binSet.add(floor);
        } else {
          // the new binSet is half size now, just add it
          binSet.add(new DataBin(value, 1, 1));
        }
      }
    }
  }

  /**
   * Check and ensure capacity is not exceed.
   * @return true if it's there is enough capacity without manipulation, or
   *          false if action has been taken to ensure capacity.
   */
  private boolean ensureCapacity() {
    if (binSet.size() < capacity) {
      return true;
    } else {
      // oh, we have to compress binSet
      compressBinSet();
      return false;
    }
  }

  /**
   * Compress binSet to a smaller-sized one. The compress method is quite
   * simple: two adjacent DataBin is combine into one. The result of compressing
   * like this is that wide-ranged DataBins in sparse area and narrow-ranged
   * DataBin in dense area.
   */
  private void compressBinSet() {
    TreeSet<DataBin> newBinSet = Sets.newTreeSet();
    Iterator<DataBin> iterator = binSet.iterator();

    while (iterator.hasNext()) {
      DataBin first = iterator.next();
      DataBin second = iterator.next();
      if (second != null) {
        // merge second to first
        first.setWidth(second.getUpper() - first.getLower());
        first.setCount(first.getCount() + second.getCount());
      }
      newBinSet.add(first);
    }

    binSet = newBinSet;
  }

  /**
   * Calculated percentiles for passed-in percents.
   * @param percents list of percentage to be calculated.
   * @return percentiles.
   */
  public synchronized List<Long> getPercentiles(List<Double> percents) {
    if (percents == null || percents.isEmpty()) {
      return Lists.newArrayList();
    }

    Map<Double, Long> percentileMap = Maps.newHashMap();
    TreeSet<Double> sortedPercents = Sets.newTreeSet(percents);
    Iterator<Double> sortedPercentIterator = sortedPercents.iterator();
    Double percent = sortedPercentIterator.next();
    long countMark = (long)Math.ceil(totalCount * percent);
    long cumulativeCount = 0;

    for (DataBin dataBin : binSet) {
      while (cumulativeCount + dataBin.getCount() > countMark) {
        // count mark is reached in this data bin, try the best to guess
        // the percentile. we assume the count is evenly distributed in bin.
        long leftCountToMark = countMark - cumulativeCount;
        long percentile = dataBin.getLower()
            + (leftCountToMark / dataBin.getCount()) * dataBin.getWidth();
        percentileMap.put(percent, percentile);

        // calculate next percent
        if (!sortedPercentIterator.hasNext()) {
          percent = null;
          break;
        }
        percent = sortedPercentIterator.next();
        countMark = (long)Math.floor(totalCount * percent);
      }

      if (percent == null) {
        // no more percent to calculate
        break;
      }
      cumulativeCount += dataBin.getCount();
    }

    // compose the result
    List<Long> percentiles = Lists.newArrayList();
    for (Double p : percents) {
      percentiles.add(percentileMap.get(p));
    }

    return percentiles;
  }

  /**
   * Get bin set. Only for test.
   * @return internal bin set storing frequencies.
   */
  public TreeSet<DataBin> getBinSet() {
    return binSet;
  }

  /**
   * Get capacity. Only for test.
   * @return capacity for internal use.
   */
  public int getCapacity() {
    return capacity;
  }
}
