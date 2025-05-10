package dev.mayaqq.cynosure.core.bytecodecs.item

import com.teamresourceful.bytecodecs.base.ByteCodec
import com.teamresourceful.bytecodecs.base.`object`.ObjectByteCodec
import dev.mayaqq.cynosure.core.bytecodecs.ExtraByteCodecs
import dev.mayaqq.cynosure.core.codecs.fieldOf
import net.minecraft.world.item.ItemStack

public object ItemStackByteCodec : ByteCodec<ItemStack> by ByteCodec.BYTE.dispatch(ItemStackByteCodec::pickByteCodec, ItemStackByteCodec::getType) {

    public val EMPTY_ITEM: ByteCodec<ItemStack> = ByteCodec.unit(ItemStack.EMPTY)

    public val SINGLE_ITEM: ByteCodec<ItemStack> = ExtraByteCodecs.ITEM.map(::ItemStack, ItemStack::getItem)

    public val ITEM_STACK_WITH_COUNT: ByteCodec<ItemStack> = ObjectByteCodec.create(
        ExtraByteCodecs.ITEM fieldOf ItemStack::getItem,
        ByteCodec.INT fieldOf ItemStack::getCount,
        ::ItemStack
    )

    public val ITEM_WITH_COUNT_AND_TAG: ByteCodec<ItemStack> = ObjectByteCodec.create(
        ExtraByteCodecs.ITEM fieldOf ItemStack::getItem,
        ByteCodec.INT fieldOf ItemStack::getCount,
        ExtraByteCodecs.NULLABLE_COMPOUND_TAG fieldOf ItemStack::getTag
    ) { item, count, tag ->
        val stack = ItemStack(item, count)
        tag?.let(stack::setTag)
        stack
    }

    @JvmStatic
    private fun pickByteCodec(id: Byte): ByteCodec<ItemStack> = when(id.toInt()) {
        1 -> SINGLE_ITEM
        2 -> ITEM_STACK_WITH_COUNT
        3 -> ITEM_WITH_COUNT_AND_TAG
        else -> EMPTY_ITEM
    }

    @JvmStatic
    private fun getType(stack: ItemStack): Byte = when {
        stack.hasTag() -> 3
        stack.count > 1 -> 2
        !stack.isEmpty -> 1
        else -> 0
    }.toByte()
}


