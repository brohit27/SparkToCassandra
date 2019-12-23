
import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, SparkContext, sql}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.{DataFrameNaFunctions, SQLContext, SQLImplicits, SparkSession}
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext._
import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._
import org.apache.spark

object KafkaSparkStreaming {
  def main(args: Array[String]): Unit = {
    val brokers = "localhost:9092"
    val groupid = "GRP1"
    val topics = "patient_data2"

    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("KAFKAStreaming").set("spark.cassandra.connection.host","192.168.111.131")
      .set("spark.cassandra.connection.port","9042")
      .set("spark.cassandra.auth.username", "healthcare-project").set("spark.cassandra.auth.password", "password")
      .set("spark.cassandra.output.consistency.level","LOCAL_ONE")

    val ssc = new StreamingContext(sparkConf, Seconds(30))

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
    val lines = messages.map(_.value)

    val PatientRDD = lines.map(row => {

      val columnValues = row.split(",")
      val pId = columnValues(0).toInt
      val pName = columnValues(1)
      val pGender = columnValues(2)
      val pAge = columnValues(3) .toInt
      val pHeartDisease = columnValues(4)
      val pEverMarried = columnValues(5)
      val pResidenceType = columnValues(6)
      val pAvgGlucoseLevel = columnValues(7).toDouble
      val pBmi = columnValues(8).toDouble
      val pSmokingStatus = columnValues(9)
      val pLoadTime ={
       val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
        val time = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).format(fmt)
        time
      }
      (pId,pLoadTime,pAge,pAvgGlucoseLevel,pBmi,pEverMarried,pGender,pHeartDisease,pName,pResidenceType,pSmokingStatus)
    })

    PatientRDD.foreachRDD(rdd => {
      rdd.saveToCassandra("test","people_data1",SomeColumns("pid","ploadtime","page","pavgglucoselevel","pbmi","pevermarried","pgender","pheartdisease","pname","presidencetype","psmokingstatus"))})

   // PatientRDD.map(a => a._1+","+a._2)
    // kick it off
    ssc.start()
    ssc.awaitTermination()
  }
}