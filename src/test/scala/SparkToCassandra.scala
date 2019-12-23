/*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, SparkContext, sql}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.{SparkSession,SQLContext,SQLImplicits,DataFrameNaFunctions}
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext._
import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._
import org.apache.spark

object SparkToCassandra {
  def main(args: Array[String]): Unit = {
    val brokers = "localhost:9092"
    val groupid = "GRP1"
    val topics = "patient_data2"

    val sparkconf = new SparkConf().setMaster("local[*]").setAppName("KAFKAStreaming")
      .set("spark.cassandra.connection.host", "192.168.111.128").set("spark.cassandra.connection.port", "9042")
    val ssc = new StreamingContext(sparkconf, Seconds(30))

    //sc.setLogLevel("OFF")

    val topicSet = topics.split(",").toSet
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.GROUP_ID_CONFIG -> groupid,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer]
    )

    val messages = KafkaUtils.createDirectStream[String, String](ssc,
      LocationStrategies.PreferConsistent, ConsumerStrategies.Subscribe[String, String](topicSet, kafkaParams))

    /* case class Patient(pId: Int, pGender: String, pAge: Int, pHeartDisease: String, pEverMarried: String, pResidenceType: String,
                       pAvgGlucoseLevel: Double, pBmi: Double, pSmokingStatus: String) extends Serializable */

    messages.map{x => {
      val arr = x.split(",")
      (arr(0), arr(1),arr(2),arr(3),arr(4),arr(5),arr(6),arr(7),arr(8))
    }}.saveToCassandra("test", "sparktable", SomeColumns("sno", "pname"))

    ssc.checkpoint("C:\\Cass")

    /*def updateFunction(values: Seq[Float], runningCount: Option[Float]) = {
      val newCount = values.sum + runningCount.getOrElse(0.0f)
      new Some(newCount)
    } */

  //lines.foreachRDD(rdd => rdd.saveToCassandra("test","people_data",SomeColumns("pid","page","pavgglucoselevel","pbmi","pevermarried","pgender","pheartdisease","presidencetype","psmokingstatus")))
    /*  val PatientRDD = { lines.map(row => {
      val words = row.split(",")
      val patientRDDs = words.map(p => Patient(p(0).toInt, p(1).toString, p(2).toInt, p(3).toString, p(4).toString, p(5).toString, p(6).toDouble, p(7).toDouble, p(8).toString))
      patientRDDs
    })
  }   */

    /* PatientRDD.foreachRDD( rdd => {
       rdd.saveToCassandra("test","People_data",SomeColumns("pid",)
     })     */

    ssc.start()
    ssc.awaitTermination()
  }
}

*/