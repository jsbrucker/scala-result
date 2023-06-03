# Result Features

## General
* Update methods to leverage pass-by-name when possible
* ErrorBiased Result, same methods but Err/Ok usage flipped?
  * maybe a @errBiased annotation macro for `for` comprehensions?
  * .errBiased
```scala
case class ErrBiasedResult[T, E] private[result](result: Result[E, T]) {
  def map[F](op: E => F): ErrBiasedResult[T, F] = ???
  def flatMap[U >: T, F](op: E => Result[F, U]): ErrBiasedResult[U, F] = ???
  def flatMap[U >: T, F](op: E => ErrBiasedResult[U, F]): ErrBiasedResult[U, F] = ???
  def contains(err: E): Boolean
}
```

## Performance (compile-time and run-time; cpu and memory)
* Compare implicit constraints defined on method vs. on implicit class definitions.
* Compare constructing new cases vs `.asInstanceOf`
* Compare `sealed trait` vs `sealed abstract class` base
* Compare `extends Any` vs `extends Product with Serializable`

## ScalaPB
* TypeMapper

## Result
* In cases where a type is known to be `Unit` - Done
```scala
def map[U](okValue: => U)(implicit ec: T <:< Unit): Result[E, U]
def mapErr[F](errValue: => F)(implicit ec: E <:< Unit): Result[F, T]
```
* flatten containing Option - Done
```scala
def flatten[U](implicit ec: E <:< Unit, tc: T <:< Option[U]): Result[E, U] = this match {
  case Ok(Some(u)) => Ok(u)
  case _ => Err.unit
}
def flattenErr[F](implicit ec: E <:< Option[F], tc: T <:< Unit): Result[F, U] = this match {
  case Err(Some(f)) => Err(f)
  case _ => Ok.unit
}
```

## Boolean
* ToResult - DONE
### Not convinced the following are useful additions
* Ok.when(cond: Boolean): Result[Unit, Unit] = Ok.when[Unit](cond)(())
* Ok.when[T](cond: Boolean)(okValue: => T): Result[Unit, T]
* Err.when(cond: Boolean): Result[Unit, Unit] = Err.when[Unit](cond)(())
* Err.when[E](cond: Boolean)(errValue: => E): Result[E, Unit]
* Ok.whenOrElse[E, T](cond: Boolean)(okValue: => T, errValue: => E): Result[E, T]
* Err.whenOrElse[E, T](cond: Boolean)(errValue: => E, okValue: => T): Result[E, T]

## RichFuture
* Future[V] => Future[Result[Throwable, V]]
```scala
def transformToResult(implicit ec: ExecutionContext): Future[Result[Throwable, V]] = self.transform {
  case Success(v) => Ok(v)
  case Failure(ex) => Err(ex)
}
```
```scala
// Not going to support a case where the return value `V` is expected as a `E`
def recoverToResult[E, T >: V](pf: PartialFuntion[Throwable, Result[E, T]])(implicit ec: ExecutionContext): Future[Result[E, T]] = self.map(Ok(_)).recover(pf)
```
```scala
def recoverToErr[E](pf: PartialFunction[Throwable, E])(implicit ec: ExecutionContext): Future[Result[E, V] = self.map(Ok(_)).recover(pf.andThen(Err(_)))
```

## RichOption
* toOk: Result[Unit, T] AND toErr: Result[E, Unit] - DONE
* Option[Result[E, T]] => Result[E, T]
```scala
def resultOrElse[F >: E, U >: T](default: => Result[F, U])(implicit ec: Result[E, T] <:< O) =
  self.getOrElse(default)
```
* Option[Result[E, T]] => Option[T]
```scala
def flatten(implicit ec: Result[E, T] <:< O): Option[T] = self.flatMap(_.ok)
```
* Option[Result[E, T]] => Option[E]
```scala
def flattenErr(implicit ec: Result[E, T] <:< O): Option[E] = self.flatMap(_.err)
```

## FutureResult - ResultT type ops
## OptionResult - ResultT type ops

## ZIO
* .fromResult
