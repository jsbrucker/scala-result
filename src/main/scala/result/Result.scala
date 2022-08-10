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
  * @groupname Boolean Boolean operators
  * @groupprio Boolean 4
  * @groupname Cast Type-safe casts
  * @groupprio Cast 7
  * @groupname Misc Miscellaneous methods
  * @groupprio Misc 8
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

  /** Returns `true` if the result is an [[Ok]] value containing the given value.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Ok(2)
    * >>> x.contains(2)
    * true
    *
    * >>> val y: Result[Int, String] = Ok(3)
    * >>> y.contains(2)
    * false
    *
    * >>> val z: Result[Int, String] = Err("Some error message")
    * >>> z.contains(2)
    * false
    * }}}
    *
    * @group Query
    */
  def contains[U >: T](x: U): Boolean = this match {
    case Err(_) => false
    case Ok(v)  => v == x
  }

  /** Returns `true` if the result is an [[Err]] value containing the given value.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Ok(2)
    * >>> x.containsErr("Some error message")
    * false
    *
    * >>> val y: Result[Int, String] = Err("Some error message")
    * >>> y.containsErr("Some error message")
    * true
    *
    * >>> val z: Result[Int, String] = Err("Some other error message")
    * >>> z.containsErr("Some error message")
    * false
    * }}}
    *
    * @group Query
    */
  def containsErr[U >: E](x: U): Boolean = this match {
    case Err(e) => x == e
    case Ok(_)  => false
  }

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

  /** Returns `rhs` if the result is [[Ok]], otherwise returns this [[Err]] value.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x1: Result[Int, String] = Ok(2)
    * >>> val y1: Result[String, String] = Err("late error")
    * >>> x1.and(y1)
    * Err(late error)
    *
    * >>> val x2: Result[Int, String] = Err("early error")
    * >>> val y2: Result[String, String] = Ok("foo")
    * >>> x2.and(y2)
    * Err(early error)
    *
    * >>> val x3: Result[Int, String] = Err("not a 2")
    * >>> val y3: Result[String, String] = Err("late error")
    * >>> x3.and(y3)
    * Err(not a 2)
    *
    * >>> val x4: Result[Int, String] = Ok(2)
    * >>> val y4: Result[String, String] = Ok("different result type")
    * >>> x4.and(y4)
    * Ok(different result type)
    * }}}
    *
    * @group Boolean
    */
  def and[U >: T, F >: E](rhs: Result[U, F]): Result[U, F] = this match {
    case Ok(_)  => rhs
    case Err(e) => Err(e)
  }

  /** Calls `op` if the [[Result]] is [[Ok]], otherwise returns this [[Err]] value.
    *
    * This function can be used for control flow based on `Result` values. Often used to chain fallible operations that
    * may return [`Err`].
    *
    * ==Examples==
    *
    * {{{
    * >>> def ensureEven(x: Int): Result[Int, String] = if (x % 2 == 0) Ok(x) else Err("Odd Number")
    * >>> def ensurePositive(x: Int): Result[Int, String] = if (x > 0) Ok(x) else Err("Not Positive")
    *
    * >>> Ok(2).andThen(ensureEven).andThen(ensurePositive)
    * Ok(2)
    *
    * >>> Ok(1).andThen(ensureEven).andThen(ensurePositive)
    * Err(Odd Number)
    *
    * >>> Ok(-2).andThen(ensureEven).andThen(ensurePositive)
    * Err(Not Positive)
    *
    * >>> Err("Some Error").andThen(ensureEven).andThen(ensurePositive)
    * Err(Some Error)
    * }}}
    *
    * @group Boolean
    */
  def andThen[U, F >: E](op: T => Result[U, F]): Result[U, F] = this match {
    case Ok(t)  => op(t)
    case Err(e) => Err(e)
  }

  /** An alias of [[andThen]] for compatibility with for-comprehensions
    *
    * @group Misc
    */
  def flatMap[U, F >: E](op: T => Result[U, F]): Result[U, F] = andThen(op)

  /** Returns `rhs` if the [[Result]] is [[Err]], otherwise returns the this [[Ok]] value.
    *
    * Arguments passed to `or` are eagerly evaluated; if you are passing the result of a function call, it is
    * recommended to use [[Result.orElse orElse]], which is lazily evaluated.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x1: Result[Int, String] = Ok(2)
    * >>> val y1: Result[Int, String] = Err("late error")
    * >>> x1.or(y1)
    * Ok(2)
    *
    * >>> val x2: Result[Int, String] = Err("early error")
    * >>> val y2: Result[Int, String] = Ok(2)
    * >>> x2.or(y2)
    * Ok(2)
    *
    * >>> val x3: Result[Int, String] = Err("not a 2")
    * >>> val y3: Result[Int, String] = Err("late error")
    * >>> x3.or(y3)
    * Err(late error)
    *
    * >>> val x4: Result[Int, String] = Ok(2)
    * >>> val y4: Result[Int, String] = Ok(100)
    * >>> x4.or(y4)
    * Ok(2)
    * }}}
    *
    * @group Boolean
    */
  def or[U >: T, F >: E](rhs: Result[U, F]): Result[U, F] = this match {
    case Err(_) => rhs
    case _      => this
  }

  /** Calls `op` if the result is [[Err]], otherwise returns this [[Ok]] value.
    *
    * This function can be used for control flow based on result values.
    *
    * ===Examples===
    *
    * {{{
    * >>> def sq(x: Int): Result[Int, Int] = { Ok(x * x) }
    * >>> def err(x: Int): Result[Int, Int] = { Err(x) }
    *
    * >>> Ok(2).orElse(sq).orElse(sq)
    * Ok(2)
    *
    * >>> Ok(2).orElse(err).orElse(sq)
    * Ok(2)
    *
    * >>> Err(3).orElse(sq).orElse(err)
    * Ok(9)
    *
    * >>> Err(3).orElse(err).orElse(err)
    * Err(3)
    * }}}
    *
    * @group Boolean
    */
  def orElse[U >: T, F](op: E => Result[U, F]): Result[U, F] = this match {
    case Ok(t)  => Ok(t)
    case Err(e) => op(e)
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
