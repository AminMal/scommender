package ir.ac.usc

import server.RoutesModule

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.http.scaladsl.server._
import utils.ApplicationJsonSupport
import org.scalatest.{Matchers, WordSpecLike}

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class RouteProvider(systemName: String) extends ServiceProvider(systemName) with WordSpecLike
  with Matchers
  with ScalatestRouteTest with ApplicationJsonSupport {

  val provider = new RoutesModule(service)

  implicit val routeTestTimeout: RouteTestTimeout = new RouteTestTimeout(FiniteDuration(60, TimeUnit.SECONDS))
  implicit val tildeArrowInjection: TildeArrow[RequestContext, Future[RouteResult]] {
    type Out = RouteTestResult
  } = TildeArrow.injectIntoRoute
}