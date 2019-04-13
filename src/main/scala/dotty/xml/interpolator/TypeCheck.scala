package dotty.xml.interpolator

import scala.quoted._
import scala.tasty._

import dotty.xml.interpolator.Tree._

object TypeCheck {

  def apply(nodes: Seq[Node], args: List[Expr[Any]])(implicit reflect: Reflection): Unit = {
    import reflect._
    nodes.foreach(node => node match {
      case elem : Elem =>
        elem.attributes.foreach(attribute => attribute.value match {
          case Seq(Placeholder(id)) =>
            val term = args(id).unseal
            val expected = attribute.isNamespace match {
              case true => Seq('[String].unseal.tpe)
              case _ => Seq('[String].unseal.tpe,'[Seq[scala.xml.Node]].unseal.tpe, '[Option[Seq[scala.xml.Node]]].unseal.tpe)
            }
            if (!expected.exists(term.tpe <:< _)) {
              error(s"""type mismatch;
                  | found   : ${term.tpe.widen.show}
                  | required: ${expected.map(_.show).mkString(" | ")}
                """.stripMargin, term.pos)
            }
          case _ =>
        })
      case _ =>
    })
  }
}