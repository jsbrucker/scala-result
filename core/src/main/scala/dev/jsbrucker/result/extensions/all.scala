package dev.jsbrucker.result
package extensions

object all {
  implicit class Ops[V] private[all] (self: V) {

    /** Convert value to `Result`
      *
      * ==Examples==
      *
      * The `ToResult` type class is predefined for `Either` and `Try`
      * {{{
      * >>> import dev.jsbrucker.result._
      * >>> import dev.jsbrucker.result.extensions.all._
      *
      * >>> Right(1).toResult == Ok(1)
      * true
      *
      * >>> Left("Some Error").toResult == Err("Some Error")
      * true
      * }}}
      */
    def toResult[E, T](implicit toResult: ToResult[E, T, V]): Result[E, T] =
      toResult(self)

    /** Wrap arbitrary value in Ok
      *
      * ==Examples==
      *
      * {{{
      * >>> import dev.jsbrucker.result._
      * >>> import dev.jsbrucker.result.extensions.all._
      * >>> 1.asOk == Ok(1)
      * true
      * }}}
      */
    def asOk[E]: Ok[E, V] = Ok(self)

    /** Wrap arbitrary value in Err
      *
      * ==Examples==
      *
      * {{{
      * >>> import dev.jsbrucker.result._
      * >>> import dev.jsbrucker.result.extensions.all._
      * >>> "Error".asErr == Err("Error")
      * true
      * }}}
      */
    def asErr[T]: Err[V, T] = Err(self)
  }

  private[result] trait Syntax {
    implicit class Ops[V](value: V) extends all.Ops[V](value)
  }
}
