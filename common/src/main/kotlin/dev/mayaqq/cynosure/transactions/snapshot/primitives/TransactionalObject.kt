package dev.mayaqq.cynosure.transactions.snapshot.primitives

import dev.mayaqq.cynosure.transactions.TransactionContext
import dev.mayaqq.cynosure.transactions.snapshot.SnapshotParticipant

public class TransactionalObject<T : Any>(
    initial: T
) : SnapshotParticipant<T>() {

    public var value: T = initial
        private set

    context(TransactionContext)
    public fun set(newValue: T): T {
        updateSnapshots()
        val v = value
        value = newValue
        return v
    }

    override fun createSnapshot(): T = value

    override fun readSnapshot(snapshot: T) {
        value = snapshot
    }
}