package dev.mayaqq.cynosure.items

import dev.mayaqq.cynosure.CynosureInternal
import dev.mayaqq.cynosure.core.extensions.Extension
import dev.mayaqq.cynosure.core.extensions.ExtensionRegistry
import dev.mayaqq.cynosure.internal.CynosureHooks
import dev.mayaqq.cynosure.items.ItemExtension.Registry.register
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

/**
 * ItemExtensions includes a set of interfaces that extend normal item's functionality. These can be either
 * implemented by implementing the interface on the item class, or by registering an extension using [register]
 */
public interface ItemExtension : Extension<Item> {

    /**
     * Companion object providing registry functionality
     */
    public companion object Registry : ExtensionRegistry<Item, ItemExtension>(Item::class.java, ItemExtension::class.java)
}

public fun <E : ItemExtension> Item.registerExtension(extension: E, allowOverride: Boolean = false) {
    ItemExtension.register(this, extension, allowOverride)
}

public inline fun <reified E : ItemExtension> Item.getExtension(): E? =
    ItemExtension.getExtension(E::class.java, this)

/**
 * Java Helper for [getExtension]
 */
public fun <E : ItemExtension> getExtension(item: Item, extensionClass: Class<E>): E? =
    ItemExtension.getExtension(extensionClass, item)


/**
 * Give this item the ability to be traded with piglins
 */
public fun interface BarterCurrency : ItemExtension {
    public fun isBarterCurrency(stack: ItemStack): Boolean
}

/**
 * Makes an item make piglins neutral
 */
public interface PiglinNeutralizing : ItemExtension {
    public fun makesPiglinsNeutral(entity: LivingEntity, stack: ItemStack): Boolean
}

/**
 * An extension providing the ability to listen to item entity ticks
 */
public fun interface ItemEntityTickListener : ItemExtension {
    public fun tickItemEntity(stack: ItemStack, entity: ItemEntity): Boolean
}

/**
 * An extension providing itemstack dependant furnace burn times
 */
public fun interface CustomFurnaceFuel : ItemExtension {
    public fun getItemBurnTime(stack: ItemStack, recipeType: RecipeType<*>?): Int
}

/**
 * Allows creating a custom entity for this item
 */
public fun interface CustomEntityItem : ItemExtension {
    public fun createItemEntity(original: ItemEntity, level: Level, stack: ItemStack): Entity
}

/**
 * Disables the cape rendering when equipped on the chest
 */
public fun interface DisablesCape : ItemExtension {
    public fun disablesCape(itemStack: ItemStack, player: AbstractClientPlayer): Boolean
}

@OptIn(CynosureInternal::class)
public var Item.burnTime: Int
    get() = CynosureHooks.getItemBurnTime(defaultInstance)
    set(value) { CynosureHooks.registerBurnTime(this, value) }