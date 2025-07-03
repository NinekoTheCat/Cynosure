package dev.mayaqq.cynosure.blocks.poi

import com.google.common.collect.ImmutableSet
import dev.mayaqq.cynosure.mixin.accessor.PoiTypesInvoker
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.ai.village.poi.PoiManager
import net.minecraft.world.entity.ai.village.poi.PoiRecord
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

public object PoiHelpers {
    public fun getBlockStates(block: Block): MutableSet<BlockState?> {
        return ImmutableSet.copyOf(block.stateDefinition.possibleStates)
    }

    public fun poi(block: Block): PoiType = PoiType(getBlockStates(block), 0, 1)

    public fun poiFactory(block: Block): () -> PoiType = { poi(block) }

    public fun registerState(poi: PoiType) {
        PoiTypesInvoker.registerBlockStates(poi.holder()?: return, poi.matchingStates)
    }

    public fun PoiType.key(): ResourceKey<PoiType>? = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getResourceKey(this).getOrNull()
    public fun ResourceKey<PoiType>.holder(): Holder<PoiType>? = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolder(this).getOrNull()
    public fun PoiType.holder(): Holder<PoiType>? = this.key()?.holder()
    public fun PoiManager.inRange(poi: PoiType, pos: BlockPos, range: Int): Stream<PoiRecord> {
        return this.getInRange({it.`is`(poi.key()?: return@getInRange false)}, pos, range, PoiManager.Occupancy.ANY)
    }
    public fun PoiManager.anyInRange(poi: PoiType, pos: BlockPos, range: Int): Boolean = this.inRange(poi, pos, range).toList().isNotEmpty()
}