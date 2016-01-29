package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.CanContextualiseCaseClass.NotACaseClass
import com.github.jimkinsey.mustache.tags.SectionStart.Lambda
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock

class CanContextualiseCaseClassTests extends FunSpec {
  private val converter = mock[CaseClassConverter]
  private val contextualiser = new CanContextualiseCaseClass(converter)
  import contextualiser._

  describe("CanContextualiseCaseClass") {

    it("delegates to the converter") {
      case class House(number: Int)
      when(converter.map(any())).thenReturn(Right(Map("hello" -> "world")))
      context(House(11)) should be(Right(Map("hello" -> "world")))
    }

    it("cannot contextualise a Product which is not a case class") {
      val notACaseClass = new Product {
        override def productElement(n: Int): Any = 42
        override def productArity: Int = 3
        override def canEqual(that: Any): Boolean = false
      }
      context(notACaseClass) should be(Left(NotACaseClass(notACaseClass)))
    }

    it("strips out the reference to the outer class when the case class is inner") {
      case class Inner(n: Int)
      context(Inner(7)).right.get.keySet should not contain "$outer"
    }

    it("can contextualise a case class with an Int field") {
      case class Numbered(n: Int)
      context(Numbered(42)) should be(Right(Map("n" -> 42)))
    }

    it("can contextualise a String field") {
      case class Named(name: String)
      context(Named("Charley")) should be(Right(Map("name" -> "Charley")))
    }

    it("can contextualise a Boolean field") {
      case class Flagged(awesome: Boolean)
      context(Flagged(awesome = true)) should be(Right(Map("awesome" -> true)))
    }

    it("can contextualise a Mustache-compatible lambda field") {
      case class Ram(lambda: Lambda)
      val lambda: Lambda = (str, render) => Right("Lambdad!")
      context(Ram(lambda)) should be(Right(Map("lambda" -> lambda)))
    }

    it("recursively contextualises a case class") {
      case class PostCode(areaCode: String, code: String)
      case class Address(postCode: PostCode)
      context(
        Address(
          PostCode("SE10", "8HR")
        )
      ) should be(Right(
        Map(
          "postCode" -> Map("areaCode" -> "SE10", "code" -> "8HR")
        )
      ))
    }

    it("recursively contextualises an iterable") {
      case class Thing(n: Int)
      case class Container(things: Seq[Thing])
      context(
        Container(Seq(Thing(1), Thing(2)))
      ) should be(Right(
        Map("things" -> Seq(Map("n" -> 1), Map("n" -> 2)))
      ))
    }

    it("recursively contextualises a map") {
      case class Navigator(map: Map[Int, Int])
      context(
        Navigator(Map(1 -> 2))
      ) should be(Right(Map("map" -> Map("1" -> 2))))
    }

  }


}
