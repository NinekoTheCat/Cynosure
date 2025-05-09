package dev.mayaqq.cynosure.utils.codecs

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import com.teamresourceful.bytecodecs.base.ByteCodec
import dev.mayaqq.cynosure.utils.bytecodecs.FriendlyByteCodec
import net.minecraft.world.item.crafting.Ingredient

public object IngredientCodec : Codec<Ingredient> by Codec.PASSTHROUGH.comapFlatMap(
    IngredientCodec::decodeIngredient, IngredientCodec::encodeIngredient
) {
    @JvmField
    public val NETWORK: ByteCodec<Ingredient> = FriendlyByteCodec(Ingredient::toNetwork, Ingredient::fromNetwork)

    private fun decodeIngredient(dynamic: Dynamic<*>): DataResult<Ingredient> {
        val thing = dynamic.convert(JsonOps.INSTANCE).value
        return DataResult.success(Ingredient.fromJson(thing))
    }

    private fun encodeIngredient(ingredient: Ingredient): Dynamic<JsonElement> =
        Dynamic(JsonOps.INSTANCE, ingredient.toJson())

}