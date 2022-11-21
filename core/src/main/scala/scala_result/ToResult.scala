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
    * >>> Result(Right(1)) == Ok(1)
    * true
    *
    * >>> Result(Left("Error")) == Err("Error")
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
    * >>> Result(scala.util.Success(1)) == Ok(1)
    * true
    *
    * >>> val ex: Exception = new Exception("Error")
    * >>> Result(scala.util.Failure(ex)) == Err(ex)
    * true
    * }}}
    */
  implicit def tryToResult[T]: ToResult[Throwable, T, scala.util.Try[T]] = {
    case scala.util.Success(ok) => Ok(ok)
    case scala.util.Failure(e)  => Err(e)
  }

  /** Converts `Boolean` into `Result[Unit, Unit]`
    *
    *   - `true` is `Ok`
    *   - `false is `Err`
    *
    * ===Examples===
    *
    * {{{
    * >>> Result(true) == Ok.unit
    * true
    *
    * >>> Result(false) == Err.unit
    * true
    * }}}
    */
  implicit val booleanToResult: ToResult[Unit, Unit, Boolean] = {
    case true  => Ok.unit
    case false => Err.unit
  }
}
