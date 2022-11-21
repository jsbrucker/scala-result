package scala_result
package extensions

object option {

  /** Wrapper for `Option`
    *
    * ==Examples==
    *
    * {{{
    * >>> import scala_result._
    * >>> import scala_result.extensions.option._
    *
    * >>> Ops(Some(1)).toResult(2)
    * Ok(1)
    * }}}
    */
  implicit class Ops[V](self: Option[V]) {

    /** An alias of [[toOkOrElse]]
      *
      * ==Examples==
      *
      * {{{
      * >>> import scala_result._
      * >>> import scala_result.extensions.option._
      *
      * >>> None.toResult(1)
      * Err(1)
      *
      * >>> Some(2).toResult(1)
      * Ok(2)
      * }}}
      */
    def toResult[E](default: => E): Result[E, V] =
      toOkOrElse(default)

    /** Convert an `Option[E]` into a Result[E, T] using `default` for the `Ok`
      * value in the `None` case.
      *
      * ==Examples==
      *
      * {{{
      * >>> import scala_result._
      * >>> import scala_result.extensions.option._
      *
      * >>> None.toErrOrElse(1)
      * Ok(1)
      *
      * >>> Some(2).toErrOrElse(1)
      * Err(2)
      * }}}
      */
    def toErrOrElse[T](default: => T): Result[V, T] =
      self.map(Err(_)).getOrElse(Ok(default))

    /** Convert an `Option[T]` into a Result[E, T] using `default` for the `Err`
      * value in the `None` case.
      *
      * ==Examples==
      *
      * {{{
      * >>> import scala_result._
      * >>> import scala_result.extensions.option._
      *
      * >>> None.toOkOrElse(1)
      * Err(1)
      *
      * >>> Some(2).toOkOrElse(1)
      * Ok(2)
      * }}}
      */
    def toOkOrElse[E](default: => E): Result[E, V] =
      self.map(Ok(_)).getOrElse(Err(default))

    /** Convert an `Option[Result[E, T]]` into a `Result[E, Option[T]]` with the
      * `None` case being treated as an `Ok`
      *
      * ==Examples==
      *
      * {{{
      * >>> import scala_result._
      * >>> import scala_result.extensions.option._
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
      * >>> import scala_result._
      * >>> import scala_result.extensions.option._
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

  private[scala_result] trait Syntax {
    implicit class Ops[V](value: Option[V]) extends option.Ops[V](value)
  }
}
