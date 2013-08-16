package com.hanborq.gravity;

import com.hanborq.gravity.conf.GravityConstants;
import org.apache.hadoop.conf.Configuration;


public class DefaultWorkFactory extends WorkFactory {

  private String workId = null;
  private Class workClass = null;
  private Configuration workConf;


  /**
   * The required constructor.
   * @param conf configuration.
   */
  public DefaultWorkFactory(Configuration conf) throws WorkException {
    workId = conf.get(GravityConstants.WORK_ID);
    if (workId == null || workId.isEmpty()) {
      throw new WorkException("Work ID not found when initializing factory");
    }
    String workClassName = conf.get(GravityConstants.WORK_CLASS);
    if (workClassName == null || workClassName.isEmpty()) {
      throw new WorkException(
              "Work class name not found when initializing factory");
    }
    try {
      workClass = Class.forName(workClassName);
    } catch (ClassNotFoundException e) {
      throw new WorkException(
              "Work class not found when initializing WorkFactory", e);
    }
    workConf = new Configuration(conf);
  }

  @Override
  public Work createWorkInstance() throws WorkException {
    try {
      Work work = (Work) workClass.newInstance();
      work.setId(workId);
      work.setConf(workConf);
      return work;
    } catch (Exception e) {
      throw new WorkException("Create work instance failed", e);
    }
  }
}
