package result

case class RichAny[A](self: A) extends AnyVal {

  /** Wrap arbitrary value in Ok
    * ==Examples==
    *
    * {{{
    * >>> import Extensions.toRichAny
    * >>> 1.asOk == Ok(1)
    * true
    * }}}
    */
  def asOk[E]: Ok[E, A] = Ok(self)

  /** Wrap arbitrary value in Err
    *
    * ==Examples==
    *
    * {{{
    * >>> import Extensions.toRichAny
    * >>> "Error".asErr == Err("Error")
    * true
    * }}}
    */
  def asErr[T]: Err[A, T] = Err(self)
}
