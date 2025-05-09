package dev.mayaqq.cynosure.transactions.internal

import dev.mayaqq.cynosure.transactions.*
import dev.mayaqq.cynosure.utils.getValue
import dev.mayaqq.cynosure.utils.result.failure
import dev.mayaqq.cynosure.utils.result.unit

internal val LocalManager: TransactionManager by ThreadLocal.withInitial(::TransactionManager)

internal class TransactionManager {

    private val thread: Thread = Thread.currentThread()
    private val stack: ArrayDeque<TransactionImpl> = ArrayDeque()
    private val outerCloseListeners: MutableList<OuterCloseListener> = mutableListOf()
    private var currentDepth: Int = -1

    val isOpen: Boolean
        get() = currentDepth > -1

    val lifecycle: Transaction.Lifecycle
        get() = if (!isOpen) Transaction.Lifecycle.OPEN else stack[currentDepth].lifecycle

    fun openOuter(): Transaction {
        if (currentDepth > -1) error("Transaction already open")
        return open()
    }

    fun open(): Transaction {
        currentDepth++

        val transaction = if (stack.size == currentDepth) TransactionImpl(currentDepth).also(stack::add) else stack[currentDepth]
        transaction.lifecycle = Transaction.Lifecycle.OPEN
        return transaction
    }

    fun validateThread() {
        val current = Thread.currentThread()
        require(current == thread) { "Attempted to access transation from thread ${thread.name} on ${current.name}" }
    }

    fun getCurrentUnsafe(): Transaction? {
        validateThread()
        return stack[currentDepth].takeIf { it.lifecycle == Transaction.Lifecycle.OPEN }
    }

    internal inner class TransactionImpl(depth: Int) : Transaction(depth) {

        override var lifecycle: Lifecycle = Lifecycle.NONE

        private val closeCallbacks: MutableList<CloseListener> = mutableListOf()

        private fun validateCurrentTransaction() {
            validateThread()
            require(nestingDepth == currentDepth) { "Attempted to call transaction function on a transaction with depth $nestingDepth but current depth is $currentDepth" }
        }

        private fun validateOpen() = require(lifecycle == Lifecycle.OPEN) { "Cannot close a transaction that isnt open" }

        fun openInner(): Transaction {
            validateOpen()
            validateCurrentTransaction()
            return open()
        }

        override fun close(result: TransactionResult) {
            validateCurrentTransaction()
            lifecycle = Lifecycle.CLOSING

            var closeResult: Result<Unit> = Result.unit
            for (index in closeCallbacks.size downTo 0) {
                closeCallbacks[index].runCatching { (this@TransactionImpl as TransactionContext).onClose(result) }
                    .onFailure { (closeResult.exceptionOrNull() ?: RuntimeException("Transaction error").also { closeResult = it.failure() }).addSuppressed(it) }
            }

            if (currentDepth == -1) {
                lifecycle = Lifecycle.OUTER_CLOSING
                for (index in outerCloseListeners.size downTo 0) {
                    outerCloseListeners[index].runCatching { this@runCatching.onOuterClose(result) }
                        .onFailure { (closeResult.exceptionOrNull() ?: RuntimeException("Transaction error").also { closeResult = it.failure() }).addSuppressed(it) }
                }
            }

            currentDepth--
            lifecycle = Lifecycle.NONE
            closeResult.getOrThrow()
        }

        override fun addOuterCloseListener(listener: OuterCloseListener) {
            validateThread()
            if (!isOpen) error("No transaction open on current thread")
            outerCloseListeners.add(listener)
        }

        override fun addCloseListener(listener: CloseListener) {
            validateThread()
            validateOpen()
            closeCallbacks.add(listener)
        }

        override fun get(depth: Int): Transaction? {
            validateThread()
            return stack.getOrNull(depth)?.takeIf { it.lifecycle == Lifecycle.OPEN }
        }
    }


}