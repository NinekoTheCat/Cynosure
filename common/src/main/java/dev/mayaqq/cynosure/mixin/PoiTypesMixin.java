package dev.mayaqq.cynosure.mixin;

import dev.mayaqq.cynosure.blocks.poi.PoiTypeRegistryAccessorKt;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(PoiTypes.class)
public class PoiTypesMixin {

    @Mutable @Shadow @Final private static Set<BlockState> BEDS;

    @Inject(method = "<clinit>",
            at = @At(
                value = "FIELD",
                target = "Lnet/minecraft/world/entity/ai/village/poi/PoiTypes;BEDS:Ljava/util/Set;",
                opcode = Opcodes.PUTSTATIC,
                shift = At.Shift.AFTER
            )
    )
    private static void modifyBeds(CallbackInfo ci) {
        Set<BlockState> newBeds = new HashSet<>(BEDS);
        PoiTypeRegistryAccessorKt.getBeds().forEach(block -> {
            newBeds.addAll(block.getStateDefinition()
                    .getPossibleStates()
                    .stream()
                    .filter(state -> state.getValue(BedBlock.PART) == BedPart.HEAD)
                    .collect(Collectors.toCollection(HashSet::new)));
        });
        BEDS = newBeds;
	}
}
