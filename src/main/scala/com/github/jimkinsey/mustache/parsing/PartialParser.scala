package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.Context
import com.github.jimkinsey.mustache.components.Partial

object PartialParser {
  case class TemplateNotFound(name: String)
}

class PartialParser(rendered: (String, Context) => Either[Any, String]) extends ComponentParser[Partial] {

  def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[Any, Option[ParseResult[Partial]]] = {
    Right(for {
      res <- s"\\{\\{>\\s*(.+?)\\s*\\}\\}".r.findPrefixMatchOf(template)
      name = res.group(1)
      remainder = res.after.toString
    } yield ParseResult(new Partial(name, rendered), remainder))
  }
}
