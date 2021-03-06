package bhuj.context

import bhuj.context.CaseClassConverter.GeneralFailure
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock

class CanContextualiseMapTests extends FunSpec {
  import canContextualiseMap.context

  describe("Contextualising a map") {

    it("returns an empty map if the map is empty") {
      context(Map[String,Any]()) should be(Right(Map.empty))
    }

    it("returns the map if non-empty") {
      context(Map("x" -> 42)) should be(Right(Map("x" -> 42)))
    }

    it("contextualises any case classes it finds") {
      case class Player(lives: Int)
      when(caseClassConverter.map(any[Player])).thenReturn(Right(Map("a" -> 1)))
      context(Map("player" -> Player(3))) should be(Right(Map("player" -> Map("a" -> 1))))
    }

    it("returns the original value when it fails to contextualise a case class") {
      case class Uncontextualisable(x: Int)
      when(caseClassConverter.map(any[Uncontextualisable])).thenReturn(Left(GeneralFailure("#fail")))
      context(Map("thing" -> Uncontextualisable(42))) should be(Right(Map("thing" -> Uncontextualisable(42))))
    }

    it("recursively contextualises nested maps") {
      case class Name(first: String)
      val map = Map("lead" -> Map("name" -> Name("Jim")))
      when(caseClassConverter.map(any[Name])).thenReturn(Right(Map("first" -> "Jim")))
      context(map) should be(Right(Map("lead" -> Map("name" -> Map("first" -> "Jim")))))
    }

    it("recursively contextualises case classes in lists") {
      case class Wall(height: Int)
      when(caseClassConverter.map(any[Wall])).thenReturn(Right(Map("height" -> 40)))
      context(Map("walls" -> List(Wall(40)))) should be(Right(Map("walls" -> List(Map("height" -> 40)))))
    }

    it("recursively contextualises maps in lists") {
      case class Wall(height: Int)
      when(caseClassConverter.map(any[Wall])).thenReturn(Right(Map("height" -> 40)))
      context(Map(
        "walls" -> List(
          Map("east" -> Wall(40)),
          Map("west" -> Wall(40)))
      )) should be(Right(Map(
        "walls" -> List(
          Map("east" -> Map("height" -> 40)),
          Map("west" -> Map("height" -> 40))))
      ))
    }

    it("recursively contextualises lists in lists") {
      case class Brick(length: Int)
      when(caseClassConverter.map(any[Brick])).thenReturn(Right(Map("length" -> 2)))
      context(Map(
        "wall" -> List(List(Brick(2)))
      )) should be(Right(Map(
        "wall" -> List(List(Map("length" -> 2)))
      )))
    }

    it("contextualises a case class in an option") {
      case class House(name: String)
      when(caseClassConverter.map(any[House])).thenReturn(Right(Map("name" -> "dunromin")))
      context(Map(
        "house" -> Some(House("dunromin"))
      )) should be(Right(Map(
        "house" -> Some(Map("name" -> "dunromin"))
      )))
    }
  }

  private val caseClassConverter = mock[CaseClassConverter]
  private val canContextualiseMap = new CanContextualiseMap(caseClassConverter)

}
