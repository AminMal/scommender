package scommender
package service.algebra

import controllers.ContextManagerActor.Messages.AddUserRating
import models.{SongDTO, User}
import utils.box.BoxF

import akka.Done
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel


/**
 * Service representing context manager actor
 */
trait ContextManagerServiceAlgebra {

  /**
   * update the application model based on the latest application config
   */
  def updateModel(): Unit

  /**
   * fetch latest matrix factorization model that application is using
   *
   * @return if a trained model exists, that model is returned, else empty value for Option[MatrixFactorizationModel]
   */
  def getLatestModel: BoxF[Option[MatrixFactorizationModel]]

  /**
   * add a song rating for a user
   *
   * @param userId id for the user
   * @param songId id for the song
   * @param rating rating to the song
   * @return operation result, could be failed or successful
   */
  def addUserRating(userId: Long, songId: Long, rating: Double): BoxF[Done]

  /**
   * add a song rating for a user
   *
   * @param request object of type AddUserRating, holding userId, songId, rating
   * @return operation result, could be failed or successful
   */
  def addUserRating(request: AddUserRating): BoxF[Done]

  /**
   * add a user to users
   *
   * @param user user to add
   * @return operation result, could be failed or successful
   */
  def addUser(user: User): BoxF[Done]

  /**
   * add a song to songs
   *
   * @param song song to add
   * @return operation result, could be failed or successful
   */
  def addSong(song: SongDTO): BoxF[Done]


}
