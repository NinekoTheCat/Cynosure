package dev.mayaqq.cynosure.storage

import dev.mayaqq.cynosure.storage.resource.Resource

public interface SlottedStorage<R : Resource> : Storage<R>, Iterable<StorageSlot<R>> {

    public val size: Int

    public operator fun get(index: Int): StorageSlot<R>

}