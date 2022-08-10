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
    * >>> val x: Result[Int, String] = Ok(-3)
    * >>> x.isOk
    * true
    *
    * >>> val y: Result[Int, String] = Err("Some error message")
    * >>> y.isOk
    * false
    * }}}
    */
  def isOk: Boolean

  /** Returns `true` if the result is [[Ok]] and the value inside of it matches a predicate.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Ok(2)
    * >>> x.isOkAnd(_ > 1)
    * true
    *
    * >>> val y: Result[Int, String] = Ok(0)
    * >>> y.isOkAnd(_ > 1)
    * false
    *
    * >>> val z: Result[Int, String] = Err("hey")
    * >>> z.isOkAnd(_ > 1)
    * false
    * }}}
    */
  def isOkAnd(f: T => Boolean): Boolean

  /** Returns `true` if the result is [[Err]].
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Ok(-3)
    * >>> x.isErr
    * false
    *
    * >>> val y: Result[Int, String] = Err("Some error message")
    * >>> y.isErr
    * true
    * }}}
    */
  def isErr: Boolean

  /** Returns `true` if the result is [[Err]] and the value inside of it matches a predicate.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Err(2)
    * >>> x.isErrAnd(_ > 1)
    * true
    *
    * >>> val y: Result[String, Int] = Err(0)
    * >>> y.isErrAnd(_ > 1)
    * false
    *
    * >>> val z: Result[String, Int] = Ok("Some success string")
    * >>> z.isErrAnd(_ > 1)
    * false
    * }}}
    */
  def isErrAnd(f: E => Boolean): Boolean

  /** Converts from [[Result]]`[T, E]` to `Option[T]`.
    *
    * Converts `this` into an `Option[T]`, discarding the error, if any.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Ok(2)
    * >>> x.ok == Some(2)
    * true
    *
    * >>> val y: Result[Int, String] = Err("Nothing here")
    * >>> y.ok == None
    * true
    * }}}
    */
  final def ok: Option[T] = this match {
    case Ok(v) => Some(v)
    case _     => None
  }

  /** Converts from [[Result]]`[T, E]` to `Option[E]`.
    *
    * Converts `this` into an `Option[E]`, discarding the success value, if any.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Ok(2)
    * >>> x.err == None
    * true
    *
    * >>> val y: Result[Int, String] = Err("Nothing here")
    * >>> y.err == Some("Nothing here")
    * true
    * }}}
    */
  final def err: Option[E] = this match {
    case Err(e) => Some(e)
    case _      => None
  }

  /** Transposes a [[Result]] of an `Option` into an `Option` of a [[Result]].
    *
    * `Ok(None)` will be mapped to `None`.
    * `Ok(Some(_))` and `Err(_)` will be mapped to `Some(Ok(_))` and `Some(Err(_))`.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x1: Result[Option[Int], String] = Ok(Some(5))
    * >>> val x2: Option[Result[Int, String]] = Some(Ok(5))
    * >>> x1.transpose == x2
    * true
    *
    * >> val y1: Result[Option[Int], String] = Ok(None)
    * >> val y2: Option[Result[Int, String]] = None
    * >> y1.transpose == y2
    * true
    *
    * >>> val z1: Result[Option[Int], String] = Err("Some Error")
    * >>> val z2: Option[Result[Int, String]] = Some(Err("Some Error"))
    * >>> z1.transpose == z2
    * true
    * }}}
    */
  final def transpose[U](implicit ev: T <:< Option[U]): Option[Result[U, E]] =
    this match {
      case Ok(option) =>
        ev(option) match {
          case Some(x) => Some(Ok(x))
          case None    => None
        }
      case Err(e) => Some(Err(e))
    }
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
