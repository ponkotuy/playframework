/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

/**
 * Play framework.
 *
 * == Play ==
 * [[http://www.playframework.com http://www.playframework.com]]
 */
package object play

package play {
  /**
   * Contains the public API for Scala developers.
   *
   * ==== Read configuration ====
   * {{{
   * val poolSize = configuration.getInt("engine.pool.size")
   * }}}
   *
   * ==== Use the logger ====
   * {{{
   * Logger.info("Hello!")
   * }}}
   *
   * ==== Define a Plugin ====
   * {{{
   * class MyPlugin(app: Application) extends Plugin
   * }}}
   *
   * ==== Create adhoc applications (for testing) ====
   * {{{
   * val application = Application(new File("."), this.getClass.getClassloader, None, Play.Mode.DEV)
   * }}}
   *
   */
  package object api
}
