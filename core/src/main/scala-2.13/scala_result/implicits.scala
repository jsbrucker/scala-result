package scala_result

/** Implicit definitions used to add extension methods */
object implicits
    extends extensions.all.Syntax
    with extensions.option.Syntax
    with extensions.iterable_ops.Syntax
