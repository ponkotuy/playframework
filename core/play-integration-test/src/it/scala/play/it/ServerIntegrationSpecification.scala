/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package play.it

import org.specs2.execute._
import org.specs2.mutable.Specification
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AroundEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.core.server.NettyServer
import play.core.server.ServerProvider
import play.core.server.AkkaHttpServer

import scala.concurrent.duration._

/**
 * Helper for creating tests that test integration with different server
 * backends. Common integration tests should implement this trait, then
 * two specific tests should be created, one extending NettyIntegrationSpecification
 * and another extending AkkaHttpIntegrationSpecification.
 *
 * When a test extends this trait it will automatically get overridden versions of
 * TestServer and WithServer that delegate to the correct server backend.
 */
trait ServerIntegrationSpecification extends PendingUntilFixed with AroundEach {
  parent =>
  implicit def integrationServerProvider: ServerProvider

  val isTravis: Boolean                = sys.env.get("TRAVIS").exists(_.toBoolean)
  val isTravisCron: Boolean            = sys.env.get("TRAVIS_EVENT_TYPE").exists(_.equalsIgnoreCase("cron"))
  val isContinuousIntegration: Boolean = isTravis
  val isCronBuild: Boolean             = isTravisCron // TODO We don't have cron builds for CircleCI yet

  def aroundEventually[R: AsResult](r: => R) = {
    EventuallyResults.eventually[R](1, 20.milliseconds)(r)
  }

  def around[R: AsResult](r: => R) = {
    AsResult(aroundEventually(r))
  }

  implicit class UntilAkkaHttpFixed[T: AsResult](t: => T) {
    /**
     * We may want to skip some tests if they're slow due to timeouts. This tag
     * won't remind us if the tests start passing.
     */
    def skipUntilAkkaHttpFixed: Result = parent match {
      case _: NettyIntegrationSpecification    => ResultExecution.execute(AsResult(t))
      case _: AkkaHttpIntegrationSpecification => Skipped()
    }
  }

  implicit class UntilFastCIServer[T: AsResult](t: => T) {
    def skipOnSlowCIServer: Result = {
      if (isContinuousIntegration) Skipped()
      else ResultExecution.execute(AsResult(t))
    }
  }

  /**
   * Override the standard TestServer factory method.
   */
  def TestServer(
      port: Int,
      application: Application = play.api.PlayCoreTestApplication(),
      sslPort: Option[Int] = None
  ): play.api.test.TestServer = {
    play.api.test.TestServer(port, application, sslPort, Some(integrationServerProvider))
  }

  /**
   * Override the standard WithServer class.
   */
  abstract class WithServer(
      app: play.api.Application = GuiceApplicationBuilder().build(),
      port: Int = play.api.test.Helpers.testServerPort
  ) extends play.api.test.WithServer(
        app,
        port,
        serverProvider = Some(integrationServerProvider)
      )
}

/** Run integration tests against a Netty server */
trait NettyIntegrationSpecification extends ServerIntegrationSpecification {
  self: SpecificationLike =>

  // Do not run Netty tests in continuous integration, unless it is a cron build.
  private val skipNettyTests = isContinuousIntegration && !isCronBuild
  skipAllIf(skipNettyTests)

  // Be silent about skipping Netty tests to avoid useless output
  if (skipNettyTests) xonly

  final override def integrationServerProvider: ServerProvider = NettyServer.provider
}

/** Run integration tests against an Akka HTTP server */
trait AkkaHttpIntegrationSpecification extends ServerIntegrationSpecification {
  self: SpecificationLike =>

  final override def integrationServerProvider: ServerProvider = AkkaHttpServer.provider
}
