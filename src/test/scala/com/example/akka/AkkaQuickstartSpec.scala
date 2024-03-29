//#full-example
package com.example.akka

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, Exceptional, Failed, Outcome, Pending, Succeeded, color, freespec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import akka.http.scaladsl.model.StatusCodes
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

//#definition
class AkkaQuickstartSpec extends freespec.AnyFreeSpec with MockFactory with BeforeAndAfter with Matchers with ScalatestRouteTest{
  override def withFixture(test: NoArgTest): Outcome = {
    super.withFixture(test) match {
      case exceptional: Exceptional =>
        info("exceptional")
        exceptional
      case Succeeded =>
        info("Succeeded")
        Succeeded
      case Pending =>
        info("Pending")
        Pending
    }
  }

  after()


  "given" - {
    "when" - {
      "then" in {
        // Models
        case class Model1(id: Long, name: String)
        case class Model2(id: Long, name: String)

        // Services
        trait UnitService1 {
          def doSomething(m1: Model1, m2: Model2): Unit
        }

        class UnitService2Impl(srv1: UnitService1) extends UnitService1 {
          def doSomething(m1: Model1, m2: Model2): Unit = {
            srv1.doSomething(m1, m2)
          }
        }

        val m1 = Model1(1L, "Jim")
        val m2 = Model2(2L, "Timmy")
        val mockUnitService1 = mock[UnitService1]
        (mockUnitService1.doSomething _).expects(m1, m2)

        val unitService2 = new UnitService2Impl(mockUnitService1)
        unitService2.doSomething(m1, m2)
        succeed

      }
    }
  }


  "when: cuando dices hola" - {
    trait Formatter {
      def format(s: String): String
    }
    object EnglishFormatter extends Formatter {
      def format(s: String): String = s"Hello $s"
    }
    object GermanFormatter extends Formatter {
      def format(s: String): String = s"Hallo $s"
    }
    object JapaneseFormatter extends Formatter {
      def format(s: String): String = s"こんにちは $s"
    }

    object Greetings {
      def sayHello(name: String, formatter: Formatter): Unit = {
        println(formatter.format(name))
      }
    }

    "then: sea correcto" in {
      val mockFormatter = mock[Formatter]

      (mockFormatter.format _)
        .expects("Mr bondo")
        .returning("Ah, Mr bondo , I've been expecting you")
        .once()

      Greetings.sayHello("Mr bondo", mockFormatter)

    }
    "then: sea incorrecto" in {
      val brokenFormatter = mock[Formatter]
      (brokenFormatter.format _)
        .expects(*)
        .throwing(new NullPointerException)
        .anyNumberOfTimes()

      intercept[NullPointerException] {
        Greetings.sayHello("Erza", brokenFormatter)
      }
    }

    "then: australian format" in {
      val autstralianFormat = mock[Formatter]

      (autstralianFormat.format _)
        .expects(*)
        .onCall { s: String => s"G'day $s" }
        .twice()

      Greetings.sayHello("Wendy", autstralianFormat)
      Greetings.sayHello("Gray", autstralianFormat)
    }

    "then: verifying parameters dynamically" in {
      val teamNatsu = Set("Natsu", "Lucy", "Happy", "Erza", "Gray", "Wendy", "Carla")
      val formatter = mock[Formatter]

      def assertTeamNatsu(s: String): Unit = {
        assert(teamNatsu.contains(s))
      }

      // argAssert fails early
      (formatter.format _).expects(argAssert(assertTeamNatsu _)).onCall { s: String => s"Yo $s" }.once()

      // 'where' verifies at the end of the test
      (formatter.format _).expects(where { s: String => teamNatsu contains (s) }).onCall { s: String => s"Yo $s" }.twice()

      Greetings.sayHello("Carla", formatter)
      Greetings.sayHello("Happy", formatter)
      Greetings.sayHello("Lucy", formatter)
    }

    "with: fixture" - {
      class Fixture {
        val builder = new mutable.StringBuilder("Scalatest is ")
        val buffer = new ListBuffer[String]
      }

      def fixture = new Fixture

      "then: using fixture' should be easy" in {
        val f = fixture
        f.builder.append("easy!")
        assert(f.builder.toString() === "Scalatest is easy!")
        assert(f.buffer.isEmpty)
        f.buffer += "sweet"
        info(f.buffer.toString)
      }

      "then: using fixture, should be fun" in {
        val f = fixture
        f.builder.append("fun!")
        assert(f.builder.toString() === "Scalatest is fun!")
        assert(f.buffer.isEmpty)
      }

    }

    "with: fixture-context object" - {
      trait Builder {
        val builder = new mutable.StringBuilder("Scala test is ")
      }
      trait Buffer {
        val buffer: ListBuffer[String] = ListBuffer("ScalaTest", "is")
      }

      "then: builder should be productive" in new Builder {
        builder.append("productive")
        assert(builder.toString === "Scala test is productive")
      }

      "then: buffer should be readable" in new Buffer {
        buffer += "readable"
        assert(buffer === List("ScalaTest", "is", "readable"))
      }

      "then: builder & buffer should be clear an concise" in new Builder with Buffer {
        builder.append("clear!")
        buffer += "concise!"
        assert(builder.toString() === "Scala test is clear!")
        assert(buffer === List("ScalaTest", "is", "concise!"))
      }
    }

    "with: overriding with fixture" -  {
      "then: success" in {
        assert(1+1 === 2)

      }
      "then: shouldfail" in {
        assert(1+1 === 4)
      }
    }

    "with: trying implement a fixture" - {
      "then  use the fixture" in {

      }
    }

    "with: route service" - {
      val smallRoute =
        get {
          concat(
            pathSingleSlash {
              complete {
                "Captain on the bridge!"
              }
            },
            path("ping") {
              complete("PONG!")
            }
          )
        }
      "then: return a greeting for GET requests to the root path" in {
        Get() ~> smallRoute ~> check {
          responseAs[String] shouldEqual "Captain on the bridge!"
        }
      }

      "then: return a pong for /ping routes" in {
        Get("/ping") ~> smallRoute ~> check {
          responseAs[String] shouldEqual "PONG!"
        }
      }

      "then: unhandled paths" in {
        Get("/kermit") ~> smallRoute ~> check {
          handled shouldBe false
        }
      }

      //Route.seal()
      // method applies the logic of the in-scope ExceptionHandler and RejectionHandler
      // to all exceptions and rejections coming back from the route, and translates them
      // to the respective HttpResponse.
      //
      //Note:
      // that explicit call on the Route.seal method is needed in test code,
      // but in your application code it is not necessary.
      // As described in Sealing a Route, your application code only needs to bring implicit rejection and exception handlers in scope.
      "then: HTTP method not allowed" in {
        Put() ~> Route.seal(smallRoute) ~> check {
          status shouldEqual StatusCodes.MethodNotAllowed
        }
      }

      "then: Delete method not allowed xd" in {
        Delete() ~> Route.seal(smallRoute) ~> check {
          status shouldEqual StatusCodes.MethodNotAllowed
        }
      }
    }

  }



}
//#full-example
