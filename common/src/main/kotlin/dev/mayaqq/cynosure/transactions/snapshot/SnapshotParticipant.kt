package dev.mayaqq.cynosure.transactions.snapshot

import dev.mayaqq.cynosure.transactions.CloseListener
import dev.mayaqq.cynosure.transactions.OuterCloseListener
import dev.mayaqq.cynosure.transactions.TransactionContext
import dev.mayaqq.cynosure.transactions.TransactionResult
import net.minecraft.world.item.ItemStack

public abstract class SnapshotParticipant<T : Any> : CloseListener, OuterCloseListener {
    private val snapshots: MutableList<T?> = mutableListOf()

    protected abstract fun createSnapshot(): T

    protected abstract fun readSnapshot(snapshot: T)

    protected open fun discardSnapshot(snapshot: T) {}

    protected open fun onFinalCommit() {}

    protected fun TransactionContext.updateSnapshots() {
        if (snapshots.getOrNull(nestingDepth) == null) {
            if (snapshots.size < nestingDepth) {
                val nulls = arrayOfNulls<Any>(nestingDepth - snapshots.size)
                snapshots.addAll(nulls as Array<T?>)
            }
            snapshots[nestingDepth] = createSnapshot()
            addOuterCloseListener(this@SnapshotParticipant)
        }
    }

    final override fun TransactionContext.onClose(result: TransactionResult) {
        val snapshot = snapshots.set(nestingDepth, null) ?: return
        when {
            result == TransactionResult.ABORTED -> {
                readSnapshot(snapshot)
                discardSnapshot(snapshot)
            }
            nestingDepth > 0 -> {
                if (snapshots.getOrNull(nestingDepth -1) == null) {
                    snapshots[nestingDepth - 1] = snapshot
                    this[nestingDepth - 1]?.addCloseListener(this@SnapshotParticipant)
                } else {
                    discardSnapshot(snapshot)
                    ItemStack.CODEC
                }
            }
            else -> {
                discardSnapshot(snapshot)
                addOuterCloseListener(this@SnapshotParticipant)
            }
        }
    }

    final override fun onOuterClose(result: TransactionResult) {
        onFinalCommit()
    }
}