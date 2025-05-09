package dev.mayaqq.cynosure.utils.predicate

import dev.mayaqq.cynosure.utils.codecs.advancements.EntityPredicateCodec
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.network.FriendlyByteBuf

public fun EntityPredicate.toNetwork(buf: FriendlyByteBuf) {
    buf.writeJsonWithCodec(EntityPredicateCodec.CODEC, this)
}

public fun entityPredicateFromNetwork(buf: FriendlyByteBuf): EntityPredicate {
    return buf.readJsonWithCodec(EntityPredicateCodec.CODEC)
}