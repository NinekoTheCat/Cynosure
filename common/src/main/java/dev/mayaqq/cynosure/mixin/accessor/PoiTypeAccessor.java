package dev.mayaqq.cynosure.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(PoiType.class)
public interface PoiTypeAccessor {
    @Accessor("matchingStates")
    @Mutable
    void cynosure$setBlockStates(Set<BlockState> states);
}
