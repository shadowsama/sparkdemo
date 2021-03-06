import org.apache.spark.streaming.dstream.{ReceiverInputDStream, DStream}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import twitter4j.Status
import util.TwitterHelper

// NOTE: You must supply twitter credentials in twitter.txt to run this demo //

object SparkStreaming extends App {

  val happyWords = Set("happy", "love", "laugh", "excited")
  val whitespace = """\s+""".r

  val ssc = new StreamingContext(
    master = "local[4]",
    appName = "SparkStreaming",
    batchDuration = Seconds(1)
  )

  TwitterHelper.configureTwitterCredentials()

  val tweets: ReceiverInputDStream[Status] = TwitterUtils.createStream(ssc, None)
  val statuses: DStream[String] = tweets.map(status => status.getText)

  def filterTweetsWithWords(filterWords: Set[String], statuses: DStream[String]) = statuses.filter { status =>
    !whitespace.split(status).find(word => happyWords.contains(word.toLowerCase)).isEmpty
  }

  val happyTweets = filterTweetsWithWords(happyWords, statuses)

  happyTweets.foreachRDD(rdd => println(s"${rdd.take(10).mkString("\n")}\n\n"))

  ssc.start()
  ssc.awaitTermination()
}
