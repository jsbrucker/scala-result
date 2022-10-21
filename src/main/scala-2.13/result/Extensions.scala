package result

import scala.language.implicitConversions

/** Implicit conversions used to add extension methods
  *
  * Implemented this way instead of leveraging implicit classes to minimize
  * code duplication otherwise required by exposing different APIs for for
  * different Scala versions.
  */
object Extensions {
  implicit def toRichAny[A](self: A): RichAny[A] = RichAny(self)

  implicit def toRichOption[O](self: Option[O]): RichOption[O] =
    RichOption(self)

  implicit def toRichEither[L, R](self: Either[L, R]): RichEither[L, R] =
    RichEither(self)

  implicit def toRichTry[T](self: scala.util.Try[T]): RichTry[T] = RichTry(self)

  implicit def toRichIterableOps[E, T, CC[_], C](
      self: scala.collection.IterableOps[Result[E, T], CC, C]
  ): RichIterableOps[E, T, CC, C] = RichIterableOps(self)
}
