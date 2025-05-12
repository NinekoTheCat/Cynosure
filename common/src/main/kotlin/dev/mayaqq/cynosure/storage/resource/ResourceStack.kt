package dev.mayaqq.cynosure.storage.resource

public class ResourceStack<R : Resource>(
    public val resource: R,
    public val amount: Long
) {


}