package dev.mayaqq.cynosure.transactions.snapshot

import dev.mayaqq.cynosure.transactions.TransactionContext

public class TransactionalInt(
    initial: Int
) : SnapshotParticipant<Int>() {

    public var value: Int = initial
        private set

    context(TransactionContext)
    public fun increment(): Int {
        updateSnapshots()
        val v = value
        value++
        return v
    }

    context(TransactionContext)
    public fun set(value: Int): Int {
        updateSnapshots()
        val v = this@TransactionalInt.value
        this.value = value
        return v
    }

    override fun createSnapshot(): Int = value

    override fun readSnapshot(snapshot: Int) {
        this.value = snapshot
    }
}