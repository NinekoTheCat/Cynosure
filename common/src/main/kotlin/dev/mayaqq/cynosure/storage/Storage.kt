@file:JvmName("SlottedStorageKt")

package dev.mayaqq.cynosure.storage

import dev.mayaqq.cynosure.storage.resource.Resource
import dev.mayaqq.cynosure.transactions.TransactionContext

public interface Storage<R : Resource> {

    public val supportsInsertion: Boolean get() = true

    public val supportsExtraction: Boolean get() = true

    context(TransactionContext)
    public fun insert(resource: R, amount: Long): Long

    context(TransactionContext)
    public fun extract(resource: R, amount: Long): Long
}

public fun <R : Resource> Storage<R>.insert(context: TransactionContext, resource: R, amount: Long) {
    return context.run { insert(resource, amount) }
}

public fun <R : Resource> Storage<R>.extract(context: TransactionContext, resource: R, amount: Long) {
    return context.run { extract(resource, amount) }
}