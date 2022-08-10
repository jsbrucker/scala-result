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
  * ==Transforming contained values==
  *
  * These methods transform [[Result]] to `Option`:
  *
  *   - [[Result.err err]] transforms [[Result]]`[T, E]` into `Option[E]`,
  *   mapping [[Err]]`(e)` to `Some(e)` and [[Ok]]`(v)` to `None`
  *   - [[Result.ok ok]] transforms [[Result]]`[T, E]` into `Option[T]`,
  *   mapping [[Ok]]`(v)` to `Some(v)` and [[Err]]`(e)` to `None`
  *   - [[Result.transpose transpose]] transposes a [[Result]] of an `Option` into an `Option` of a [[Result]]
  *
  * ==Extracting contained values==
  *
  * These methods extract the contained value in a [[Result]]`[T, E]` when it is the [[result.Ok Ok]]
  * variant. If the [[Result]] is [[result.Err Err]]:
  *
  *   - [[Result.expect expect]] panics with a provided custom message
  *   - [[Result.unwrap unwrap]] panics with a generic message
  *   - [[Result.unwrapOr unwrapOr]] returns the provided default value
  *   - [[Result.unwrapOrElse unwrapOrElse]] returns the result of evaluating the provided function
  *
  * These methods extract the contained value in a [[Result]]`[T, E]` when it is the [[result.Err Err]]
  * variant. If the [[Result]] is [[result.Ok Ok]]:
  *
  *   - [[Result.expectErr expectErr]] panics with a provided custom message
  *   - [[Result.unwrapErr unwrapErr]] panics with a generic message
  *
  * @note
  * This documentation is a derivative of the [[https://doc.rust-lang.org/std/result/ Rust Result<T, E> documentation]]
  */
package object result
