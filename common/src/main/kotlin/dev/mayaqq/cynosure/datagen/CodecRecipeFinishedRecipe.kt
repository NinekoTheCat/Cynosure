package dev.mayaqq.cynosure.datagen

import com.google.gson.JsonObject
import dev.mayaqq.cynosure.recipes.CodecRecipeSerializer
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

/**
 * Generic class to help with the implementation of [FinishedRecipe] for recipes which utilise a [CodecRecipeSerializer]
 */
public abstract class AbstractCodecRecipeFinishedRecipe<S : CodecRecipeSerializer<R>,R : Recipe<*>>
    (private val recipe: R, private val serializer: S) : FinishedRecipe {
    override fun serializeRecipeData(p0: JsonObject): Unit = serializer.toJson(recipe.id, p0, recipe)

    override fun getId(): ResourceLocation =ResourceLocation(recipe.id.namespace,"${serializer.name}/${recipe.id.path}")

    override fun getType(): RecipeSerializer<*> = serializer
}

public class CodecRecipeFinishedRecipeNoAchievements<S : CodecRecipeSerializer<R>,R : Recipe<*>>
    (recipe: R, serializer: S) : AbstractCodecRecipeFinishedRecipe<S,R>(
    recipe, serializer
) {
    override fun serializeAdvancement(): JsonObject? = null

    override fun getAdvancementId(): ResourceLocation? = null
}