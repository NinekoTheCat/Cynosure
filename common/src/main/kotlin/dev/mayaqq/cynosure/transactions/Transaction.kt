package dev.mayaqq.cynosure.transactions

import java.lang.AutoCloseable

public abstract class Transaction protected constructor(
    override val nestingDepth: Int
) : TransactionContext, AutoCloseable {

    public abstract val lifecycle: Lifecycle

    @TransactionsDsl
    public inline val commit: Unit
        get() { commit() }

    @TransactionsDsl
    public inline val abort: Unit
        get() { abort() }

    @TransactionsDsl
    public fun commit() {
        close(TransactionResult.COMMITED)
    }

    @TransactionsDsl
    public fun abort() {
        close(TransactionResult.ABORTED)
    }

    override fun close() {
        if (lifecycle == Lifecycle.OPEN) abort()
    }

    protected abstract fun close(result: TransactionResult)

    public enum class Lifecycle {
        NONE,
        OPEN,
        CLOSING,
        OUTER_CLOSING
    }
}