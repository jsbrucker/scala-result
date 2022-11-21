package scala_result

/** Used to convert a `Result[E, T]` to a value of type `V`
  *
  * This interface is leveraged by the [[Result.to]] method.
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
  implicit def eitherFromResult[E, T]: FromResult[E, T, Either[E, T]] =
    _.toEither

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
  implicit def tryFromResult[T]: FromResult[Throwable, T, scala.util.Try[T]] =
    _.toTry
}
