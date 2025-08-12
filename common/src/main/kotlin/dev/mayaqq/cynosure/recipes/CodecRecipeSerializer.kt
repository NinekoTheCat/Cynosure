package dev.mayaqq.cynosure.recipes

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.teamresourceful.bytecodecs.base.ByteCodec
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class CodecRecipeSerializer<T : Recipe<*>>(
    public val name: String,
    private val codec: (ResourceLocation) -> Codec<T>,
    private val networkCodec: (ResourceLocation) -> ByteCodec<T>
) : RecipeSerializer<T> {

    private companion object Log : Logger by LoggerFactory.getLogger(CodecRecipeSerializer::class.java)

    override fun fromJson(var1: ResourceLocation, var2: JsonObject): T {
        return codec(var1).parse(JsonOps.INSTANCE, var2)
            .getOrThrow(true) { error("Error reading codec recipe: $it") }
    }

    override fun fromNetwork(var1: ResourceLocation, var2: FriendlyByteBuf): T {
        return networkCodec(var1).decode(var2)
    }

    override fun toNetwork(var1: FriendlyByteBuf, var2: T) {
        networkCodec(var2.id).encode(var2, var1)
    }

    /**
     * Writes [recipe] to [writeTo] as JSON, intended for encoding recipes
     * into a [net.minecraft.data.recipes.FinishedRecipe]'s output.
     */
    public fun toJson(recipeId: ResourceLocation, writeTo: JsonObject, recipe: T) {
        val obj = codec(recipeId).encodeStart(JsonOps.INSTANCE,recipe).getOrThrow(true)
        { error("Error writing codec recipe: $it") }.asJsonObject
        obj.entrySet().forEach {
            writeTo.add(it.key,it.value)
        }
    }

}