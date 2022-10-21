package result

/** Wrapper for `Either`
  *
  * ==Examples==
  *
  * {{{
  * >>> RichEither(Right(1)).toResult
  * Ok(1)
  * }}}
  */
case class RichEither[L, R](self: Either[L, R]) extends AnyVal {

  /** Convert an `Either[L, R]` to a [[Result]]`[L, R]`.
    *
    * This is useful for the cases where usage of `Either` does not follow the convention that the `Right` value is
    * used to represent an `Ok`.
    *
    * ==Examples==
    *
    * {{{
    * >>> import Extensions.toRichEither
    *
    * >>> Right(1).toResult
    * Ok(1)
    *
    * >>> Left(2).toResult
    * Err(2)
    * }}}
    */
  def toResult: Result[L, R] = self match {
    case Right(ok) => Ok(ok)
    case Left(e)   => Err(e)
  }

  /** Convert an `Either[L, R]` to a [[Result]]`[R, L]`.
    *
    * This is useful for the cases where usage of `Either` does not follow the convention that the `Right` value is
    * used to represent an `Ok`.
    *
    * ==Examples==
    *
    * {{{
    * >>> import Extensions.toRichEither
    *
    * >>> Right(1).toSwappedResult
    * Err(1)
    *
    * >>> Left(2).toSwappedResult
    * Ok(2)
    * }}}
    */
  def toSwappedResult: Result[R, L] = self match {
    case Left(ok) => Ok(ok)
    case Right(e) => Err(e)
  }
}
