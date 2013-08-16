package com.hanborq.gravity;

import com.hanborq.gravity.conf.GravityConstants;
import org.apache.hadoop.conf.Configuration;

import java.lang.reflect.Constructor;


/**
 * WorkFactory create new Work instances of a certain type. Any implementation
 * of WorkFactory must a constructor with a single parameter of Configuration
 * type, such as:
 * <p>
 *   public SubWorkFactory(Configuration conf) {
 *     ...
 *   }
 * </p>
 * .
 */
public abstract class WorkFactory {

  /**
   * Create a work factory that can be used to generate Work instance of
   * certain type.
   * @param conf configuration
   * @return new work factory
   */
  public static WorkFactory getFactory(Configuration conf)
          throws WorkException {
    String workFactoryClass = conf.get(GravityConstants.WORK_FACTORY_CLASS,
            GravityConstants.DEFAULT_WORK_FACTORY_CLASS);
    try {
      Class clazz = Class.forName(workFactoryClass);
      clazz = clazz.asSubclass(WorkFactory.class);
      Constructor constructor = clazz.getDeclaredConstructor(Configuration.class);
      return (WorkFactory) constructor.newInstance(conf);
    } catch (Exception e) {
      throw new WorkException("Create work factory failed", e);
    }
  }

  /**
   * Create a new Work instance(of course, no necessary to be a really new one).
   * @return new work instance.
   */
  public abstract Work createWorkInstance() throws WorkException;
}
