package dev.mayaqq.cynosure.core.codecs.item

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import dev.mayaqq.cynosure.core.codecs.Codecs
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import java.util.function.Function

public object ItemStackCodec : Codec<ItemStack> by Codecs.lazy({ Codecs.alternatives(
    ItemStackCodec.EMPTY_STACK,
    ItemStackCodec.SINGLE_ITEM,
    ItemStackCodec.MULTIPLE_ITEM
) }) {

    public val EMPTY_STACK: Codec<ItemStack> = Codec.unit(ItemStack.EMPTY)
        .flatComapMap(
            Function.identity(),
            fun(stack) = if (stack != ItemStack.EMPTY) DataResult.error { "Stack is not empty" } else DataResult.success(stack)
        )

    public val SINGLE_ITEM: Codec<ItemStack> = BuiltInRegistries.ITEM.byNameCodec()
        .flatComapMap(
            ::ItemStack,
            fun(stack) = if (stack.count > 1 || stack.hasTag()) DataResult.error { "Stack contains more than one item or has a tag" }
                else DataResult.success(stack.item)
        )

    public val MULTIPLE_ITEM: Codec<ItemStack> = ItemStack.CODEC

}