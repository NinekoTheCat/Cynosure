package dev.mayaqq.cynosure.blocks.poi

import com.google.common.collect.ImmutableSet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.entity.ai.village.poi.PoiTypes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import kotlin.jvm.optionals.getOrNull

public object PoiHelpers {
    public fun getBlockStates(block: Block): MutableSet<BlockState?> {
        return ImmutableSet.copyOf(block.stateDefinition.possibleStates)
    }

    public fun poi(block: Block): PoiType = PoiType(getBlockStates(block), 0, 1)

    public fun poiFactory(block: Block): () -> PoiType = { poi(block) }

    public fun registerState(poi: PoiType) {
        BuiltInRegistries.POINT_OF_INTEREST_TYPE.getResourceKey(poi).getOrNull()?.let { key ->
            PoiTypes.registerBlockStates(
                BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolderOrThrow(key), poi.matchingStates
            )
        }
    }
}