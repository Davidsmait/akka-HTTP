package com.example.akka

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.{ExecutionContext, Future}

object JediValuesAkkaStream {

  /**
   * Reactive Streams -> high throughput and fault tolerant streams of data by simply plugging in streaming components
   * sources =>  producers of data
   * flows =>  transformations that will transform elements along the way
   * sinks =>  consumers of data
   *
   * * Akka streams needs of actor system because Akka streams run on top of actors
   */

//    actor system has embedded of what's called materializer so that Akka streams components can have their own life
  implicit val system: ActorSystem = ActorSystem()
  import system.dispatcher

  // streaming components
  val source: Source[Int, NotUsed] = Source(1 to 100)
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(_ * 2)
  val sink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
  val summingSink: Sink[Int, Future[Int]] = Sink.fold[Int,Int](0)((currentSum, incomingElement) => currentSum + incomingElement)

  val graph: RunnableGraph[NotUsed] = source.via(flow).to(sink)
  val anotherGraph: RunnableGraph[Future[Done]] = source.via(flow).toMat(sink)(Keep.right)
  val sumGraph= source.toMat(summingSink)(Keep.both)

  def main(args: Array[String]): Unit = {
//    val jediValue: NotUsed = graph.run() // make the graph come alive
    // NotUsed is a dedicated type in akka stream which is a bit like unit and not useful for processing

//    val anotherJediValue: Future[Done] = anotherGraph.run()
//    anotherJediValue.onComplete(_ => println("Stream is done"))

    val (notuse ,sumGraphValue) = sumGraph.run()
    sumGraphValue.foreach(println)

    //once you start, no turning back
    // Jedi Values = MATERIALIZED VALUES
    // JEDI VALUES MAY OR NOT BE CONNECTTED TO THE ACTUAL ELEMENTS THAT go through the graph
    // jedi values can have any types
  }
}
