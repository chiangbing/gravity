package com.hanborq.gravity.metrics.utils;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.TreeSet;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

/**
 * Test for DataBin.
 */
public class DataBinTest {

  @Test
  public void testBasicCompare() {
    DataBin bin1 = new DataBin(1, 1);
    DataBin bin2 = new DataBin(2, 1);

    assertThat(bin1, lessThan(bin2));
    assertThat(bin2, greaterThan(bin1));
    assertFalse(bin1.equals(bin2));

    bin2 = new DataBin(1, 1);
    assertEquals(bin1, bin2);
    assertEquals(bin2, bin1);

    bin2 = new DataBin(1, 2);
    assertEquals(bin1, bin2);
    assertEquals(bin2, bin1);
  }

  @Test
  public void testUseInTreeSet() {
    TreeSet<DataBin> binSet = Sets.newTreeSet();

    DataBin bin1 = new DataBin(1, 1);
    DataBin bin2 = new DataBin(2, 2);
    DataBin bin3 = new DataBin(4, 100);
    DataBin bin4 = new DataBin(1000, 1);

    binSet.add(bin1);
    binSet.add(bin2);
    binSet.add(bin3);
    binSet.add(bin4);

    assertEquals(4, binSet.size());

    DataBin locateBin = new DataBin(1, 1);
    DataBin floor = binSet.floor(locateBin);
    assertNotNull(floor);
    assertEquals(locateBin, floor);

    locateBin = new DataBin(3, 1);
    floor = binSet.floor(locateBin);
    assertNotNull(floor);
    assertEquals(locateBin, floor);

    locateBin = new DataBin(200, 1);
    floor = binSet.floor(locateBin);
    assertNotNull(floor);
    assertFalse(locateBin.equals(floor));
  }
}
