/** =Use Case=
  *
  * Error handling with the `Result` type.
  *
  * [[Result]] is a type used for returning and propagating errors. It is an disjoint union with the variants, [[Ok]],
  * representing success and containing a value, and [[Err]], representing error and containing an error value.
  *
  * Functions should return [[Result]] whenever errors are expected and recoverable.
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
  * @note
  * This documentation is a derivative of the [[https://doc.rust-lang.org/std/result/ Rust Result<T, E> documentation]]
  */
package object result
