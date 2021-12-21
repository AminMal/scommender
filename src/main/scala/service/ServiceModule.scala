package ir.ac.usc
package service

import controllers._
import service.algebra._
import service.impl._

import akka.actor.ActorSystem
import controllers.ApplicationStatusController.Responses.HealthCheckResponse
import scala.concurrent.{ExecutionContext, Future}


trait ServiceModule {

  /* Only implementation of actor system is required to get started */
  val system: ActorSystem

  lazy val applicationStatusService: ApplicationStatusServiceAlgebra =
    new ApplicationStatusService(
      system.actorOf(ApplicationStatusController.props)
    )


  lazy val configurationManagementService: ConfigurationManagementServiceAlgebra =
    new ConfigurationManagerService(
      system.actorOf(ConfigManagerActor.props)
    )

  lazy val contextManagerService: ContextManagerServiceAlgebra =
    new ContextManagerService(
      system.actorOf(ContextManagerActor.props)
    )

  lazy val performanceEvaluatorService: PerformanceEvaluatorServiceAlgebra =
    new PerformanceEvaluatorService(
      () => system.actorOf(PerformanceEvaluatorActor.props),
      contextManagerService
    )

  lazy val recommendationManagerService: RecommendationServiceAlgebra =
    new RecommendationService(
      system.actorOf(RecommenderManagerActor.props)
    )(system.dispatcher)

  /* since modules are evaluated lazily, this initiate method just invokes the first lazy evaluation
     and makes things run, although, you can send any HTTP request to do this, but this method is called
     right after server start to have a smoother flow */
  def initiate(implicit ec: ExecutionContext): Future[HealthCheckResponse] = {
    applicationStatusService.health().map { response =>
      println(s"--- initialized application service, initial health check response: ${response.currentTime} ---")
      response
    }
  }

}

object ServiceModule {
  def forSystem(actorSystem: ActorSystem): ServiceModule = new ServiceModule {
    override val system: ActorSystem = actorSystem
  }
}