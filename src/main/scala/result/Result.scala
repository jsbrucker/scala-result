package result

import scala.annotation.implicitNotFound

/** A Rust `Result<T, E>` inspired interface for handling results.
  *
  * [[Result]] is a type that represents either success ([[Ok]]) or failure ([[Err]]).
  * See the [[result package documentation]] for details.
  *
  * @groupname Query Querying the variant
  * @groupprio Query 0
  * @groupname Extract Extracting contained values
  * @groupprio Extract 1
  * @groupname Collection Transforming variant to other collection types
  * @groupprio Collection 2
  * @groupname Transform Transforming contained values
  * @groupprio Transform 3
  * @groupname Boolean Boolean operators
  * @groupprio Boolean 4
  * @groupname Cast Type-safe casts
  * @groupprio Cast 7
  * @groupname Misc Miscellaneous methods
  * @groupprio Misc 8
  */
sealed trait Result[+E, +T] extends Any {

  /** Returns `true` if the result is [[Ok]].
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Ok(-3)
    * >>> x.isOk
    * true
    *
    * >>> val y: Result[String, Int] = Err("Some error message")
    * >>> y.isOk
    * false
    * }}}
    *
    * @group Query
    */
  def isOk: Boolean = this match {
    case Ok(_)  => true
    case Err(_) => false
  }

  /** Returns `true` if the result is [[Ok]] and the value inside of it matches a predicate.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Ok(2)
    * >>> x.isOkAnd(_ > 1)
    * true
    *
    * >>> val y: Result[String, Int] = Ok(0)
    * >>> y.isOkAnd(_ > 1)
    * false
    *
    * >>> val z: Result[String, Int] = Err("hey")
    * >>> z.isOkAnd(_ > 1)
    * false
    * }}}
    *
    * @group Query
    */
  def isOkAnd(f: T => Boolean): Boolean = this match {
    case Ok(v)  => f(v)
    case Err(_) => false
  }

  /** Returns `true` if the result is [[Ok]] or the error value matches a predicate.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Ok(2)
    * >>> x.isOkOr(_ == "Foo")
    * true
    *
    * >>> val y: Result[String, Int] = Err("Foo")
    * >>> y.isOkOr(_ == "Foo")
    * true
    *
    * >>> val z: Result[String, Int] = Err("Bar")
    * >>> z.isOkOr(_ == "Foo")
    * false
    * }}}
    *
    * @group Query
    */
  def isOkOr(f: E => Boolean): Boolean = this match {
    case Ok(_)  => true
    case Err(e) => f(e)
  }

  /** Returns `true` if the result is [[Err]].
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Ok(-3)
    * >>> x.isErr
    * false
    *
    * >>> val y: Result[String, Int] = Err("Some error message")
    * >>> y.isErr
    * true
    * }}}
    *
    * @group Query
    */
  def isErr: Boolean = this match {
    case Ok(_)  => false
    case Err(_) => true
  }

  /** Returns `true` if the result is [[Err]] and the value inside of it matches a predicate.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Err(2)
    * >>> x.isErrAnd(_ > 1)
    * true
    *
    * >>> val y: Result[Int, String] = Err(0)
    * >>> y.isErrAnd(_ > 1)
    * false
    *
    * >>> val z: Result[Int, String] = Ok("Some success string")
    * >>> z.isErrAnd(_ > 1)
    * false
    * }}}
    *
    * @group Query
    */
  def isErrAnd(f: E => Boolean): Boolean = this match {
    case Ok(_)  => false
    case Err(e) => f(e)
  }

  /** Returns `true` if the result is [[Err]] or the okay value matches a predicate.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, String] = Err(2)
    * >>> x.isErrOr(_ == "Foo")
    * true
    *
    * >>> val y: Result[Int, String] = Ok("Foo")
    * >>> y.isErrOr(_ == "Foo")
    * true
    *
    * >>> val z: Result[Int, String] = Ok("Bar")
    * >>> z.isErrOr(_ == "Foo")
    * false
    * }}}
    *
    * @group Query
    */
  def isErrOr(f: T => Boolean): Boolean = this match {
    case Ok(v)  => f(v)
    case Err(_) => true
  }

  /** Returns `true` if the result is an [[Ok]] value containing the given value.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Ok(2)
    * >>> x.contains(2)
    * true
    *
    * >>> val y: Result[String, Int] = Ok(3)
    * >>> y.contains(2)
    * false
    *
    * >>> val z: Result[String, Int] = Err("Some error message")
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
    * >>> val x: Result[String, Int] = Ok(2)
    * >>> x.containsErr("Some error message")
    * false
    *
    * >>> val y: Result[String, Int] = Err("Some error message")
    * >>> y.containsErr("Some error message")
    * true
    *
    * >>> val z: Result[String, Int] = Err("Some other error message")
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
    * >>> val x: Result[String, Int] = Err("emergency failure")
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
    * >>> val x: Result[String, Int] = Ok(2)
    * >>> x.unwrap
    * 2
    *
    * >>> val y: Result[String, Int] = Err("emergency failure")
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
    * >>> val x: Result[String, Int] = Ok(9)
    * >>> x.unwrapOr(default)
    * 9
    *
    * >>> val y: Result[String, Int] = Err("error")
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
    * >>> Ok[String, Int](2).unwrapOrElse(_.size)
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
    * >>> val x: Result[Int, String] = Ok("unexpected success")
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
    * >>> val x: Result[Int, String] = Err(2)
    * >>> x.unwrapErr
    * 2
    *
    * >>> val y: Result[Int, String] = Ok("unexpected success")
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

  /** Returns the contained [[Ok]] value if an [[Ok]], and the contained [[Err]] value if an [[Err]]
    *
    * In other words, this function returns the value (the `R`) of a [[Result]]`[R, R]`, regardless of whether or not
    * that result is [[Ok]] or [[Err]]. This can be useful in rare cases when it doesn't matter whether the result was a
    * success or failure.
    *
    * ==Examples==
    *
    * {{{
    * >>> val ok: Result[Int, Int] = Ok(3)
    * >>> val err: Result[Int, Int] = Err(4)
    *
    * >>> ok.intoOkOrErr
    * 3
    *
    * >>> err.intoOkOrErr
    * 4
    * }}}
    *
    * @group Extract
    */
  def intoOkOrErr[R](implicit
      @implicitNotFound("${T} is not a ${R}") vr: T <:< R,
      @implicitNotFound("${E} is not a ${R}") er: E <:< R
  ): R = this match {
    case Ok(v)  => vr(v)
    case Err(e) => er(e)
  }

  /** Returns the contained [[Ok]] value, but never throws
    *
    * Unlike [[unwrap]], this method is known to never throw on the result types it is implemented for. Therefore, it
    * can be used instead of [[unwrap]] as a maintainability safeguard that will fail to compile if the error type of
    * the [[Result]] is later changed to an error that can actually occur.
    *
    * To leverage this method, the result must match `Result[Nothing, _]`. Because `Nothing` can never be instantiated,
    * we can be assured that if the error type is `Nothing` then an [[Err]] cannot be instantiated.
    *
    * ==Examples==
    *
    * {{{
    * >>> def onlyGoodNews(msg: String): Result[Nothing, String] = Ok("This msg is fine: " + msg)
    * >>> onlyGoodNews("Some Message").intoOk
    * This msg is fine: Some Message
    *
    * >>> val possibleError: Result[Int, String] = Ok("Some Message")
    * possibleError.intoOk // This line would fail to compile because [[intoOk]] cannot prove it isn't an [[Err]].
    * }}}
    *
    * @group Extract
    */
  def intoOk(implicit
      @implicitNotFound("Result[${E}, ${T}] is not an Ok[Nothing, ${T}]") ev: E <:< Nothing
  ): T =  this match {
    case Ok(v)  => v
    case Err(e) => ev(e) // Unreachable
  }

  /** Returns the contained [[Err]] value, but never throws
    *
    * Unlike [[unwrapErr]], this method is known to never throw on the result types it is implemented for. Therefore, it
    * can be used instead of [[unwrapErr]] as a maintainability safeguard that will fail to compile if the error type of
    * the [[Result]] is later changed to an error that can actually occur.
    *
    * To leverage this method, the result must match `Result[_, Nothing]`. Because `Nothing` can never be instantiated,
    * we can be assured that if the error type is `Nothing` then an [[Err]] cannot be instantiated.
    *
    * ==Examples==
    *
    * {{{
    * >>> def onlyBadNews(msg: String): Result[String, Nothing] = Err("This msg is unacceptable: " + msg)
    * >>> onlyBadNews("Some Error").intoErr
    * This msg is unacceptable: Some Error
    *
    * >>> val possibleOkay: Result[String, Int] = Err("Some Error")
    * possibleOkay.intoErr // This line would fail to compile because [[intoErr]] cannot prove it isn't an [[Ok]].
    * }}}
    *
    * @group Extract
    */
  def intoErr(implicit
      @implicitNotFound("Result[${E}, ${T}] is not an Err[${E}, Nothing]") ev: T <:< Nothing
  ): E = this match {
    case Ok(v)  => ev(v) // Unreachable
    case Err(e) => e
  }

  /** Converts from [[Result]]`[E, T]` to `Option[T]`.
    *
    * Converts `this` into an `Option[T]`, discarding the error, if any.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Ok(2)
    * >>> x.ok == Some(2)
    * true
    *
    * >>> val y: Result[String, Int] = Err("Nothing here")
    * >>> y.ok == None
    * true
    * }}}
    *
    * @group Collection
    */
  final def ok: Option[T] = this match {
    case Ok(v) => Some(v)
    case _     => None
  }

  /** Converts from [[Result]]`[E, T]` to `Option[E]`.
    *
    * Converts `this` into an `Option[E]`, discarding the success value, if any.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, Int] = Ok(2)
    * >>> x.err == None
    * true
    *
    * >>> val y: Result[String, Int] = Err("Nothing here")
    * >>> y.err == Some("Nothing here")
    * true
    * }}}
    *
    * @group Collection
    */
  final def err: Option[E] = this match {
    case Err(e) => Some(e)
    case _      => None
  }

  /** An alias of [[ok]] for consistency with Scala naming
    *
    * @group Collection
    */
  def toOption: Option[T] = ok

  /** An alias of [[err]] for consistency with Scala naming (`Err` suffix required for disambiguation)
    *
    * @group Collection
    */
  def toOptionErr: Option[E] = err

  /** Returns a `Seq` containing the `Ok` value if it exists or an empty `Seq` if this is a `Err`.
    *
    * ==Examples==
    *
    * {{{
    * >>> Ok(12).toSeq == Seq(12)
    * true
    *
    * >>> Err(12).toSeq == Seq()
    * true
    * }}}
    *
    * @group Collection
    */
  def toSeq: collection.immutable.Seq[T] = this match {
    case Ok(ok) => collection.immutable.Seq(ok)
    case _      => collection.immutable.Seq.empty
  }

  /** Returns a `Seq` containing the `Err` value if it exists or an empty `Seq` if this is a `Ok`.
    *
    * ==Examples==
    *
    * {{{
    * >>> Err(12).toSeqErr == Seq(12)
    * true
    *
    * >>> Ok(12).toSeqErr == Seq()
    * true
    * }}}
    *
    * @group Collection
    */
  def toSeqErr: collection.immutable.Seq[E] = this match {
    case Err(e) => collection.immutable.Seq(e)
    case _      => collection.immutable.Seq.empty
  }

  /** Returns an `Either` using the `Ok` value if it exists for `Right` otherwise using the `Err` value for `Left`.
    *
    * ==Examples==
    *
    * {{{
    * >>> Ok(9).toEither == Right(9)
    * true
    *
    * >>> val ex = new Exception("Test Exception")
    * >>> Err(12).toEither == Left(12)
    * true
    * }}}
    *
    * @group Collection
    */
  def toEither: Either[E, T] = this match {
    case Ok(ok) => Right(ok)
    case Err(e) => Left(e)
  }

  /** Returns a `Try` using the `Ok` value if it exists for `Success` otherwise using the `Err` value for `Failure`.
    *
    * NOTE: This method is only available if the `Err` value is a `Throwable`
    *
    * ==Examples==
    *
    * {{{
    * >>> import scala.util.{Failure, Success}
    *
    * >>> Ok(12).toTry == Success(12)
    * true
    *
    * >>> val ex = new Exception("Test Exception")
    * >>> Err(ex).toTry == Failure(ex)
    * true
    *
    * Err(12).toTry // This line should fail to compile
    * }}}
    *
    * @group Collection
    */
  def toTry(implicit
      @implicitNotFound("${E} is not a Throwable") ev: E <:< Throwable
  ): scala.util.Try[T] = this match {
    case Ok(ok) => scala.util.Success(ok)
    case Err(e) => scala.util.Failure(e)
  }

  /** Allows for conversion of a [[Result]]`[E, T]` into an arbitrary type `V`
    *
    * This should be used in conjunction with [[Result]]`.apply[E, T, V]` to
    * construct [[Result]]s from a user defined `V`. This can be helpful when
    * leveraging custom ADTs.
    *
    * ===Examples===
    *
    * {{{
    * >>> sealed trait Case
    * >>> case class Bad(str: String) extends Case
    * >>> case class Good(value: Int) extends Case
    *
    * >>> val goodInt = 1
    * >>> val good = Good(goodInt)
    * >>> val badStr = "Error"
    * >>> val bad = Bad(badStr)
    *
    * // A contrived example:
    * // NOTE: Using `intoOkOrErr` directly should be preferred for this case
    *
    * >>> implicit val caseFromADTResult: FromResult[Bad, Good, Case] = _.intoOkOrErr
    *
    * >>> val okADT: Result[Bad, Good] = Ok(good)
    * >>> okADT.to[Case] == good
    * true
    *
    * >>> val errADT: Result[Bad, Good] = Err(bad)
    * >>> errADT.to[Case] == bad
    * true
    *
    * // A more interesting example:
    *
    * >>> implicit val caseFromPrimitiveResult: FromResult[String, Int, Case] = {
    * ...   case Ok(v) => Good(v)
    * ...   case Err(e) => Bad(e)
    * ... }
    *
    * >>> val okPrimitive: Result[String, Int] = Ok(goodInt)
    * >>> okPrimitive.to[Case] == good
    * true
    *
    * >>> val errPrimitive: Result[String, Int] = Err(badStr)
    * >>> errPrimitive.to[Case] == bad
    * true
    * }}}
    *
    * @group Collection
    */
  def to[V](implicit fromResult: FromResult[E, T, V]): V = fromResult(this)

  /** Transposes a [[Result]] of an Ok `Option` into an `Option` of a [[Result]].
    *
    * `Ok(None)` will be mapped to `None`.
    * `Ok(Some(_))` and `Err(_)` will be mapped to `Some(Ok(_))` and `Some(Err(_))`.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x1: Result[String, Option[Int]] = Ok(Some(5))
    * >>> val x2: Option[Result[String, Int]] = Some(Ok(5))
    * >>> x1.transposeOption == x2
    * true
    *
    * >>> val y1: Result[String, Option[Int]] = Ok(None)
    * >>> val y2: Option[Result[String, Int]] = None
    * >>> y1.transposeOption == y2
    * true
    *
    * >>> val z1: Result[String, Option[Int]] = Err("Some Error")
    * >>> val z2: Option[Result[String, Int]] = Some(Err("Some Error"))
    * >>> z1.transposeOption == z2
    * true
    * }}}
    *
    * @group Collection
    */
  def transposeOption[U](implicit
      @implicitNotFound("${T} is not an Option[${U}]") ev: T <:< Option[U]
  ): Option[Result[E, U]] =
    this match {
      case Ok(option) => ev(option).map(Ok(_))
      case Err(e)     => Some(Err(e))
    }

  /** Transposes a [[Result]] of an `Err` `Option` into an `Option` of a [[Result]].
    *
    * `Err(None)` will be mapped to `None`.
    * `Err(Some(_))` and `Ok(_)` will be mapped to `Some(Err(_))` and `Some(Ok(_))`.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x1: Result[Option[Int], String] = Err(Some(5))
    * >>> val x2: Option[Result[Int, String]] = Some(Err(5))
    * >>> x1.transposeOptionErr == x2
    * true
    *
    * >>> val y1: Result[Option[Int], String] = Err(None)
    * >>> val y2: Option[Result[Int, String]] = None
    * >>> y1.transposeOptionErr == y2
    * true
    *
    * >>> val z1: Result[Option[Int], String] = Ok("Some Okay")
    * >>> val z2: Option[Result[Int, String]] = Some(Ok("Some Okay"))
    * >>> z1.transposeOptionErr == z2
    * true
    * }}}
    *
    * @group Collection
    */
  def transposeOptionErr[F](implicit
      @implicitNotFound("${E} is not an Option[${F}]") ev: E <:< Option[F]
  ): Option[Result[F, T]] =
    this match {
      case Ok(ok)      => Some(Ok(ok))
      case Err(option) => ev(option).map(Err(_))
    }

  /** Transposes a [[Result]] of an Ok `Future` into an `Future` of a [[Result]].
    *
    * `Ok(Future(_))` and `Err(_)` will be mapped to `Future(Ok(_))` and `Future(Err(_))`.
    *
    * ==Examples==
    *
    * {{{
    * >>> import scala.concurrent.Future
    * >>> implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    *
    * >>> val x: Result[String, Future[Int]] = Ok(Future(5))
    * >>> x.transposeFuture.isInstanceOf[Future[Result[String, Int]]]
    * true
    *
    * >>> val y: Result[String, Future[Int]] = Err("Some Error")
    * >>> y.transposeFuture.isInstanceOf[Future[Result[String, Int]]]
    * true
    * }}}
    *
    * @group Collection
    */
  def transposeFuture[U](implicit
      @implicitNotFound("${T} is not a Future[${U}]") ev: T <:< scala.concurrent.Future[U],
      executor: scala.concurrent.ExecutionContext
  ): scala.concurrent.Future[Result[E, U]] =
    this match {
      case Ok(future) => ev(future).map(Ok(_))
      case Err(e)     => scala.concurrent.Future(Err(e))
    }

  /** Transposes a [[Result]] of an `Err` `Future` into an `Future` of a [[Result]].
    *
    * `Err(Future(_))` and `Ok(_)` will be mapped to `Future(Err(_))` and `Future(Ok(_))`.
    *
    * ==Examples==
    *
    * {{{
    * >>> import scala.concurrent.Future
    * >>> implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    *
    * >>> val x: Result[Future[String], Int] = Ok(5)
    * >>> x.transposeFutureErr.isInstanceOf[Future[Result[String, Int]]]
    * true
    *
    * >>> val y: Result[Future[String], Int] = Err(Future("Some Error"))
    * >>> y.transposeFutureErr.isInstanceOf[Future[Result[String, Int]]]
    * true
    * }}}
    *
    * @group Collection
    */
  def transposeFutureErr[F](implicit
      @implicitNotFound("${E} is not a Future[${F}]") ev: E <:< scala.concurrent.Future[F],
      executor: scala.concurrent.ExecutionContext
  ): scala.concurrent.Future[Result[F, T]] =
    this match {
      case Ok(ok)      => scala.concurrent.Future(Ok(ok))
      case Err(future) => ev(future).map(Err(_))
    }

  /** Maps a [[Result]]`[E, T]` to [[Result]]`[E, U]` by applying a function to a contained [[Ok]] value, leaving an
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
  def map[U](op: T => U): Result[E, U] = this match {
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

  /** Maps a [[Result]]`[E, T]` to `U` by applying fallback function default to a contained [[Err]] value, or function
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

  /** Maps a [[Result]]`[E, T]` to [[Result]]`[F, T]` by applying a function to a contained [[Err]] value, leaving an
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
    * >>> Ok[Int, String]("Some Value").mapErr(square(_))
    * Ok(Some Value)
    * }}}
    *
    * @group Transform
    */
  def mapErr[F](op: E => F): Result[F, T] = this match {
    case Ok(t)  => Ok(t)
    case Err(e) => Err(op(e))
  }

    /** Returns the provided default (if [[Ok]]), or applies a function to the contained value (if [[Err]]),
    *
    * Arguments passed to `mapErrOr` are eagerly evaluated; if you are passing the result of a function call, it is
    * recommended to use [[mapErrOrElse]], which is lazily evaluated.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[String, String] = Err("foo")
    * >>> x.mapErrOr(42, _.size)
    * 3
    *
    * >>> val y: Result[String, String] = Ok("bar")
    * >>> y.mapErrOr(42, _.size)
    * 42
    * }}}
    *
    * @group Transform
    */
  def mapErrOr[F](default: F, f: E => F): F = this match {
    case Ok(_) => default
    case Err(e)  => f(e)
  }

  /** Maps a [[Result]]`[E, T]` to `F` by applying fallback function default to a contained [[Ok]] value, or function
    * `f` to a contained [[Err]] value.
    *
    * This function can be used to unpack a successful result while handling an error.
    *
    * ==Examples==
    *
    * {{{
    * >>> val k = 21
    *
    * >>> val x: Result[String, String] = Err("foo")
    * >>> x.mapErrOrElse(_ => k * 2, _.size)
    * 3
    *
    * >>> val y: Result[String, String] = Ok("bar")
    * >>> y.mapErrOrElse(_ => k * 2, _.size)
    * 42
    * }}}
    *
    * @group Transform
    */
  def mapErrOrElse[F](default: T => F, f: E => F): F = this match {
    case Err(e)  => f(e)
    case Ok(t) => default(t)
  }

  /** Converts from `Result[E, Result[E, T]]` to `Result[E, T]`
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Int, Result[Int, String]] = Ok(Ok("hello"))
    * >>> x.flatten
    * Ok(hello)
    *
    * >>> val y: Result[Int, Result[Int, String]] = Ok(Err(6))
    * >>> y.flatten
    * Err(6)
    *
    * >>> val z: Result[Int, Result[Int, String]] = Err(6)
    * >>> z.flatten
    * Err(6)
    *
    * // Flattening only removes one level of nesting at a time:
    * >>> val multi: Result[Int, Result[Int, Result[Int, String]]] = Ok(Ok(Ok("hello")))
    *
    * >>> multi.flatten
    * Ok(Ok(hello))
    *
    * >>> multi.flatten.flatten
    * Ok(hello)
    * }}}
    *
    * @group Transform
    */
  def flatten[F >: E, U](implicit
      @implicitNotFound("${T} is not a Result[${F}, ${U}]") ev: T <:< Result[F, U]
  ): Result[F, U] = andThen(ev)

  /** Converts from `Result[E, T, Result[T, E]]` to `Result[T]`
    *
    * ==Examples==
    *
    * {{{
    * >>> val x: Result[Result[String, Int], Int] = Err(Err("Some Error"))
    * >>> x.flattenErr
    * Err(Some Error)
    *
    * >>> val y: Result[Result[String, Int], Int] = Err(Ok(6))
    * >>> y.flattenErr
    * Ok(6)
    *
    * >>> val z: Result[Result[String, Int], Int] = Ok(6)
    * >>> z.flattenErr
    * Ok(6)
    *
    * // Flattening only removes one level of nesting at a time:
    * >>> val multi: Result[Result[Result[String, Int], Int], Int] = Err(Err(Err("Some Error")))
    *
    * >>> multi.flattenErr
    * Err(Err(Some Error))
    *
    * >>> multi.flattenErr.flattenErr
    * Err(Some Error)
    * }}}
    *
    * @group Transform
    */
  def flattenErr[F, U >: T](implicit
      @implicitNotFound("${E} is not a Result[${F}, ${U}]") ev: E <:< Result[F, U]
  ): Result[F, U] = orElse(ev)

  /** Applies `fOk` if this is an [[Ok]] or `fErr` if this is an [[Err]]
    *
    * ==Examples==
    *
    * {{{
    * >>> val ok = Ok[Int, Int](1)
    * >>> ok.fold(_ - 1, _ + 1)
    * 2
    *
    * >>> val err = Err[Int, Int](-1)
    * >>> err.fold(_ - 1, _ + 1)
    * -2
    * }}}
    *
    * @group Transform
    */
  def fold[O](fErr: E => O, fOk: T => O): O = this match {
    case Ok(value)  => fOk(value)
    case Err(error) => fErr(error)
  }

  /** Returns `rhs` if the result is [[Ok]], otherwise returns this [[Err]] value.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x1: Result[String, Int] = Ok(2)
    * >>> val y1: Result[String, String] = Err("late error")
    * >>> x1.and(y1)
    * Err(late error)
    *
    * >>> val x2: Result[String, Int] = Err("early error")
    * >>> val y2: Result[String, String] = Ok("foo")
    * >>> x2.and(y2)
    * Err(early error)
    *
    * >>> val x3: Result[String, Int] = Err("not a 2")
    * >>> val y3: Result[String, String] = Err("late error")
    * >>> x3.and(y3)
    * Err(not a 2)
    *
    * >>> val x4: Result[String, Int] = Ok(2)
    * >>> val y4: Result[String, String] = Ok("different result type")
    * >>> x4.and(y4)
    * Ok(different result type)
    * }}}
    *
    * @group Boolean
    */
  def and[F >: E, U >: T](rhs: Result[F, U]): Result[F, U] = this match {
    case Ok(_)  => rhs
    case Err(e) => Err(e)
  }

  /** Calls `op` if the [[Result]] is [[Ok]], otherwise returns this [[Err]] value.
    *
    * This function can be used for control flow based on `Result` values. Often used to chain fallible operations that
    * may return [[`Err`]].
    *
    * An alias of [[flatMap]]
    *
    * ==Examples==
    *
    * {{{
    * >>> def ensureEven(x: Int): Result[String, Int] = if (x % 2 == 0) Ok(x) else Err("Odd Number")
    * >>> def ensurePositive(x: Int): Result[String, Int] = if (x > 0) Ok(x) else Err("Not Positive")
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
  def andThen[F >: E, U](op: T => Result[F, U]): Result[F, U] = this match {
    case Ok(t)  => op(t)
    case Err(e) => Err(e)
  }

  /** Returns `rhs` if the [[Result]] is [[Err]], otherwise returns the this [[Ok]] value.
    *
    * Arguments passed to `or` are eagerly evaluated; if you are passing the result of a function call, it is
    * recommended to use [[orElse]], which is lazily evaluated.
    *
    * ==Examples==
    *
    * {{{
    * >>> val x1: Result[String, Int] = Ok(2)
    * >>> val y1: Result[String, Int] = Err("late error")
    * >>> x1.or(y1)
    * Ok(2)
    *
    * >>> val x2: Result[String, Int] = Err("early error")
    * >>> val y2: Result[String, Int] = Ok(2)
    * >>> x2.or(y2)
    * Ok(2)
    *
    * >>> val x3: Result[String, Int] = Err("not a 2")
    * >>> val y3: Result[String, Int] = Err("late error")
    * >>> x3.or(y3)
    * Err(late error)
    *
    * >>> val x4: Result[String, Int] = Ok(2)
    * >>> val y4: Result[String, Int] = Ok(100)
    * >>> x4.or(y4)
    * Ok(2)
    * }}}
    *
    * @group Boolean
    */
  def or[F >: E, U >: T](rhs: Result[F, U]): Result[F, U] = this match {
    case Err(_) => rhs
    case _      => this
  }

  /** Calls `op` if the result is [[Err]], otherwise returns this [[Ok]] value.
    *
    * This function can be used for control flow based on result values.
    *
    * An alias of [[flatMapErr]]
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
  def orElse[F, U >: T](op: E => Result[F, U]): Result[F, U] = this match {
    case Ok(t)  => Ok(t)
    case Err(e) => op(e)
  }

  /** An alias of [[andThen]] for compatibility with for-comprehensions and consistency with Scala naming
    *
    * @group Misc
    */
  def flatMap[F >: E, U](op: T => Result[F, U]): Result[F, U] = andThen(op)

  /** An alias of [[orElse]] for consistency with Scala naming (`Err` suffix required for disambiguation)
    *
    * @group Misc
    */
  def flatMapErr[F, U >: T](op: E => Result[F, U]): Result[F, U] = orElse(op)

  /** Executes the given side-effecting function if this is an `Ok`.
    *
    * ===Examples===
    *
    * {{{
    * Err[String, Int]("Some Error").inspect(println(_)) // Doesn't print
    * Ok(5).inspect(println(_)) // Prints 5
    * }}}
    *
    * @group Misc
    */
  def inspect[U](op: T => U): Unit = this match {
    case Ok(t) => op(t)
    case _     =>
  }

  /** Executes the given side-effecting function if this is an `Err`.
    *
    * ===Examples===
    *
    * {{{
    * Ok[Int, String]("Some Value").inspectErr(println(_)) // Doesn't print
    * Err(5).inspectErr(println(_)) // Prints 5
    * }}}
    *
    * @group Misc
    */
  def inspectErr[F](op: E => F): Unit = this match {
    case Err(e) => op(e)
    case _      =>
  }

  /** An alias of [[inspect]] for compatibility with for-comprehensions and Scala naming
    *
    * @group Misc
    */
  def foreach[U](op: T => U): Unit = inspect(op)

  /** An alias of [[inspectErr]] for consistency with Scala naming (`Err` suffix required for disambiguation)
    *
    * @group Misc
    */
  def foreachErr[F](op: E => F): Unit = inspectErr(op)

  /** Returns `true` if `Err` or returns the result of the application of the given predicate to the `Ok` value.
    *
    * ===Examples===
    *
    * {{{
    * >>> Ok(12).forall(_ > 10)
    * true
    *
    * >>> Ok(7).forall(_ > 10)
    * false
    *
    * >>> Err[Int, Int](12).forall(_ => false)
    * true
    * }}}
    *
    * @group Misc
    */
  def forall(p: T => Boolean): Boolean = this match {
    case Ok(t) => p(t)
    case _     => true
  }

  /** Returns `true` if `Ok` or returns the result of the application of the given predicate to the `Err` value.
    *
    * ===Examples===
    *
    * {{{
    * >>> Err(12).forallErr(_ > 10)
    * true
    *
    * >>> Err(7).forallErr(_ > 10)
    * false
    *
    * >>> Ok[Int, Int](12).forallErr(_ => false)
    * true
    * }}}
    *
    * @group Misc
    */
  def forallErr(p: E => Boolean): Boolean = this match {
    case Err(e) => p(e)
    case _      => true
  }

  /** Returns `false` if `Err` or returns the result of the application of the given predicate to the `Ok` value.
    *
    * ===Examples===
    *
    * {{{
    * >>> Ok(12).exists(_ > 10)
    * true
    *
    * >>> Ok(7).exists(_ > 10)
    * false
    *
    * >>> Err[Int, Int](12).exists(_ => true)
    * false
    * }}}
    *
    * @group Misc
    */
  def exists(p: T => Boolean): Boolean = this match {
    case Ok(b) => p(b)
    case _     => false
  }

  /** Returns `false` if `Ok` or returns the result of the application of the given predicate to the `Err` value.
    *
    * ===Examples===
    *
    * {{{
    * >>> Err(12).existsErr(_ > 10)
    * true
    *
    * >>> Err(7).existsErr(_ > 10)
    * false
    *
    * >>> Ok[Int, Int](12).existsErr(_ => true)
    * false
    * }}}
    *
    * @group Misc
    */
  def existsErr(p: E => Boolean): Boolean = this match {
    case Err(e) => p(e)
    case _      => false
  }

  /** Returns the existing `Ok` if this is an `Ok` and the predicate `p` holds for the ok value, or `Err(default)` if
    * the predicate `p` does not hold, else returns the existing `Err`.
    *
    * ===Examples===
    *
    * {{{
    * >>> Ok(12).filterOrElse(_ > 10, -1)
    * Ok(12)
    *
    * >>> Ok(7).filterOrElse(_ > 10, -1)
    * Err(-1)
    *
    * >>> Err(7).filterOrElse(_ => false, -1)
    * Err(7)
    * }}}
    *
    * @group Misc
    */
  def filterOrElse[F >: E](p: T => Boolean, default: => F): Result[F, T] =
    this match {
      case Ok(ok) if !p(ok) => Err(default)
      case _                => this
    }

  /** Returns the existing `Err` if this is an `Err` and the predicate `p` holds for the err value, or `Ok(default)` if
    * the predicate `p` does not hold, else returns the existing `Ok`.
    *
    * ===Examples===
    *
    * {{{
    * >>> Err(12).filterErrOrElse(_ > 10, -1)
    * Err(12)
    *
    * >>> Err(7).filterErrOrElse(_ > 10, -1)
    * Ok(-1)
    *
    * >>> Ok(7).filterErrOrElse(_ => false, -1)
    * Ok(7)
    * }}}
    *
    * @group Misc
    */
  def filterErrOrElse[U >: T](p: E => Boolean, default: => U): Result[E, U] =
    this match {
      case Err(b) if !p(b) => Ok(default)
      case _               => this
    }

  /** Upcasts this `Result[E, T]` to `Result[E, U]`
    *
    * Normally used when constructing an [[Err]]
    *
    * ===Examples===
    *
    * {{{
    * scala> Err(1)
    * res0: Err[Int, Nothing] = Err(1)
    *
    * scala> Err(2).withOk[String]
    * res1: Result[Int, String] = Err(2)
    * }}}
    *
    * @group Cast
    */
  def withOk[U >: T]: Result[E, U] = this

  /** Upcasts this `Result[E, T]` to `Result[F, T]`
    *
    * Normally used when constructing an [[Ok]]
    *
    * ===Examples===
    *
    * {{{
    * scala> Ok(1)
    * res0: Ok[Nothing, Int] = Ok(1)
    *
    * scala> Ok(2).withErr[String]
    * res1: Result[String, Int] = Ok(2)
    * }}}
    *
    * @group Cast
    */
  def withErr[F >: E]: Result[F, T] = this
}

object Result {

  /** Allows for construction of a [[Result]]`[E, T]` from an arbitrary type `V`
    *
    * NOTE: [[ToResult]] has a implicits defined for `Either` and `Try`
    *
    * ===Examples===
    *
    * {{{
    * >>> sealed trait Case
    * >>> case class Bad(str: String) extends Case
    * >>> case class Good(value: Int) extends Case
    *
    * >>> val goodInt = 1
    * >>> val good = Good(goodInt)
    * >>> val badStr = "Error"
    * >>> val bad = Bad(badStr)
    *
    * >>> implicit val resultFromCase: ToResult[String, Int, Case] = {
    * ...   case Good(v) => Ok(v)
    * ...   case Bad(e) => Err(e)
    * ... }
    *
    * >>> Result(good) == Ok(goodInt)
    * true
    *
    * >>> Result(bad) == Err(badStr)
    * true
    * }}}
    *
    * @group Extract
    */
  def apply[E, T, V](value: V)(implicit
      toResult: ToResult[E, T, V]
  ): Result[E, T] = toResult(value)

  @throws(classOf[RuntimeException])
  private final def unwrapFailed[X, Y](msg: String, value: Y): X =
    throw new RuntimeException(s"$msg: $value")
}

/** Contains the success value
  *
  * @groupname Extract Extracting contained values
  * @groupprio Extract 1
  */
case class Ok[+E, +T](v: T) extends AnyVal with Result[E, T] {
  /** Returns the contained [[Ok]] value, but never throws
    *
    * Unlike [[unwrap]], this method is known to never throw.
    * Related: [[Result.intoOk]]
    *
    * ==Examples==
    *
    * {{{
    * >>> val ok = Ok("Some Message")
    * >>> ok.intoOk
    * Some Message
    * }}}
    *
    * @group Extract
    */
  def intoOk: T = v
}

/** Contains the error value
  *
  * @groupname Extract Extracting contained values
  * @groupprio Extract 1
  */
case class Err[+E, +T](e: E) extends AnyVal with Result[E, T] {
  /** Returns the contained [[Err]] value, but never throws
    *
    * Unlike [[unwrap]], this method is known to never throw.
    * Related: [[Result.intoErr]]
    *
    * ==Examples==
    *
    * {{{
    * >>> val err = Err("Some Error")
    * >>> err.intoErr
    * Some Error
    * }}}
    *
    * @group Extract
    */
  def intoErr: E = e
}
