package result

/** A Rust `Result<T, E>` inspired interface for handling results.
  *
  * [[Result]] is a type that represents either success ([[Ok]]) or failure ([[Err]]).
  * See the [[result package documentation]] for details.
  */
sealed trait Result[+T, +E] extends Any

/** Contains the success value */
case class Ok[+T, +E](v: T) extends AnyVal with Result[T, E]

/** Contains the error value */
case class Err[+T, +E](e: E) extends AnyVal with Result[T, E]
