package com.example.akka

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}

object JediValuesAkkaStream {

  /**
   * Reactive Streams -> high throughput and fault tolerant streams of data by simply plugging in streaming components
   * sources =>  producers of data
   * sinks =>  consumers of data
   * flows =>  transformations that will transform elements along the way
   *
   * * Akka streams needs of actor system because Akka streams run on top of actors
   */
  implicit val system: ActorSystem = ActorSystem()

  // streaming components
  val source = Source(1 to 100)
  val flw = Flow[Int].map(_ * 2)
  val sinks = Sink.foreach[Int](println)
}
