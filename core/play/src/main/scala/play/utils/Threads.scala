/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package play.utils

/**
 * provides helpers for managing ClassLoaders and Threads
 */
object Threads {
  /**
   * executes given function in the context of the provided classloader
   * @param classloader that should be used to execute given function
   * @param b function to be executed
   */
  def withContextClassLoader[T](classloader: ClassLoader)(b: => T): T = {
    val thread    = Thread.currentThread
    val oldLoader = thread.getContextClassLoader
    try {
      thread.setContextClassLoader(classloader)
      b
    } finally {
      thread.setContextClassLoader(oldLoader)
    }
  }
}
