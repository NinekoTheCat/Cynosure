@file:JvmName("DSL")
package dev.mayaqq.cynosure.transactions

import dev.mayaqq.cynosure.transactions.internal.TransactionManagerAccess

@TransactionsDsl
public inline fun transaction(action: Transaction.() -> Unit) {
    TransactionManagerAccess.openOuter().use(action)
}

@TransactionsDsl
public inline fun TransactionContext.transaction(action: Transaction.() -> Unit) {
    TransactionManagerAccess.openInner(this).use(action)
}


fun meow() {

    transaction {

        transaction {

        }
        commit
    }
}