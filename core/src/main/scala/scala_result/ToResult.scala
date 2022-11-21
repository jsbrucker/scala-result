package scala_result

/** Used to convert a value of type `V` to a `Result[E, T]`
  *
  * This interface is leveraged by the [[Result.apply]] method and
  * [[extensions.all.Ops.toResult]].
  */
trait ToResult[+E, +T, -V] {
  def apply(value: V): Result[E, T]
}

object ToResult {

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
  implicit def eitherToResult[E, T]: ToResult[E, T, Either[E, T]] = {
    case Right(ok) => Ok(ok)
    case Left(e)   => Err(e)
  }

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
  implicit def tryToResult[T]: ToResult[Throwable, T, scala.util.Try[T]] = {
    case scala.util.Success(ok) => Ok(ok)
    case scala.util.Failure(e)  => Err(e)
  }
}
