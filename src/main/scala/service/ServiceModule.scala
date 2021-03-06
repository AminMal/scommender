package scommender
package service

import controllers._
import service.algebra._
import service.impl._

import akka.actor.ActorSystem
import akka.util.Timeout
import utils.DataFrameProvider


/**
 * This trait holds all the services required inside.
 */
sealed class ServiceModule(
                            val system: ActorSystem,
                            dataframeProducer: () => DataFrameProvider
                          )(implicit timeout: Timeout) {

  import system.dispatcher

  lazy val applicationStatusService: ApplicationStatusServiceAlgebra =
    new ApplicationStatusService(
      system.actorOf(ApplicationStatusController.props, name = ApplicationStatusController.name)
    )


  lazy val configurationManagementService: ConfigurationManagementServiceAlgebra =
    new ConfigurationManagerService(
      system.actorOf(ConfigManagerActor.props)
    )

  lazy val contextManagerService: ContextManagerServiceAlgebra =
    new ContextManagerService(
      system.actorOf(ContextManagerActor.props(dataframeProducer))
    )

  lazy val performanceEvaluatorService: PerformanceEvaluatorServiceAlgebra =
    new PerformanceEvaluatorService(
      () => system.actorOf(PerformanceEvaluatorActor.props(dataframeProducer())),
      contextManagerService
    )

  lazy val recommendationManagerService: RecommendationServiceAlgebra =
    new RecommendationService(
      system.actorOf(RecommenderManagerActor.props(dataframeProducer))
    )(system.dispatcher, timeout)
}

object ServiceModule {

  /**
   * Creates an instance of service module for given actor system
   *
   * @param actorSystem service actor system
   * @param to          timeout for messages
   * @return a new instance of service module
   */
  def forSystem(
                 actorSystem: ActorSystem,
                 dataframeProducer: () => DataFrameProvider
               )(implicit to: Timeout): ServiceModule =
    new ServiceModule(actorSystem, dataframeProducer)
}