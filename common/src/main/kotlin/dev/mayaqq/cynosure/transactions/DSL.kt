@file:JvmName("DSL")
package dev.mayaqq.cynosure.transactions

import dev.mayaqq.cynosure.transactions.internal.LocalManager
import dev.mayaqq.cynosure.transactions.internal.TransactionManagerAccess

// TODO: Document
@TransactionsDsl
public inline fun transaction(action: Transaction.() -> Unit) {
    TransactionManagerAccess.openOuter().use(action)
}

@TransactionsDsl
public inline fun TransactionContext.transaction(action: Transaction.() -> Unit) {
    TransactionManagerAccess.openInner(this).use(action)
}

public val transactionDepth: Int
    get() = LocalManager.depth