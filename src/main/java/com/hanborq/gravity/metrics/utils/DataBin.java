package com.hanborq.gravity.metrics.utils;


/**
 * Represent a data bin(bucket), which hold a range of data value and its
 * frequency.
 */
public class DataBin implements Comparable<DataBin> {
  private long lower;
  private long width;
  private long count;

  /**
   * Create a empty bin with zero lower and zero width.
   */
  public DataBin() {
    this(0, 0, 0);
  }

  /**
   * Create a bin.
   * @param lower lower bound of this bin.
   * @param width width of this bin.
   */
  public DataBin(long lower, long width) {
    this(lower, width, 0);
  }

  /**
   * Create a bin.
   * @param lower lower of bin.
   * @param width width of bin.
   * @param initialCount initial count for bin.
   */
  public DataBin(long lower, long width, long initialCount) {
    this.lower = lower;
    this.width = width;
    this.count = initialCount;
  }

  /**
   * Get lower bound.
   * @return lower bound of bin.
   */
  public long getLower() {
    return lower;
  }

  /**
   * Set lower bound.
   * @param lower lower bound of bin.
   */
  public void setLower(long lower) {
    this.lower = lower;
  }

  /**
   * Access upper bound of this DataBin, which derived from lower and width.
   * @return upper bound of bin.
   */
  public long getUpper() {
    return lower + width;
  }

  /**
   * Get width of bin.
   * @return width of bin.
   */
  public long getWidth() {
    return width;
  }

  /**
   * Set width of bin.
   * @param width width of bin.
   */
  public void setWidth(long width) {
    this.width = width;
  }

  /**
   * Get count of bin.
   * @return count of bin.
   */
  public long getCount() {
    return count;
  }

  /**
   * Increase count of bin.
   */
  public void increaseCount() {
    ++count;
  }

  /**
   * Explicitly set the count.
   * @param count
   */
  public void setCount(long count) {
    this.count = count;
  }

  /**
   * Add comparable capability for DataBin.
   * The only meaning for DataBin comparison is for a value to find which
   * DataBin it is belong to. Thus, comparison is designed as:
   * <ol>
   *   <li>If this bin is completely ahead of other (no overlap), then it's
   *   less than other;</li>
   *   <li>If this bin is completely behind of other (no overlap), then it's
   *   larger than other;</li>
   *   <li>If not the above situations neither, then this is overlapped with
   *   other, this is considered equal to other.</li>
   * </ol>
   * @param other other DataBin to compare with.
   * @return  a negative integer, zero, or a positive integer as this object
   *		is less than, equal to, or greater than the specified object.
   */
  @Override
  public int compareTo(DataBin other) {
    if (getUpper() <= other.getLower()) {
      return -1;
    } else if (getLower() >= other.getUpper()) {
      return 1;
    } else {
      // overlapped, since DataBin should be used as a order list of disjoint
      // ones, so two overlapped DataBin is considered as equal.
      return 0;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DataBin)) {
      return false;
    }

    DataBin other = (DataBin)obj;

    return this.compareTo(other) == 0;
  }
}
