package result

/** A Rust `Result<T, E>` inspired interface for handling results.
  *
  * [[Result]] is a type that represents either success ([[Ok]]) or failure ([[Err]]).
  * See the [[result package documentation]] for details.
  */
sealed trait Result[+T, +E] extends Any {

  /** Returns `true` if the result is [[Ok]].
    *
    * ==Examples==
    *
    * {{{
    * val x: Result[Int, String] = Ok(-3)
    * assert(x.isOk == true)
    *
    * val y: Result[Int, String] = Err("Some error message")
    * assert(y.isOk == false)
    * }}}
    */
  def isOk: Boolean

  /** Returns `true` if the result is [[Ok]] and the value inside of it matches a predicate.
    *
    * ==Examples==
    *
    * {{{
    * val x: Result[Int, String] = Ok(2)
    * assert(x.isOkAnd(_ > 1) == true)
    *
    * val y: Result[Int, String] = Ok(0)
    * assert(y.isOkAnd(_ > 1) == false)
    *
    * val z: Result[Int, String] = Err("hey")
    * assert(z.isOkAnd(_ > 1) == false)
    * }}}
    */
  def isOkAnd(f: T => Boolean): Boolean

  /** Returns `true` if the result is [[Err]].
    *
    * ==Examples==
    *
    * {{{
    * val x: Result[Int, String] = Ok(-3)
    * assert(x.isErr == false)
    *
    * val y: Result[Int, String] = Err("Some error message")
    * assert(y.isErr == true)
    * }}}
    */
  def isErr: Boolean

  /** Returns `true` if the result is [[Err]] and the value inside of it matches a predicate.
    *
    * ==Examples==
    *
    * {{{
    * val x: Result[String, Int] = Err(2)
    * assert(x.isErrAnd(_ > 1) == true)
    *
    * val y: Result[String, Int] = Err(0)
    * assert(y.isErrAnd(_ > 1) == false)
    *
    * val z: Result[String, Int] = Ok("Some success string")
    * assert(z.isErrAnd(_ > 1) == false)
    * }}}
    */
  def isErrAnd(f: E => Boolean): Boolean
}

/** Contains the success value */
case class Ok[+T, +E](v: T) extends AnyVal with Result[T, E] {
  override def isOk: Boolean = true

  override def isOkAnd(f: T => Boolean): Boolean = f(v)

  override def isErr: Boolean = false

  override def isErrAnd(f: E => Boolean): Boolean = false
}

/** Contains the error value */
case class Err[+T, +E](e: E) extends AnyVal with Result[T, E] {
  override def isOk: Boolean = false

  override def isOkAnd(f: T => Boolean): Boolean = false

  override def isErr: Boolean = true

  override def isErrAnd(f: E => Boolean): Boolean = f(e)
}
