package bhuj

import bhuj.context.{CanContextualise, ContextImplicits}
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar._

class MustacheTests extends FunSpec {
  import ContextImplicits.canContextualiseMap

  describe("A Mustache renderer") {

    it("returns a TemplateNotFound failure when asked to render a template not known to it") {
      new Mustache(templates = Map.empty.get).renderTemplate("page", Map[String,Any]()) should be(Left(TemplateNotFound("page")))
    }

    it("returns the result of rendering the named template when it is available") {
      new Mustache(templates = Map("page" -> "A page!").get).renderTemplate("page", Map[String,Any]()) should be(Right("A page!"))
    }

    it("returns the failure if the contextualiser cannot produce a context") {
      val failure = mock[CanContextualise.Failure]
      implicit object IntCanContextualise extends CanContextualise[Int] {
        def context(i: Int) = Left(failure)
      }
      val mustache = new Mustache(templates = Map("x" -> "x={{x}}").get)
      mustache.renderTemplate("x", 42) should be(Left(ContextualisationFailure(failure)))
    }

    it("uses the available evidence to produce a context for rendering") {
      case class Person(name: String)
      implicit object PersonCanContextualise extends CanContextualise[Person] {
        def context(person: Person) = Right(Map("name" -> person.name))
      }
      val mustache = new Mustache(templates = Map("greeting" -> "Hello {{name}}!").get)
      mustache.renderTemplate("greeting", Person("Charlotte")) should be(Right("Hello Charlotte!"))
    }

    it("can be constructed with a global context to pass to the Renderer") {
      val mustache = new Mustache(globalContext = Map("theAnswer" -> 42))
      mustache.render("The answer is {{theAnswer}}") should be(Right("The answer is 42"))
    }

    it("works for multiline templates") {
      val mustache = new Mustache()
      mustache.render(
        """{{a}}
          |{{b}}
          |""".stripMargin, Map("a" -> 1, "b" -> 2)) should be(Right(
        """1
          |2
          |""".stripMargin
      ))
    }

    it("fails with an unclosed tag failure when a section tag is not closed") {
      new Mustache().render("0123{{#sec}}unclosed") should be(Left(UnclosedTag("sec")))
    }

    it("fails with an invalid delimiters failure when invalid delimiters are used in a set delimiters tag") {
      new Mustache().render("{{== = =}}") should be(Left(InvalidDelimiters("=", "= ")))
    }

    it("does not fail to parse when a context contains a key the same as that of the context itself") {
      new Mustache().render(
        """{{#things}}
          |  {{{things}}}
          |{{/things}}
          |""".stripMargin) should not be a[ParseFailure]
    }

    it("allows access to the context item when rendering an Option using underscore (_)") {
      new Mustache().render("""{{#maybe}}~{{_}}~{{/maybe}}""", Map("maybe" -> Some("It is!!!"))) should be(Right("~It is!!!~"))
    }

  }

}
