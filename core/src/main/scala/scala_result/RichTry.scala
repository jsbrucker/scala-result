package scala_result

/** Wrapper for `Try`
  *
  * ==Examples==
  *
  * {{{
  * >>> import Extensions.toRichTry
  *
  * >>> scala.util.Success(1).toResult
  * Ok(1)
  *
  * >>> case object TestException extends Throwable
  * >>> val result = scala.util.Failure(TestException).toResult
  * >>> result.containsErr(TestException)
  * true
  * }}}
  */
case class RichTry[T](self: scala.util.Try[T]) extends AnyVal {
  def toResult: Result[Throwable, T] = self match {
    case scala.util.Success(ok) => Ok(ok)
    case scala.util.Failure(e)  => Err(e)
  }
}
