package dev.mayaqq.cynosure.transactions

@TransactionsDsl
public fun interface OuterCloseListener {
    public fun onOuterClose(result: TransactionResult)
}

@TransactionsDsl
public fun interface CloseListener {
    public fun TransactionContext.onClose(result: TransactionResult)
}