package scommender
package service

import models.{RecommendationResult, User}
import org.scalatest.{AsyncWordSpec, Matchers}

class RecommendationServiceSpec extends BoxFWordSpecLike with Matchers {

  val provider = new ServiceProvider("recommendation-service")
  import provider._

  val someUser: User = User(
    userId = 1069699,
    cityId = None,
    gender = "none"
  )

  val count: Int = 4

  "recommender service" should {
    "return recommendations for specific user" inBox {
      service.recommendationManagerService.getRecommendations(someUser.userId, count = count)
        .map { result =>
          assert(result.isInstanceOf[RecommendationResult] && result.songs.length == count)
        }
    }
  }

}
