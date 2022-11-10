package result

import scala.language.implicitConversions

/** Implicit converters to allow `Result` methods and functionality to be
  * leveraged on types with companion [[ToResult]] and [[FromResult]] impls.
  */
object ImplicitConversions {

  /** Converts a `V` to a `Result[E, T]`
    *
    * Usage: `import result.ImplicitConversions.toResult`
    */
  implicit def toResult[E, T, V](value: V)(implicit
      ev: ToResult[E, T, V]
  ): Result[E, T] = Result(value)

  /** Converts a `Result[E, T]` to a `V`
    *
    * Usage: `import result.ImplicitConversions.fromResult`
    */
  implicit def fromResult[E, T, V](result: Result[E, T])(implicit
      ev: FromResult[E, T, V]
  ): V = result.to[V]
}
