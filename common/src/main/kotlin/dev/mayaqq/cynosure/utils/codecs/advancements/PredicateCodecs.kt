package dev.mayaqq.cynosure.utils.codecs.advancements

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import dev.mayaqq.cynosure.utils.predicate.entityPredicateFromNetwork
import dev.mayaqq.cynosure.utils.predicate.toNetwork
import io.netty.buffer.Unpooled
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.network.FriendlyByteBuf
import java.util.ArrayList

public object EntityPredicateCodec {
    public val CODEC: Codec<EntityPredicate> = Codec.PASSTHROUGH.comapFlatMap(::decodeEntityPredicate, ::encodeEntityPredicate)
    public val BYTE_CODEC: Codec<EntityPredicate> = Codec.BYTE.listOf().flatXmap(::decodeEntityPredicateFromNetwork, ::encodeEntityPredicateToNetwork)

    private fun decodeEntityPredicate(dynamic: Dynamic<*>): DataResult<EntityPredicate> {
        val thing: Any = dynamic.convert(JsonOps.INSTANCE).value
        if (thing is JsonElement) return DataResult.success(EntityPredicate.fromJson(thing))
        return DataResult.error { "Value was not an instance of JsonElement" }
    }

    private fun encodeEntityPredicate(predicate: EntityPredicate): Dynamic<JsonElement> {
        return Dynamic(JsonOps.INSTANCE, predicate.serializeToJson()).convert(JsonOps.COMPRESSED)
    }

    private fun decodeEntityPredicateFromNetwork(data: List<Byte>): DataResult<EntityPredicate> {
        try {
            val array = ByteArray(data.size)
            for (i in data.indices) {
                array[i] = data[i]
            }
            val buffer = Unpooled.wrappedBuffer(array)
            return DataResult.success(entityPredicateFromNetwork(FriendlyByteBuf(buffer)))
        } catch (e: Exception) {
            return DataResult.error { "Failed to decode ingredient from network: " + e.message }
        }
    }

    private fun encodeEntityPredicateToNetwork(predicate: EntityPredicate): DataResult<List<Byte>> {
        try {
            val buffer = Unpooled.buffer()
            predicate.toNetwork(FriendlyByteBuf(buffer))
            val array = buffer.array()
            val bytes: MutableList<Byte> = ArrayList(array.size)
            for (b in array) {
                bytes.add(b)
            }
            return DataResult.success(bytes)
        } catch (e: Exception) {
            return DataResult.error { "Failed to encode ingredient to network: " + e.message }
        }
    }
}