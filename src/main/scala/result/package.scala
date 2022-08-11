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
  * sealed trait MajorVersion
  * object MajorVersion {
  *   case object V1 extends MajorVersion
  *   case object V2 extends MajorVersion
  * }
  *
  * sealed trait ParseError
  * object ParseError {
  *   case object InvalidHeaderLength extends ParseError
  *   case object UnsupportedVersion extends ParseError
  * }
  *
  * def parseMajorVersion(header: List[Int]): Result[MajorVersion, ParseError] =
  *   header.headOption match {
  *     case None    => Err(ParseError.InvalidHeaderLength)
  *     case Some(1) => Ok(MajorVersion.V1)
  *     case Some(2) => Ok(MajorVersion.V2)
  *     case _       => Err(ParseError.UnsupportedVersion)
  *   }
  *
  * val version = parseMajorVersion(List(1, 2, 3, 4))
  * version match {
  *   case Ok(v)  => println(s"working with version: \$v")
  *   case Err(e) => println(s"error parsing header: \$e")
  * }
  * }}}
  *
  * Pattern matching on [[Result]]s is clear and straightforward for simple cases, but [[Result]] comes with some
  * convenience methods that make working with it more succinct.
  *
  * {{{
  * val goodResult: Result[Int, String] = Ok(10);
  * val badResult: Result[Int, String] = Err("Some Error");
  *
  * // The `isOk` and `isErr` methods do what they say.
  * assert(goodResult.isOk && !goodResult.isErr)
  * assert(badResult.isErr && !badResult.isOk)
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
  * The [[Result.isOkAnd isOkAnd]] and [[Result.isErrAnd isErrAnd]] take in a predicate and return `true` if the
  * [[Result]] is [[Ok]] or [[Err]] respectively, and the predicate returns `true` when applied to the contained value.
  *
  * @note
  * This documentation is a derivative of the [[https://doc.rust-lang.org/std/result/ Rust Result<T, E> documentation]]
  */
package object result
