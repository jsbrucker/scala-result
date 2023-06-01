package dev.jsbrucker

/** =Use Case=
  *
  * Error handling with the [[Result]] type.
  *
  * `Result` is a type used for returning and propagating errors. It is an
  * disjoint union with the variants, [[Ok]], representing success and
  * containing a value, and [[Err]], representing error and containing an error
  * value.
  *
  * Functions should return `Result` whenever errors are expected and
  * recoverable.
  *
  * For simplicity many examples make use of primitives such as `String` and
  * `Int` for the error type. It is recommended that in practice developers
  * should try to make use of more structured types to allow for improved error
  * handling. As opposed to relying on a stringly-typed interface or integer
  * error codes.
  *
  * A simple function returning `Result` might be defined and used like so:
  *
  * {{{
  * >>> import dev.jsbrucker.result._
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
  * Pattern matching on `Result`s is clear and straightforward for simple cases,
  * but `Result` comes with some convenience methods that make working with it
  * more succinct.
  *
  * {{{
  * >>> import dev.jsbrucker.result._
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
  * In addition to working with pattern matching, `Result` provides a wide
  * variety of different methods.
  *
  * ==Querying the variant==
  *
  * The [[Result.isOk isOk]] and [[Result.isErr isErr]] methods return `true` if
  * the `Result` is `Ok` or `Err`, respectively.
  *
  * The [[Result.isOkAnd isOkAnd]] and [[Result.isErrAnd isErrAnd]] methods take
  * in a predicate and return `true` if the `Result` is `Ok` or `Err`
  * respectively, and the predicate returns `true` when applied to the contained
  * value.
  *
  * The [[Result.contains contains]] and [[Result.containsErr containsErr]]
  * methods take in a value and return `true` if it matches the inner `Ok` or
  * `Err` value respectively.
  *
  * ==Transforming contained values==
  *
  * These methods transform `Result` to `Option`:
  *
  *   - [[Result.err err]] transforms `Result[E, T]` into `Option[E]`, mapping
  *     `Err(e)` to `Some(e)` and `Ok(v)` to `None`
  *   - [[Result.ok ok]] transforms `Result[E, T]` into `Option[T]`, mapping
  *     `Ok(v)` to `Some(v)` and `Err(e)` to `None`
  *   - [[Result.transposeOption transposeOption]] transposes a `Result[E,
  *     Option[T]]` into an `Option[Result[E, T]]`
  *   - [[Result.transposeOptionErr transposeOptionErr]] transposes a
  *     `Result[Option[E], T]` into an `Option[Result[E, T]]`
  *
  * This method transforms the contained value of the `Ok` variant:
  *
  *   - [[Result.map map]] transforms `Result[E, T]` into `Result[E, U]` by
  *     applying the provided function to the contained value of `Ok` and
  *     leaving `Err` values unchanged
  *
  * This method transforms the contained value of the `Err` variant:
  *
  *   - [[Result.mapErr mapErr]] transforms `Result[E, T]` into `Result[F, T]`
  *     by applying the provided function to the contained value of `Err` and
  *     leaving `Ok` values unchanged
  *
  * These methods transform a `Result[E, T]` into a value of a possibly
  * different type `U`:
  *
  *   - [[Result.mapOr mapOr]] and [[Result.mapErrOr mapErrOr]] applies the
  *     provided function to the contained value of `Ok` or `Err` respecitively,
  *     or returns the provided default value.
  *   - [[Result.mapOrElse mapOrElse]] and [[Result.mapErrOrElse mapErrOrElse]]
  *     applies the provided function to the contained value of `Ok` or `Err`
  *     respectively, or applies the provided default fallback function to the
  *     contained value of `Err` or `Ok` respectively.
  *
  * These methods transform `Result` to `Future`:
  *
  *   - [[Result.transposeFuture transposeFuture]] transposes a `Result[E,
  *     Future[T]]` into a `Future[Result[E, T]]`
  *   - [[Result.transposeFutureErr transposeFutureErr]] transposes a
  *     `Result[Future[E], T]` into a `Future[Result[E, T]]`
  *
  * ==Extracting contained values==
  *
  * These methods extract the contained value in a `Result[E, T]` when it is the
  * `Ok` variant. If the `Result` is `Err`:
  *
  *   - [[Result.expect expect]] panics with a provided custom message
  *   - [[Result.unwrap unwrap]] panics with a generic message
  *   - [[Result.unwrapOr unwrapOr]] returns the provided default value
  *   - [[Result.unwrapOrElse unwrapOrElse]] returns the result of evaluating
  *     the provided function
  *
  * These methods extract the contained value in a `Result[E, T]` when it is the
  * `Err` variant. If the `Result` is `Ok`:
  *
  *   - [[Result.expectErr expectErr]] panics with a provided custom message
  *   - [[Result.unwrapErr unwrapErr]] panics with a generic message
  *
  * ==Boolean operators==
  *
  * These methods treat the `Result` as a boolean value, where `Ok` acts like
  * `true` and `Err` acts like `false`. There are two categories of these
  * methods: ones that take a `Result` as input, and ones that take a function
  * as input (to be lazily evaluated).
  *
  * The [[Result.and and]] and [[Result.or or]] methods take another `Result` as
  * input, and produce a `Result` as output. The `and` method can produce a
  * `Result[E, U]` value having a different inner type `U` than `Result[E, T]`.
  * The `or` method can produce a `Result[F, T]` value having a different error
  * type `F` than `Result[E, T]`.
  *
  * | method | self     | input     | output   |
  * |:-------|:---------|:----------|:---------|
  * | `and`  | `Err(e)` | (ignored) | `Err(e)` |
  * | `and`  | `Ok(x)`  | `Err(d)`  | `Err(d)` |
  * | `and`  | `Ok(x)`  | `Ok(y)`   | `Ok(y)`  |
  * | `or`   | `Err(e)` | `Err(d)`  | `Err(d)` |
  * | `or`   | `Err(e)` | `Ok(y)`   | `Ok(y)`  |
  * | `or`   | `Ok(x)`  | (ignored) | `Ok(x)`  |
  *
  * The [[Result.andThen andThen]] and [[Result.orElse orElse]] methods take a
  * function as input, and only evaluate the function when they need to produce
  * a new value. The `andThen` method can produce a `Result[E, U]` value having
  * a different inner type `U` than `Result[E, T]`. The `orElse` method can
  * produce a `Result[F, T]` value having a different error type `F` than
  * `Result[E, T]`.
  *
  * NOTE: [[Result.flatMap flatMap]] is equivalent to `andThen` and it is
  * provided for consistency with typical Scala conventions. Along those lines
  * [[Result.flatMapErr flatMapErr]] is equivalent to `orElse`.
  *
  * | method    | self     | function input | function result | output   |
  * |:----------|:---------|:---------------|:----------------|:---------|
  * | `andThen` | `Err(e)` | (not provided) | (not evaluated) | `Err(e)` |
  * | `andThen` | `Ok(x)`  | `x`            | `Err(d)`        | `Err(d)` |
  * | `andThen` | `Ok(x)`  | `x`            | `Ok(y)`         | `Ok(y)`  |
  * | `orElse`  | `Err(e)` | `e`            | `Err(d)`        | `Err(d)` |
  * | `orElse`  | `Err(e)` | `e`            | `Ok(y)`         | `Ok(y)`  |
  * | `orElse`  | `Ok(x)`  | (not provided) | (not evaluated) | `Ok(x)`  |
  *
  * ==Implicits==
  *
  * Extension methods are provided to facilitate conversion of several types to
  * a `Result`. They can imported using `import dev.jsbrucker.result.implicits._`
  *   - All types get some extension methods out of the box. This includes:
  *     - [[extensions.all.Ops.asOk asOk]]
  *     - [[extensions.all.Ops.asErr asErr]]
  *     - [[extensions.all.Ops.toResult toResult]] - For types with an implicit
  *       definition of [[ToResult]] in scope.
  *   - `Option` gets a number of additional helpers. See:
  *     [[extensions.option.Ops]]
  *   - When using Scala 2.13 [[extensions.iterable_ops.Ops]] is available for
  *     processing collections of `Result`s.
  *
  * @note
  *   This documentation began as a derivative of the
  *   [[https://doc.rust-lang.org/std/result/ Rust Result<T, E> documentation]]
  */
package object result {}
