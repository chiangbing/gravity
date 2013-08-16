package com.hanborq.gravity.metrics.utils;

import com.google.common.collect.Lists;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Test for Percentile.
 */
public class PercentileTest {

  private static List<Long> DATA = Lists.newArrayList();


  @BeforeClass
  public static void setUpOnce() throws Exception {
    Random random = new Random(System.currentTimeMillis());

    // create 1 million data values
    for (int i = 0; i < 1000000; ++i) {
      DATA.add(random.nextLong() % 150000);
    }
  }

  @Test
  public void testPercentileValidate() {
    Percentile percentile = new Percentile();

    // feed DATA to percentile
    for (Long data : DATA) {
      percentile.add(data);
    }

    TreeSet<DataBin> binSet = percentile.getBinSet();

    // bin set size should be be in range [capacity/2 ~ capacity]
    int binSetSize = binSet.size();
    int expectedMinSize = percentile.getCapacity() / 2;
    int expectedMaxSize = percentile.getCapacity();
    assertThat(binSetSize, greaterThanOrEqualTo(expectedMinSize));
    assertThat(binSetSize, lessThanOrEqualTo(expectedMaxSize));

    // all bins should be monotonically increasing and not overlapped
    DataBin previous = null;
    boolean first = true;
    for (DataBin current : binSet) {
      if (first) {
        previous = current;
        first = false;
        continue;
      }
      assertTrue(current.getLower() >= previous.getUpper());
    }

    // and sum of count of all bins should equal to the size of DATA
    long countSum = 0;
    for (DataBin bin : binSet) {
      countSum += bin.getCount();
    }
    assertEquals(DATA.size(), countSum);

    // all values in DATA should fall into one of those bins
    for (Long value : DATA) {
      assertTrue(binSet.contains(new DataBin(value, 1)));
    }
  }

  @AfterClass
  public static void tearDownOnce() throws Exception {
    DATA = null;
  }

}
