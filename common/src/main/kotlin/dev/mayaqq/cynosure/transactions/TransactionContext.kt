package dev.mayaqq.cynosure.transactions

public sealed interface TransactionContext {

    public val nestingDepth: Int

    public fun addCloseListener(listener: CloseListener)

    public fun addOuterCloseListener(listener: OuterCloseListener)

    public operator fun get(depth: Int): Transaction?

}