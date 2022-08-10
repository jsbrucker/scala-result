package result

/** A Rust `Result<T, E>` inspired interface for handling results.
  *
  * [[Result]] is a type that represents either success ([[Ok]]) or failure ([[Err]]).
  * See the [[result package documentation]] for details.
  *
  * @groupname Query Querying the variant
  * @groupprio Query 0
  * @groupname Extract Extracting contained values
  * @groupprio Extract 1
  * @groupname Option Transform variant to Option
  * @groupprio Option 2
  * @groupname Transform Transforming contained values
  * @groupprio Transform 3
  * @groupname Cast Type-safe casts
  * @groupprio Cast 7
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
    *
    * @group Query
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
    *
    * @group Query
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
    *
    * @group Query
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
    *
    * @group Query
    */
  def isErrAnd(f: E => Boolean): Boolean

  /** Returns the contained [[Ok]] value.
    *
    * ==Throws==
    *
    * Throws if the value is an [[Err]], with a exception message combining the passed message and the [[Err]]'s value.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Err("emergency failure")
    * >>> intercept[RuntimeException](x.expect("Testing expect")).getMessage
    * Testing expect: emergency failure
    * }}}
    *
    * @group Extract
    */
  @throws(classOf[RuntimeException])
  def expect(msg: String): T = this match {
    case Ok(t)  => t
    case Err(e) => Result.unwrapFailed(msg, e)
  }

  /** Returns the contained [[Ok]] value.
    *
    * Because this function may panic, its use is generally discouraged. Instead, prefer to use pattern matching and
    * handle the [[Err]] case explicitly, or call [[unwrapOr]] or [[unwrapOrElse]].
    *
    * ==Throws==
    *
    * Throws if the value is an [[Err]], with a exception message provided by the [[Err]]'s value.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Ok(2)
    * >>> x.unwrap
    * 2
    *
    * >>> val y: Result[Int, String] = Err("emergency failure")
    * >>> intercept[RuntimeException](y.unwrap).getMessage
    * called `Result::unwrap` on an `Err` value: emergency failure
    * }}}
    *
    * @group Extract
    */
  @throws(classOf[RuntimeException])
  def unwrap: T = this match {
    case Ok(t) => t
    case Err(e) =>
      Result.unwrapFailed("called `Result::unwrap` on an `Err` value", e)
  }

  /** Returns the contained [[Ok]] value or a provided default.
    *
    * Arguments passed to [[Result.unwrapOr unwrapOr]] are eagerly evaluated; if you are passing the result of a
    * function call, it is recommended to use [[Result.unwrapOrElse unwrapOrElse]], which is lazily evaluated.
    *
    * ==Examples==
    *
    * {{{
    * >>> val default = 2
    *
    * >>> val x: Result[Int, String] = Ok(9)
    * >>> x.unwrapOr(default)
    * 9
    *
    * >>> val y: Result[Int, String] = Err("error")
    * >>> y.unwrapOr(default)
    * 2
    * }}}
    *
    * @group Extract
    */
  def unwrapOr[U >: T](default: U): U = this match {
    case Ok(t)  => t
    case Err(_) => default
  }

  /** Returns the contained [[Ok]] value or computes it from a provided function applied to the [[Err]] value.
    *
    * ==Examples==
    *
    * {{{
    * >>> Ok[Int, String](2).unwrapOrElse(_.size)
    * 2
    *
    * >>> Err("foo").unwrapOrElse(_.size)
    * 3
    * }}}
    *
    * @group Extract
    */
  def unwrapOrElse[U >: T](op: E => U): U = this match {
    case Ok(t)  => t
    case Err(e) => op(e)
  }

  /** Returns the contained [[Err]] value.
    *
    * ==Throws==
    *
    * Throws if the value is an [[Ok]], with a exception message combining the passed message and the [[Ok]]'s value.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Ok("unexpected success")
    * >>> intercept[RuntimeException](x.expectErr("Testing expect")).getMessage
    * Testing expect: unexpected success
    * }}}
    *
    * @group Extract
    */
  def expectErr(msg: String): E = this match {
    case Ok(t)  => Result.unwrapFailed(msg, t)
    case Err(e) => e
  }

  /** Returns the contained [[Err]] value.
    *
    * Because this function may panic, its use is generally discouraged. Instead, prefer to use pattern matching and
    * handle the [[Ok]] case explicitly.
    *
    * ==Throws==
    *
    * Throws if the value is an [[Ok]], with a exception message provided by the [[Ok]]'s value.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Err(2)
    * >>> x.unwrapErr
    * 2
    *
    * >>> val y: Result[String, Int] = Ok("unexpected success")
    * >>> intercept[RuntimeException](y.unwrapErr).getMessage
    * called `Result::unwrapErr` on an `Ok` value: unexpected success
    * }}}
    *
    * @group Extract
    */
  @throws(classOf[RuntimeException])
  def unwrapErr: E = this match {
    case Ok(t) =>
      Result.unwrapFailed("called `Result::unwrapErr` on an `Ok` value", t)
    case Err(e) => e
  }

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
    *
    * @group Option
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
    *
    * @group Option
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
    *
    * @group Option
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

  /** Maps a [[Result]]`[T, E]` to [[Result]]`[U, E]` by applying a function to a contained [[Ok]] value, leaving an
    * [[Err]] value untouched.
    *
    * This function can be used to compose the results of two functions.
    *
    * ==Examples==
    *
    * {{{
    * >>> def toInt(c: Char) = if (c.isDigit) Ok(c.asDigit) else Err("Not a digit")
    * >>> def square(i: Int) = i * i
    *
    * >>> toInt('1').map(square(_))
    * Ok(1)
    *
    * >>> toInt('2').map(square(_))
    * Ok(4)
    *
    * >>> toInt('A').map(square(_))
    * Err(Not a digit)
    * }}}
    *
    * @group Transform
    */
  def map[U](op: T => U): Result[U, E] = this match {
    case Ok(t)  => Ok(op(t))
    case Err(e) => Err(e)
  }

  /** Returns the provided default (if [[Err]]), or applies a function to the contained value (if [[Ok]]),
    *
    * Arguments passed to `mapOr` are eagerly evaluated; if you are passing the result of a function call, it is
    * recommended to use [[mapOrElse]], which is lazily evaluated.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, String] = Ok("foo")
    * >>> x.mapOr(42, _.size)
    * 3
    *
    * >>> val y: Result[String, String] = Err("bar")
    * >>> y.mapOr(42, _.size)
    * 42
    * }}}
    *
    * @group Transform
    */
  def mapOr[U](default: U, f: T => U): U = this match {
    case Ok(t)  => f(t)
    case Err(_) => default
  }

  /** Maps a [[Result]]`[T, E]` to `U` by applying fallback function default to a contained [[Err]] value, or function
    * `f` to a contained [[Ok]] value.
    *
    * This function can be used to unpack a successful result
    * while handling an error.
    *
    * ==Examples==
    *
    * {{{
    * >>> val k = 21
    *
    * >>> val x: Result[String, String] = Ok("foo")
    * >>> x.mapOrElse(_ => k * 2, _.size)
    * 3
    *
    * >>> val y: Result[String, String] = Err("bar")
    * >>> y.mapOrElse(_ => k * 2, _.size)
    * 42
    * }}}
    *
    * @group Transform
    */
  def mapOrElse[U](default: E => U, f: T => U): U = this match {
    case Ok(t)  => f(t)
    case Err(e) => default(e)
  }

  /** Maps a [[Result]]`[T, E]` to [[Result]]`[T, F]` by applying a function to a contained [[Err]] value, leaving an
    * [[Ok]] value untouched.
    *
    * This function can be used to pass through a successful result while handling an error.
    *
    * ==Examples==
    *
    * {{{
    * >>> def square(i: Int) = i * i
    *
    * >>> Err(1).mapErr(square(_))
    * Err(1)
    *
    * >>> Err(2).mapErr(square(_))
    * Err(4)
    *
    * >>> Ok[String, Int]("Some Value").mapErr(square(_))
    * Ok(Some Value)
    * }}}
    *
    * @group Transform
    */
  def mapErr[F](op: E => F): Result[T, F] = this match {
    case Ok(t)  => Ok(t)
    case Err(e) => Err(op(e))
  }

  /** Upcasts this `Result[T, E]` to `Result[U, E]`
    *
    * Normally used when constructing an [[Err]]
    *
    * {{{
    * scala> Err(1)
    * res0: Err[Nothing, Int] = Err(1)
    *
    * scala> Err(2).withOk[String]
    * res1: Result[String, Int] = Err(2)
    * }}}
    *
    * @group Cast
    */
  def withOk[U >: T]: Result[U, E] = this

  /** Upcasts this `Result[T, E]` to `Result[T, F]`
    *
    * Normally used when constructing an [[Ok]]
    *
    * {{{
    * scala> Ok(1)
    * res0: Ok[Int, Nothing] = Ok(1)
    *
    * scala> Ok(2).withErr[String]
    * res1: Result[Int, String] = Ok(2)
    * }}}
    *
    * @group Cast
    */
  def withErr[F >: E]: Result[T, F] = this
}

object Result {
  @throws(classOf[RuntimeException])
  private final def unwrapFailed[X, Y](msg: String, value: Y): X =
    throw new RuntimeException(s"$msg: $value")
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
