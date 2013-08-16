package com.hanborq.gravity.lib.hbase;

import java.util.Random;

/**
 * Gdr Simulator. Partially copied from HugeTable.
 */
public class GdrSim {
    // GDR data fields
    public long timestamp;
    public String imsi;
    public String msisdn;
    public int sourceip;
    public int destip;
    public byte gdrtype;
    public byte reqnum;
    public String apn;
    public int gdrtime;
    public short result;
    public byte gtpver;
    public byte remoteno;
    public byte frontno;
    public int offset;

    // for data generation
    static final int[] msisdnPrefix = {
            135, 136, 137, 138, 139, 155, 156, 157, 158, 159 };
    static final String[] apns = { "cmnet", "cmwap" };

    private Random random;
    private int repeatMsisdn;
    private String[] bufferedMsisdn;
    private int msPrefix;
    private int msSegment;

    public GdrSim() {
        random = new Random(System.currentTimeMillis());
        repeatMsisdn = 0;
        bufferedMsisdn = new String[100];
        msPrefix = -1;
        msSegment = -1;
    }

    // generate next CDR record
    public void next() {
        timestamp = System.currentTimeMillis();
        if (msPrefix <= 0) {
            msPrefix = msisdnPrefix[random.nextInt(msisdnPrefix.length)];
        }

        if (msSegment <= 0) {
            msSegment = random.nextInt(10);
        }

        int bufferIdx = random.nextInt(bufferedMsisdn.length);
        if (repeatMsisdn <= 0) {
            // use a new one
            bufferedMsisdn[bufferIdx] = Long.toString(msPrefix * 100000000L +
                    msSegment * 100000 + random.nextInt(100000));
            repeatMsisdn = random.nextInt(40);
        } else {
            // use a old one from buffer
            if (bufferedMsisdn[bufferIdx] == null) {
                bufferedMsisdn[bufferIdx] = Long.toString(msPrefix * 100000000L +
                        msSegment * 100000 + random.nextInt(100000));
            }
            repeatMsisdn--;
        }
        msisdn = bufferedMsisdn[bufferIdx];

        imsi = msisdn;
        sourceip = 169345280 + random.nextInt(512);
        destip = 169345536 + random.nextInt(512);
        gdrtype = (byte) random.nextInt(128);
        reqnum = (byte) ((random.nextInt(10) < 1) ? random.nextInt(30) + 2 : 1);
        apn = apns[random.nextInt(apns.length)];
        gdrtime = random.nextInt(1000);
        result = (short) ((random.nextInt(100) < 1) ? random.nextInt(50) + 1 : 0);
        gtpver = (byte) random.nextInt(10);
        remoteno = (byte) random.nextInt(128);
        frontno = (byte) random.nextInt(128);
        offset = random.nextInt(100000);
    }
}
