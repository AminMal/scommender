package ir.ac.usc
package utils

import Bootstrap.{spark, system}

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}
import conf.{RecommenderDataPaths => Paths}

import DataFrameSchemas._
import akka.NotUsed
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.{ExecutionContext, Future}


object DataFrames {

  def usersDF: DataFrame = spark.read
    .schema(usersSchema)
    .parquet(path = Paths.usersPath)

  def songsDF: DataFrame = spark.read
    .schema(songsStruct)
    .parquet(path = Paths.songsPath)

  def ratingsDF: DataFrame = spark.read
    .schema(ratingsStruct)
    .parquet(path = Paths.ratingsPath)

  def trainingDF: DataFrame = spark.read
    .schema(ratingsStruct)
    .parquet(path = Paths.trainPath)

  def testDataDF: DataFrame = spark.read
    .parquet(path = Paths.testPath)

  private def dfSource(df: DataFrame): Source[Row, NotUsed] = Source(df.collect())

  private def rowConversionFlow(implicit ec: ExecutionContext): Flow[Row, Rating, NotUsed] = {
    Flow[Row].mapAsyncUnordered[Rating](4)(row => Future {
      val userId = row.getLong(0).toInt
      val songId = row.getLong(1).toInt
      val target = row.getDouble(2)

      Rating(
        user = userId,
        product = songId,
        rating = target
      )
    })
  }

  private def aggregatorSink: Sink[Rating, Future[Seq[Rating]]] = Sink.seq[Rating]

  private def ratingsGraph(implicit ec: ExecutionContext): RunnableGraph[Future[Seq[Rating]]] = {
    dfSource(ratingsDF).viaMat(rowConversionFlow)(Keep.none)
      .toMat(aggregatorSink)(Keep.right)
  }

  private def trainingGraph(implicit ex: ExecutionContext): RunnableGraph[Future[Seq[Rating]]] = {
    dfSource(trainingDF).viaMat(rowConversionFlow)(Keep.none)
      .toMat(aggregatorSink)(Keep.right)
  }

  def ratingsRddF(implicit ec: ExecutionContext): Future[RDD[Rating]] =
    ratingsGraph.run().map(ratings => spark.sparkContext.parallelize(ratings))(executor = ec)

  def trainRddF(implicit ec: ExecutionContext): Future[RDD[Rating]] =
    trainingGraph.run().map(ratings => spark.sparkContext.parallelize(ratings))(executor = ec)

}
