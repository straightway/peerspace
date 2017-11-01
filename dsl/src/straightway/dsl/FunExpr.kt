package straightway.dsl

/**
 * An expression who's value is computed by a given functor.
 */
open class FunExpr(final override val arity: Int, val name: String, private val functor: (Array<out Any>) -> Any) : Expr {

    constructor(name: String, functor: () -> Any) : this(0, name, { _ -> functor() })
    constructor(name: String, functor: (Any) -> Any) : this(1, name, { args -> functor(args[0]) })
    constructor(name: String, functor: (Any, Any) -> Any) : this(2, name, { args -> functor(args[0], args[1]) })

    override operator fun invoke(vararg params: Any): Any {
        assert(params.size == arity) { "Invalid number of parameters. Expected: $arity, got: ${params.size}" }
        return functor(params)
    }

    override fun toString() = name

    companion object {
        inline operator fun <reified TArg> invoke(name: String, noinline functor: (TArg) -> Any)
            = FunExpr(name, untyped(functor))
        inline operator fun <reified TArg1, reified TArg2> invoke(name: String, noinline functor: (TArg1, TArg2) -> Any)
            = FunExpr(name, untyped(functor))
    }
    init {
        assert(0 <= arity) { "Expressions must have non-negative arity (arity: $arity)" }
    }
}