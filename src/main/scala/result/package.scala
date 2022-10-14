import scala.math.Ordering

/** =Use Case=
  *
  * Error handling with the `Result` type.
  *
  * [[Result]] is a type used for returning and propagating errors. It is an disjoint union with the variants, [[Ok]],
  * representing success and containing a value, and [[Err]], representing error and containing an error value.
  *
  * Functions should return [[Result]] whenever errors are expected and recoverable.
  *
  * For simplicity many examples make use of primitives such as `String` and `Int` for the error type. It is recommended
  * that in practice developers should try to make use of more structured types to allow for improved error handling. As
  * opposed to relying on a stringly-typed interface or integer error codes.
  *
  * A simple function returning [[Result]] might be defined and used like so:
  *
  * {{{
  * >>> import result._
  * >>> sealed trait MajorVersion
  * >>> object MajorVersion {
  * ...   case object V1 extends MajorVersion
  * ...   case object V2 extends MajorVersion
  * ... }
  *
  * >>> sealed trait ParseError
  * >>> object ParseError {
  * ...  case object InvalidHeaderLength extends ParseError
  * ...  case object UnsupportedVersion extends ParseError
  * ... }
  *
  * >>> def parseMajorVersion(header: List[Int]): Result[ParseError, MajorVersion] =
  * ...   header.headOption match {
  * ...     case None    => Err(ParseError.InvalidHeaderLength)
  * ...     case Some(1) => Ok(MajorVersion.V1)
  * ...     case Some(2) => Ok(MajorVersion.V2)
  * ...     case _       => Err(ParseError.UnsupportedVersion)
  * ...   }
  *
  * >>> val version = parseMajorVersion(List(1, 2, 3, 4))
  * >>> version match {
  * ...   case Ok(v)  => "working with version: " + v.toString
  * ...   case Err(e) => "error parsing header: " + e.toString
  * ... }
  * working with version: V1
  *
  * }}}
  *
  * Pattern matching on [[Result]]s is clear and straightforward for simple cases, but [[Result]] comes with some
  * convenience methods that make working with it more succinct.
  *
  * {{{
  * >>> import result._
  * >>> val goodResult: Result[String, Int] = Ok(10);
  * >>> val badResult: Result[String, Int] = Err("Some Error")
  *
  * // The `isOk` and `isErr` methods do what they say.
  *
  * >>> goodResult.isOk && !goodResult.isErr
  * true
  *
  * >>> badResult.isErr && !badResult.isOk
  * true
  *
  * // `map` replaces the `Ok` value of a `Result` with the result of the provided function
  * >>> goodResult.map(_ + 1)
  * Ok(11)
  *
  * // `map` leaves an `Err` value of a `Result` as it was, ignoring the provided function
  * >>> badResult.map(_ - 1)
  * Err(Some Error)
  *
  * // Use `andThen` to continue the computation.
  * scala> goodResult.andThen(i => Ok(i == 11))
  * res0: Result[String, Boolean] = Ok(false)
  *
  * // Use `orElse` to handle the error.
  * scala> badResult.orElse {
  *      |   case "Anticipated Error" => Ok(0)
  *      |   case "Some Error"        => Err(true)
  *      |   case _                   => Err(false)
  *      | }
  * res1: Result[Boolean, Int] = Err(true)
  * }}}
  *
  * =Method overview=
  *
  * In addition to working with pattern matching, [[Result]] provides a wide variety of different methods.
  *
  * ==Querying the variant==
  *
  * The [[Result.isOk isOk]] and [[Result.isErr isErr]] methods return `true` if the [[Result]] is [[Ok]] or [[Err]],
  * respectively.
  *
  * The [[Result.isOkAnd isOkAnd]] and [[Result.isErrAnd isErrAnd]] methods take in a predicate and return `true` if the
  * [[Result]] is [[Ok]] or [[Err]] respectively, and the predicate returns `true` when applied to the contained value.
  *
  * The [[Result.contains contains]] and [[Result.containsErr containsErr]] methods take in a value and return `true` if
  * it matches the inner [[Ok]] or [[Err]] value respectively.
  *
  * ==Transforming contained values==
  *
  * These methods transform [[Result]] to `Option`:
  *
  *   - [[Result.err err]] transforms [[Result]]`[E, T]` into `Option[E]`, mapping [[Err]]`(e)` to `Some(e)` and
  *   [[Ok]]`(v)` to `None`
  *   - [[Result.ok ok]] transforms [[Result]]`[E, T]` into `Option[T]`, mapping [[Ok]]`(v)` to `Some(v)` and
  *   [[Err]]`(e)` to `None`
  *   - [[Result.transposeOption transposeOption]] transposes a [[Result]]`[E, Option[T]]` into an
  *   `Option[`[[Result]]`[E, T]]`
  *   - [[Result.transposeOptionErr transposeOptionErr]] transposes a [[Result]]`[Option[E], T]` into an
  *   `Option[`[[Result]]`[E, T]]`
  *
  * This method transforms the contained value of the [[result.Ok Ok]] variant:
  *
  *   - [[Result.map map]] transforms [[Result]]`[E, T]` into [[Result]]`[E, U]` by applying the provided function to
  *   the contained value of [[Ok]] and leaving [[Err]] values unchanged
  *
  * This method transforms the contained value of the [[result.Err Err]] variant:
  *
  *   - [[Result.mapErr mapErr]] transforms [[Result]]`[E, T]` into [[Result]]`[F, T]` by applying the provided function
  *   to the contained value of [[Err]] and leaving [[Ok]] values unchanged
  *
  * These methods transform a [[Result]]`[E, T]` into a value of a possibly different type `U`:
  *
  *   - [[Result.mapOr mapOr]] applies the provided function to the contained value of [[Ok]], or returns the provided
  *   default value if the [[Result]] is [[Err]]
  *   - [[Result.mapOrElse mapOrElse]] applies the provided function to the contained value of [[Ok]], or applies the
  *   provided default fallback function to the contained value of [[Err]]
  *
  * These methods transform [[Result]] to `Future`:
  *
  *   - [[Result.transposeFuture transposeFuture]] transposes a [[Result]]`[E, Future[T]]` into a
  *   `Future[`[[Result]]`[E, T]]`
  *   - [[Result.transposeFutureErr transposeFutureErr]] transposes a [[Result]]`[Future[E], T]` into a
  *   `Future[`[[Result]]`[E, T]]`
  *
  * ==Extracting contained values==
  *
  * These methods extract the contained value in a [[Result]]`[E, T]` when it is the [[Ok]]
  * variant. If the [[Result]] is [[Err]]:
  *
  *   - [[Result.expect expect]] panics with a provided custom message
  *   - [[Result.unwrap unwrap]] panics with a generic message
  *   - [[Result.unwrapOr unwrapOr]] returns the provided default value
  *   - [[Result.unwrapOrElse unwrapOrElse]] returns the result of evaluating the provided function
  *
  * These methods extract the contained value in a [[Result]]`[E, T]` when it is the [[Err]]
  * variant. If the [[Result]] is [[Ok]]:
  *
  *   - [[Result.expectErr expectErr]] panics with a provided custom message
  *   - [[Result.unwrapErr unwrapErr]] panics with a generic message
  *
  * ==Boolean operators==
  *
  * These methods treat the [[Result]] as a boolean value, where [[Ok]] acts like `true` and [[Err]] acts like `false`.
  * There are two categories of these methods: ones that take a [[Result]] as input, and ones that take a function as
  * input (to be lazily evaluated).
  *
  * The [[Result.and and]] and [[Result.or or]] methods take another [[Result]] as input, and produce a [[Result]] as
  * output. The [[Result.and and]] method can produce a [[Result]]`[E, U]` value having a different inner type `U` than
  * [[Result]]`[E, T]`. The [[Result.or or]] method can produce a [[Result]]`[F, T]` value having a different error type
  * `F` than [[Result]]`[E, T]`.
  *
  * | method             | self     | input     | output   |
  * |--------------------|----------|-----------|----------|
  * | [[Result.and and]] | `Err(e)` | (ignored) | `Err(e)` |
  * | [[Result.and and]] | `Ok(x)`  | `Err(d)`  | `Err(d)` |
  * | [[Result.and and]] | `Ok(x)`  | `Ok(y)`   | `Ok(y)`  |
  * | [[Result.or or]]   | `Err(e)` | `Err(d)`  | `Err(d)` |
  * | [[Result.or or]]   | `Err(e)` | `Ok(y)`   | `Ok(y)`  |
  * | [[Result.or or]]   | `Ok(x)`  | (ignored) | `Ok(x)`  |
  *
  * The [[Result.andThen andThen]] and [[Result.orElse orElse]] methods take a function as input, and only evaluate the
  * function when they need to produce a new value. The [[Result.andThen andThen]] method can produce a
  * [[Result]]`[E, U]` value having a different inner type `U` than [[Result]]`[E, T]`. The [[Result.orElse orElse]]
  * method can produce a [[Result]]`[F, T]` value having a different error type `F` than [[Result]]`[E, T]`.
  *
  * NOTE: [[Result.flatMap flatMap]] is equivalent to[[Result.andThen andThen]] and it is also provided to consistency
  * with typical Scala patterns.
  *
  * | method                     | self     | function input | function result | output   |
  * |----------------------------|----------|----------------|-----------------|----------|
  * | [[Result.andThen andThen]] | `Err(e)` | (not provided) | (not evaluated) | `Err(e)` |
  * | [[Result.andThen andThen]] | `Ok(x)`  | `x`            | `Err(d)`        | `Err(d)` |
  * | [[Result.andThen andThen]] | `Ok(x)`  | `x`            | `Ok(y)`         | `Ok(y)`  |
  * | [[Result.orElse orElse]]   | `Err(e)` | `e`            | `Err(d)`        | `Err(d)` |
  * | [[Result.orElse orElse]]   | `Err(e)` | `e`            | `Ok(y)`         | `Ok(y)`  |
  * | [[Result.orElse orElse]]   | `Ok(x)`  | (not provided) | (not evaluated) | `Ok(x)`  |
  *
  * ==Implicit classes==
  *
  * Implicit classes are provided to facilitate conversion of `Option`, `Either`, and `Try` types to a [[Result]].
  * They are available by importing [[RichOption]], [[RichEither]], and [[RichTry]] respectively.
  *
  * @note
  * This documentation is a derivative of the [[https://doc.rust-lang.org/std/result/ Rust Result<T, E> documentation]]
  */
package object result {
  import scala.language.implicitConversions

  /** Implicit conversion wrapper for `Option`
    *
    * ==Examples==
    *
    * {{{
    * >>> Some(1).toResult(2)
    * Ok(1)
    * }}}
    */
  implicit class RichOption[O](val self: Option[O]) extends AnyVal {

    /** An alias of [[toOkOrElse]]
      *
      * ==Examples==
      *
      * {{{
      * >>> None.toResult(1)
      * Err(1)
      *
      * >>> Some(2).toResult(1)
      * Ok(2)
      * }}}
      */
    def toResult[E](default: => E): Result[E, O] =
      toOkOrElse(default)

    /** Convert an `Option[E]` into a Result[E, T] using `default` for the `Ok` value in the `None` case.
      *
      * ==Examples==
      *
      * {{{
      * >>> None.toErrOrElse(1)
      * Ok(1)
      *
      * >>> Some(2).toErrOrElse(1)
      * Err(2)
      * }}}
      */
    def toErrOrElse[T](default: => T): Result[O, T] =
      self.map(Err(_)).getOrElse(Ok(default))

    /** Convert an `Option[T]` into a Result[E, T] using `default` for the `Err` value in the `None` case.
      *
      * ==Examples==
      *
      * {{{
      * >>> None.toOkOrElse(1)
      * Err(1)
      *
      * >>> Some(2).toOkOrElse(1)
      * Ok(2)
      * }}}
      */
    def toOkOrElse[E](default: => E): Result[E, O] =
      self.map(Ok(_)).getOrElse(Err(default))

    /** Convert an `Option[`[[Result]]`[E, T]]` into a [[Result]]`[E, Option[T]]`
      * with the `None` case being treated as an `Ok`
      *
      * ==Examples==
      *
      * {{{
      * >>> Some(Ok(1)).transposeOk
      * Ok(Some(1))
      *
      * >>> Some(Err(2)).transposeOk
      * Err(2)
      *
      * >>> None.transposeOk
      * Ok(None)
      * }}}
      */
    def transposeOk[T, E](implicit
        ev: O <:< Result[E, T]
    ): Result[E, Option[T]] = self match {
      case Some(result) => ev(result).map(Some(_))
      case None         => Ok(None)
    }

    /** Convert an `Option[`[[Result]]`[E, T]]` into a [[Result]]`[E, Option[T]]`
      * with the `None` case being treated as an `Err`
      *
      * ==Examples==
      *
      * {{{
      * >>> Some(Err(1)).transposeErr
      * Err(Some(1))
      *
      * >>> Some(Ok(2)).transposeErr
      * Ok(2)
      *
      * >>> None.transposeErr
      * Err(None)
      * }}}
      */
    def transposeErr[T, E](implicit
        ev: O <:< Result[E, T]
    ): Result[Option[E], T] = self match {
      case Some(result) => ev(result).mapErr(Some(_))
      case None         => Err(None)
    }
  }

  /** Implicit conversion wrapper for `Either`
    *
    * ==Examples==
    *
    * {{{
    * >>> Right(1).toResult
    * Ok(1)
    * }}}
    */
  implicit class RichEither[L, R](val self: Either[L, R]) extends AnyVal {

    /** Convert an `Either[L, R]` to a [[Result]]`[L, R]`.
      *
      * This is useful for the cases where usage of `Either` does not follow the convention that the `Right` value is
      * used to represent an `Ok`.
      *
      * ==Examples==
      *
      * {{{
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

  /** Implicit conversion wrapper for `Try`
    *
    * ==Examples==
    *
    * {{{
    * >>> scala.util.Success(1).toResult
    * Ok(1)
    *
    * >>> case object TestException extends Throwable
    * >>> val result = scala.util.Failure(TestException).toResult
    * >>> result.containsErr(TestException)
    * true
    * }}}
    */
  implicit class RichTry[T](val self: scala.util.Try[T]) extends AnyVal {
    def toResult: Result[Throwable, T] = self match {
      case scala.util.Success(ok) => Ok(ok)
      case scala.util.Failure(e)  => Err(e)
    }
  }

  /** Implicit conversion wrapper for iterable collections of [[Result]]`[E, T]` */
  implicit class ResultIterableOps[E, T, +CC[_], +C](
      val self: scala.collection.IterableOps[Result[E, T], CC, C]
  ) {

    /** A pair of, first, all [[Ok]] elements and, second, all [[Err]] elements.
      *
      * Has the same limitations as `partition` on the given collection.
      *
      * ==Examples==
      *
      * {{{
      * >>> val (oks, errs) = Vector(Ok(1), Err("A"), Ok(2), Ok(3), Err("B")).partitionResult
      *
      * >>> oks == Vector(1, 2, 3)
      * true
      *
      * >>> errs == Vector("A", "B")
      * true
      * }}}
      */
    def partitionResult[DD[_], D](implicit
        ev: C <:< scala.collection.IterableOps[Result[E, T], DD, D]
    ): (DD[T], DD[E]) = {
      val (oks, errs) = self.partition(_.isOk)
      (ev(oks).map(_.unwrap), ev(errs).map(_.unwrapErr))
    }
  }
}
