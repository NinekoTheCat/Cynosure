package dev.mayaqq.cynosure.transactions.snapshot.primitives

import dev.mayaqq.cynosure.transactions.TransactionContext
import dev.mayaqq.cynosure.transactions.snapshot.SnapshotParticipant

public class TransactionalInt(
    initial: Int
) : SnapshotParticipant<Int>() {

    public var value: Int = initial
        private set

    context(TransactionContext)
    public fun increment(by: Int = 1): Int {
        updateSnapshots()
        val v = value
        value += by
        return v
    }

    context(TransactionContext)
    public fun decrement(by: Int = 1): Int {
        updateSnapshots()
        val v = value
        value -= by
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