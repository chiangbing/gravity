package com.hanborq.gravity.utils;

import java.net.Inet4Address;
import java.net.UnknownHostException;


public class EnvHelper {

  public static String currentHost() {
    try {
      return Inet4Address.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return System.getenv("HOST");
    }
  }
}
