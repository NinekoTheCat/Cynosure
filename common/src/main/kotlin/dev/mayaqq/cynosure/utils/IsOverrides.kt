package dev.mayaqq.cynosure.utils

import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Predicate

public operator fun BlockState.rem(block: Block): Boolean = this.`is`(block)
public operator fun BlockState.rem(holder: HolderSet<Block>): Boolean = this.`is`(holder)
public operator fun BlockState.rem(tag: TagKey<Block>): Boolean = this.`is`(tag)

public operator fun ItemStack.rem(item: Item): Boolean = this.`is`(item)
public operator fun ItemStack.rem(holder: Holder<Item>): Boolean = this.`is`(holder)
public operator fun ItemStack.rem(tag: TagKey<Item>): Boolean = this.`is`(tag)
public operator fun ItemStack.rem(predicate: Predicate<Holder<Item>>): Boolean = this.`is`(predicate)

public operator fun Entity.rem(entity: Entity): Boolean = this.`is`(entity)
public operator fun EntityType<*>.rem(tag: TagKey<EntityType<*>>): Boolean = this.`is`(tag)
public operator fun Entity.rem(type: EntityType<*>): Boolean = this.type == type

public operator fun Holder<*>.rem(id: ResourceLocation): Boolean = this.`is`(id)
public operator fun <T> Holder<T>.rem(key: ResourceKey<T>): Boolean = this.`is`(key)
public operator fun <T> Holder<T>.rem(predicate: Predicate<ResourceKey<T>>): Boolean = this.`is`(predicate)
public operator fun <T> Holder<T>.rem(tag: TagKey<T>): Boolean = this.`is`(tag)

public operator fun MobEffectInstance.rem(effect: MobEffect): Boolean = this.effect == effect

public operator fun DamageSource.rem(key: ResourceKey<DamageType>): Boolean = this.`is`(key)
public operator fun DamageSource.rem(tag: TagKey<DamageType>): Boolean = this.`is`(tag)

// More to come, holy shit there is a lot lol