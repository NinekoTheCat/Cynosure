package dev.mayaqq.cynosure.datagen

import dev.mayaqq.cynosure.events.datagen.CynosureDataGenerator
import dev.mayaqq.cynosure.events.datagen.DataGenerationRegistrationEvent
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

public class AbstractCynosureDataGeneratorEntrypoint : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        // TODO: Fix this shit
        DataGenerationRegistrationEvent(CynosureDataGenerator(fabricDataGenerator,fabricDataGenerator.createPack()))
    }

}
