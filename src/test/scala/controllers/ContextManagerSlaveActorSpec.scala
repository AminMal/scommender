package ir.ac.usc
package controllers

import akka.actor.{ActorRef, Props}
import controllers.ContextManagerActor.Responses.CMOperationResult
import models.{SongDTO, User}

class ContextManagerSlaveActorSpec extends ScommenderBaseTestkit {
  import controllers.ContextManagerActor.Messages._

  def newSlave(): ActorRef = system.actorOf(Props[ContextManagerSlaveActor])
  val contextManagerMock: ActorRef = self

  val mockUser: User = User(
    userId = 0,
    cityId = Some(12),
    gender = "female"
  )

  val mockSong: SongDTO = SongDTO(
    id = 12L,
    name = "Some song in test spec",
    artistName = "Scommender",
    length = 67232L,
    genreIds = "12|32",
    language = 12D
  )

  "a context manager slave actor" should {
    "append user to spark users df" in {
      newSlave() ! (AddUser(mockUser) -> contextManagerMock)
      expectMsgType[CMOperationResult]
    }

    "append song to spark songs df" in {
      newSlave() ! (AddSong(mockSong) -> contextManagerMock)
      expectMsgType[CMOperationResult]
    }

    "append rating in spark ratings df" in {
      newSlave() ! (AddUserRating(
        mockUser.userId.toLong,
        songId = mockSong.id,
        rating = 1D
      ) -> contextManagerMock)
      expectMsgType[CMOperationResult]
    }
  }
}