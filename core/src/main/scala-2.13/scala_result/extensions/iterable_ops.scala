package scala_result
package extensions

object iterable_ops {

  /** Wrapper for iterable collections of [[Result]]`[E, T]` */
  implicit class Ops[E, T, CC[_], C](
      self: scala.collection.IterableOps[Result[E, T], CC, C]
  ) {

    /** A pair of, first, all [[Ok]] elements and, second, all [[Err]] elements.
      *
      * Has the same limitations as `partition` on the given collection.
      *
      * ==Examples==
      *
      * {{{
      * >>> import scala_result._
      * >>> import scala_result.extensions.iterable_ops._
      *
      * >>> val (oks, errs) = Vector(Ok(1), Err("A"), Ok(2), Ok(3), Err("B")).partitionResult
      *
      * >>> oks == Vector(1, 2, 3)
      * true
      *
      * >>> errs == Vector("A", "B")
      * true
      * }}}
      */
    def partitionResult[DD[_], D](implicit
        ev: C <:< scala.collection.IterableOps[Result[E, T], DD, D]
    ): (DD[T], DD[E]) = {
      val (oks, errs) = self.partition(_.isOk)
      (ev(oks).map(_.unwrap), ev(errs).map(_.unwrapErr))
    }
  }

  private[scala_result] trait Syntax {
    implicit class iterableOpsSyntax[E, T, CC[_], C](
        value: scala.collection.IterableOps[Result[E, T], CC, C]
    ) extends iterable_ops.Ops(value)
  }

}
