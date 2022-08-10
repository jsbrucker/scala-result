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
  * >>> def parseMajorVersion(header: List[Int]): Result[MajorVersion, ParseError] =
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
  * >>> val goodResult: Result[Int, String] = Ok(10);
  * >>> val badResult: Result[Int, String] = Err("Some Error")
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
  * res0: Result[Boolean, String] = Ok(false)
  *
  * // Use `orElse` to handle the error.
  * scala> badResult.orElse {
  *      |   case "Anticipated Error" => Ok(0)
  *      |   case "Some Error"        => Err(true)
  *      |   case _                   => Err(false)
  *      | }
  * res1: Result[Int, Boolean] = Err(true)
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
  *   - [[Result.err err]] transforms [[Result]]`[T, E]` into `Option[E]`, mapping [[Err]]`(e)` to `Some(e)` and
  *   [[Ok]]`(v)` to `None`
  *   - [[Result.ok ok]] transforms [[Result]]`[T, E]` into `Option[T]`, mapping [[Ok]]`(v)` to `Some(v)` and
  *   [[Err]]`(e)` to `None`
  *   - [[Result.transpose transpose]] transposes a [[Result]] of an `Option` into an `Option` of a [[Result]]
  *
  * This method transforms the contained value of the [[result.Ok Ok]] variant:
  *
  *   - [[Result.map map]] transforms [[Result]]`[T, E]` into [[Result]]`[U, E]` by applying the provided function to
  *   the contained value of [[Ok]] and leaving [[Err]] values unchanged
  *
  * This method transforms the contained value of the [[result.Err Err]] variant:
  *
  *   - [[Result.mapErr mapErr]] transforms [[Result]]`[T, E]` into [[Result]]`[T, F]` by applying the provided function
  *   to the contained value of [[Err]] and leaving [[Ok]] values unchanged
  *
  * These methods transform a [[Result]]`[T, E]` into a value of a possibly different type `U`:
  *
  *   - [[Result.mapOr mapOr]] applies the provided function to the contained value of [[Ok]], or returns the provided
  *   default value if the [[Result]] is [[Err]]
  *   - [[Result.mapOrElse mapOrElse]] applies the provided function to the contained value of [[Ok]], or applies the
  *   provided default fallback function to the contained value of [[Err]]
  *
  * ==Extracting contained values==
  *
  * These methods extract the contained value in a [[Result]]`[T, E]` when it is the [[Ok]]
  * variant. If the [[Result]] is [[Err]]:
  *
  *   - [[Result.expect expect]] panics with a provided custom message
  *   - [[Result.unwrap unwrap]] panics with a generic message
  *   - [[Result.unwrapOr unwrapOr]] returns the provided default value
  *   - [[Result.unwrapOrElse unwrapOrElse]] returns the result of evaluating the provided function
  *
  * These methods extract the contained value in a [[Result]]`[T, E]` when it is the [[Err]]
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
  * output. The [[Result.and and]] method can produce a [[Result]]`[U, E]` value having a different inner type `U` than
  * [[Result]]`[T, E]`. The [[Result.or or]] method can produce a [[Result]]`[T, F]` value having a different error type
  * `F` than [[Result]]`[T, E]`.
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
  * [[Result]]`[U, E]` value having a different inner type `U` than [[Result]]`[T, E]`. The [[Result.orElse orElse]]
  * method can produce a [[Result]]`[T, F]` value having a different error type `F` than [[Result]]`[T, E]`.
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
  * @note
  * This documentation is a derivative of the [[https://doc.rust-lang.org/std/result/ Rust Result<T, E> documentation]]
  */
package object result
