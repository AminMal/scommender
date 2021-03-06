package scommender
package controllers

import exception.{EntityNotFoundException, ModelNotTrainedYetException}
import models.RecommendationResult
import utils.box.{BoxSupport, Failed}
import utils.{DataFrameProvider, ResultParser, ResultParserImpl}

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import service.DiagnosticsService

import java.time.temporal.ChronoUnit


/**
 * This actor (controller) handles recommending songs for users, based on the MatrixFactorizationModel
 * generated by context manager actor. and is managed by recommender manager actor and killed right after
 * the job is done.
 *
 * @param resultParser an implementation of result parser to fetch user and song data from DataFrames
 */
private[controllers] class RecommendationController(resultParser: ResultParser) extends Actor with ActorLogging with BoxSupport {

  import utils.TimeUtils.timeTrack
  import RecommendationController.Messages._


  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case UpdateContext(model) =>
      context.become(receiveWithModel(model))

    case GetRecommendations(_, _) =>
      sender() ! Failed[RecommendationResult](ModelNotTrainedYetException)
      self ! PoisonPill
  }

  def receiveWithModel(model: MatrixFactorizationModel): Receive = {
    case GetRecommendations(userId, count) =>

      val recommendationResult = for {
        _ <- toBox {
          timeTrack(operationName = "Get user info", ChronoUnit.MILLIS) {
            resultParser.getUserInfo(userId)
              .getOrElse(throw EntityNotFoundException(entity = "user", id = Some(userId.toString)))
          }
        }

        recommendations <- toBox {
          timeTrack(operationName = "Getting recommendations from model", ChronoUnit.MILLIS) {
            model.recommendProducts(userId, count)
          }
        }

        songs <- toBox {
          timeTrack(operationName = "Getting song info from recommendation result", ChronoUnit.MILLIS) {
            resultParser.getSongDTOs(recommendations)
          }
        }
      } yield {
        DiagnosticsService.incrementModelServedRequestsForReport(model)
        new RecommendationResult(userId = userId, songs = songs.take(count))
      }

      sender() ! recommendationResult
      self ! PoisonPill
  }

}

object RecommendationController {

  /**
   * Generates recommendation controller actor Props in order to create new reference of this actor.
   *
   * @return props for this actor
   */
  def props(dataFrameProvider: DataFrameProvider): Props =
    Props(new RecommendationController(new ResultParserImpl(dataFrameProvider)))

  /**
   * Messages that this actor accepts.
   */
  object Messages {
    case class UpdateContext(model: MatrixFactorizationModel)

    case class GetRecommendations(userId: Int, count: Int = 6)
  }
}
