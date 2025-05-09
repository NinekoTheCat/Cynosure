package dev.mayaqq.cynosure.utils.codecs

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.Unpooled
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import java.util.*


/**
 * Code modified from [Resourcefullib](https://github.com/Team-Resourceful/Resourcefullib) by Team Resourceful.
 * Licensed under MIT
 */
public object IngredientCodec {
    public val CODEC: Codec<Ingredient> = Codec.PASSTHROUGH.comapFlatMap(::decodeIngredient, ::encodeIngredient)
    public val BYTE_CODEC: Codec<Ingredient> = Codec.BYTE.listOf().flatXmap(::decodeIngredientFromNetwork, ::encodeIngredientToNetwork)

    private fun decodeIngredient(dynamic: Dynamic<*>): DataResult<Ingredient> {
        val thing: Any = dynamic.convert(JsonOps.INSTANCE).value
        if (thing is JsonElement) return DataResult.success(Ingredient.fromJson(thing))
        return DataResult.error { "Value was not an instance of JsonElement" }
    }

    private fun encodeIngredient(ingredient: Ingredient): Dynamic<JsonElement> {
        return Dynamic(JsonOps.INSTANCE, ingredient.toJson()).convert(JsonOps.COMPRESSED)
    }

    private fun decodeIngredientFromNetwork(data: List<Byte>): DataResult<Ingredient> {
        try {
            val array = ByteArray(data.size)
            for (i in data.indices) {
                array[i] = data[i]
            }
            val buffer = Unpooled.wrappedBuffer(array)
            return DataResult.success(Ingredient.fromNetwork(FriendlyByteBuf(buffer)))
        } catch (e: Exception) {
            return DataResult.error { "Failed to decode ingredient from network: " + e.message }
        }
    }

    private fun encodeIngredientToNetwork(ingredient: Ingredient): DataResult<List<Byte>> {
        try {
            val buffer = Unpooled.buffer()
            ingredient.toNetwork(FriendlyByteBuf(buffer))
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

public object ItemStackCodec {

    private val STRING_EITHER: Codec<ItemStack> = BuiltInRegistries.ITEM.byNameCodec().xmap(
        { ItemStack(it) },
        { stack: ItemStack -> stack.item })

    private val STACK_CODEC: Codec<ItemStack> =
        RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<ItemStack> ->
            instance.group<Item, Int, Optional<CompoundTag>>(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("id")
                    .forGetter { obj: ItemStack -> obj.item },
                Codec.INT.fieldOf("count").orElse(1)
                    .forGetter { obj: ItemStack -> obj.count },
                CompoundTag.CODEC.optionalFieldOf("nbt")
                    .forGetter { o: ItemStack ->
                        Optional.ofNullable(
                            o.tag
                        )
                    }
            ).apply(instance, ItemStackCodec::createItemStack)
        }

    public val CODEC: Codec<ItemStack> = Codecs.alternatives(STRING_EITHER, STACK_CODEC)

    public val NETWORK_CODEC: Codec<ItemStack> =
        RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<ItemStack> ->
            instance.group(
                Codecs.registryId(BuiltInRegistries.ITEM).fieldOf("id").forGetter { stack: ItemStack -> stack.item },
                Codec.INT.fieldOf("count").orElse(1).forGetter { obj: ItemStack -> obj.count },
                CompoundTag.CODEC.optionalFieldOf("nbt").forGetter { o: ItemStack -> Optional.ofNullable(o.tag) }
            ).apply(instance, ItemStackCodec::createItemStack)
        }

    private fun createItemStack(item: ItemLike, count: Int, tagOptional: Optional<CompoundTag>): ItemStack {
        val stack = ItemStack(item, count)
        tagOptional.ifPresent { tag: CompoundTag -> stack.tag = tag }
        return stack
    }
}