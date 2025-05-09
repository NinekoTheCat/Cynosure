package dev.mayaqq.cynosure.transactions.snapshot

import dev.mayaqq.cynosure.transactions.CloseListener
import dev.mayaqq.cynosure.transactions.OuterCloseListener
import dev.mayaqq.cynosure.transactions.TransactionContext
import dev.mayaqq.cynosure.transactions.TransactionResult

public abstract class SnapshotParticipant<T : Any> : CloseListener, OuterCloseListener {
    private val snapshots: MutableList<T?> = mutableListOf()

    protected abstract fun createSnapshot(): T

    protected abstract fun readSnapshot(snapshot: T)

    protected open fun discardSnapshot(snapshot: T) {}

    protected open fun onFinalCommit() {}

    protected fun updateSnapshots(context: TransactionContext) {
        if (snapshots.getOrNull(context.nestingDepth) == null) {
            if (snapshots.size < context.nestingDepth) {
                val nulls = arrayOfNulls<Any>(context.nestingDepth - snapshots.size)
                snapshots.addAll(nulls as Array<T?>)
            }
            snapshots[context.nestingDepth] = createSnapshot()
            context.addOuterCloseListener(this)
        }
    }

    final override fun TransactionContext.onClose(result: TransactionResult) {
        val snapshot = this@SnapshotParticipant.snapshots.set(nestingDepth, null) ?: return

        when {
            result == TransactionResult.ABORTED -> {
                this@SnapshotParticipant.readSnapshot(snapshot)
                this@SnapshotParticipant.discardSnapshot(snapshot)
            }
            nestingDepth > 0 -> {
                if (this@SnapshotParticipant.snapshots.getOrNull(nestingDepth -1) == null) {
                    this@SnapshotParticipant.snapshots[nestingDepth - 1] = snapshot
                    this[nestingDepth - 1]?.addCloseListener(this@SnapshotParticipant)
                } else {
                    this@SnapshotParticipant.discardSnapshot(snapshot)
                }
            }
            else -> {
                this@SnapshotParticipant.discardSnapshot(snapshot)
                addOuterCloseListener(this@SnapshotParticipant)
            }
        }
    }

    final override fun onOuterClose(result: TransactionResult) {}
}