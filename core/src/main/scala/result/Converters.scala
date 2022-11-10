package result

import scala.language.implicitConversions

/** Used to convert a value of type `V` to a `Result[E, T]`
  *
  * This interface is leveraged by the [[Result.apply]] method and
  * [[ImplicitConversions.toResult]].
  */
trait ToResult[+E, +T, -V] {
  def apply(value: V): Result[E, T]
}

object ToResult {
  import Extensions.{toRichTry, toRichEither}

  /** Converts `Either[E, T]` into `Result[E, T]`
    *
    * ===Examples===
    *
    * {{{
    * >>> val right: Either[String, Int] = Right(1)
    * >>> Result(right) == Ok(1)
    * true
    *
    * >>> val left: Either[String, Int] = Left("Error")
    * >>> Result(left) == Err("Error")
    * true
    * }}}
    */
  implicit def fromEither[E, T]: ToResult[E, T, Either[E, T]] =
    _.toResult

  /** Converts `Try[T]` into `Result[Throwable, T]`
    *
    * ===Examples===
    *
    * {{{
    * >>> val success: scala.util.Try[Int] = scala.util.Success(1)
    * >>> Result(success) == Ok(1)
    * true
    *
    * >>> val ex: Exception = new Exception("Error")
    * >>> val failure: scala.util.Try[Int] = scala.util.Failure(ex)
    * >>> Result(failure) == Err(ex)
    * true
    * }}}
    */
  implicit def fromTry[T]: ToResult[Throwable, T, scala.util.Try[T]] =
    _.toResult
}

/** Used to convert a `Result[E, T]` to a value of type `V`
  *
  * This interface is leveraged by the [[Result.to]] method and
  * [[ImplicitConversions.fromResult]].
  */
trait FromResult[-E, -T, +V] {
  def apply(result: Result[E, T]): V
}

object FromResult {

  /** Converts `Result[E, T]` into `Either[E, T]`
    *
    * ===Examples===
    *
    * {{{
    * >>> val ok = Ok(1)
    * >>> ok.to[Either[String, Int]] == Right(1)
    * true
    *
    * >>> val err = Err("Error")
    * >>> err.to[Either[String, Int]] == Left("Error")
    * true
    * }}}
    */
  implicit def toEither[E, T]: FromResult[E, T, Either[E, T]] = _.toEither

  /** Converts `Result[Throwable, T]` into `Try[T]`
    *
    * ===Examples===
    *
    * {{{
    * >>> val ok = Ok(1)
    * >>> ok.to[scala.util.Try[Int]] == scala.util.Success(1)
    * true
    *
    * >>> val ex: Exception = new Exception("Error")
    * >>> val err = Err(ex)
    * >>> err.to[scala.util.Try[Int]] == scala.util.Failure(ex)
    * true
    * }}}
    */
  implicit def toTry[T]: FromResult[Throwable, T, scala.util.Try[T]] = _.toTry
}
