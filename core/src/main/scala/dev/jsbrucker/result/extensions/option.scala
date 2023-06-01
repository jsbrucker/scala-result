package dev.jsbrucker.result
package extensions

object option {

  /** Extension methods for `Option` */
  implicit class Ops[V](self: Option[V]) {

    /** Convert an `Option[E]` into a `Result[E, Unit]` using `Unit` for the
      * `Ok` value in the `None` case.
      *
      * ==Examples==
      *
      * {{{
      * >>> import dev.jsbrucker.result._
      * >>> import dev.jsbrucker.result.extensions.option._
      *
      * >>> None.toErr == Ok.unit
      * true
      *
      * >>> Some(2).toErr
      * Err(2)
      * }}}
      */
    def toErr: Result[V, Unit] = self match {
      case Some(e) => Err(e)
      case None    => Ok.unit
    }

    /** Convert an `Option[T]` into a `Result[Unit, T]` using `Unit` for the
      * `Err` value in the `None` case.
      *
      * ==Examples==
      *
      * {{{
      * >>> import dev.jsbrucker.result._
      * >>> import dev.jsbrucker.result.extensions.option._
      *
      * >>> None.toOk == Err.unit
      * true
      *
      * >>> Some(2).toOk
      * Ok(2)
      * }}}
      */
    def toOk: Result[Unit, V] = self match {
      case Some(v) => Ok(v)
      case None    => Err.unit
    }

    /** Convert an `Option[E]` into a `Result[E, T]` using `default` for the
      * `Ok` value in the `None` case.
      *
      * ==Examples==
      *
      * {{{
      * >>> import dev.jsbrucker.result._
      * >>> import dev.jsbrucker.result.extensions.option._
      *
      * >>> None.toErrOrElse(1)
      * Ok(1)
      *
      * >>> Some(2).toErrOrElse(1)
      * Err(2)
      * }}}
      */
    def toErrOrElse[T](default: => T): Result[V, T] = self match {
      case Some(e) => Err(e)
      case None    => Ok(default)
    }

    /** Convert an `Option[T]` into a `Result[E, T]` using `default` for the
      * `Err` value in the `None` case.
      *
      * ==Examples==
      *
      * {{{
      * >>> import dev.jsbrucker.result._
      * >>> import dev.jsbrucker.result.extensions.option._
      *
      * >>> None.toOkOrElse(1)
      * Err(1)
      *
      * >>> Some(2).toOkOrElse(1)
      * Ok(2)
      * }}}
      */
    def toOkOrElse[E](default: => E): Result[E, V] = self match {
      case Some(v) => Ok(v)
      case None    => Err(default)
    }

    /** Convert an `Option[Result[E, T]]` into a `Result[E, Option[T]]` with the
      * `None` case being treated as an `Ok`
      *
      * ==Examples==
      *
      * {{{
      * >>> import dev.jsbrucker.result._
      * >>> import dev.jsbrucker.result.extensions.option._
      *
      * >>> Some(Ok(1)).transposeOk
      * Ok(Some(1))
      *
      * >>> Some(Err(2)).transposeOk
      * Err(2)
      *
      * >>> None.transposeOk
      * Ok(None)
      * }}}
      */
    def transposeOk[T, E](implicit
        ev: V <:< Result[E, T]
    ): Result[E, Option[T]] = self match {
      case Some(result) => ev(result).map(Some(_))
      case None         => Ok(None)
    }

    /** Convert an `Option[Result[E, T]]` into a `Result[E, Option[T]]` with the
      * `None` case being treated as an `Err`
      *
      * ==Examples==
      *
      * {{{
      * >>> import dev.jsbrucker.result._
      * >>> import dev.jsbrucker.result.extensions.option._
      *
      * >>> Some(Err(1)).transposeErr
      * Err(Some(1))
      *
      * >>> Some(Ok(2)).transposeErr
      * Ok(2)
      *
      * >>> None.transposeErr
      * Err(None)
      * }}}
      */
    def transposeErr[T, E](implicit
        ev: V <:< Result[E, T]
    ): Result[Option[E], T] = self match {
      case Some(result) => ev(result).mapErr(Some(_))
      case None         => Err(None)
    }
  }

  private[result] trait Syntax {
    implicit class Ops[V](value: Option[V]) extends option.Ops[V](value)
  }
}
