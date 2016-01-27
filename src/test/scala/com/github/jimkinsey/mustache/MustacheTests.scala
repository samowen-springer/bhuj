package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.CanContextualise.ContextualisationFailure
import com.github.jimkinsey.mustache.Mustache.TemplateNotFound
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MustacheTests extends FunSpec {

  implicit object MapCanContextualise$ extends CanContextualise[Map[String,Any]] {
    def context(map: Map[String,Any]) = Right(map)
  }

  describe("A Mustache renderer") {

    it("returns a TemplateNotFound failure when asked to render a template not known to it") {
      new Mustache(templates = Map.empty.get).renderTemplate("page", Map[String,Any]()) should be(Left(TemplateNotFound("page")))
    }

    it("returns the result of rendering the named template when it is available") {
      new Mustache(templates = Map("page" -> "A page!").get).renderTemplate("page", Map[String,Any]()) should be(Right("A page!"))
    }

    it("returns the failure if the contextualiser cannot produce a context") {
      implicit object IntCanContextualise$ extends CanContextualise[Int] {
        def context(i: Int) = Left(ContextualisationFailure("Ints cannot be maps"))
      }
      val mustache = new Mustache(templates = Map("x" -> "x={{x}}").get)
      mustache.renderTemplate("x", 42) should be(Left(ContextualisationFailure("Ints cannot be maps")))
    }

    it("uses the available evidence to produce a context for rendering") {
      case class Person(name: String)
      implicit object PersonCanContextualise$ extends CanContextualise[Person] {
        def context(person: Person) = Right(Map("name" -> person.name))
      }
      val mustache = new Mustache(templates = Map("greeting" -> "Hello {{name}}!").get)
      mustache.renderTemplate("greeting", Person("Charlotte")) should be(Right("Hello Charlotte!"))
    }

  }

}