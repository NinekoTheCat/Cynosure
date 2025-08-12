package dev.mayaqq.cynosure.events.datagen

import dev.mayaqq.cynosure.events.api.Event
import dev.mayaqq.cynosure.mixin.DataGenerationMixin
import net.minecraft.data.DataGenerator
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput

public  class DataGenerationRegistrationEvent(public val generator: CynosureDataGenerator) : Event

public  class CynosureDataGenerator(public val innerDataGenerator: DataGenerator, public val output: PackOutput) {
    public fun <T:DataProvider> addProvider (provider: T) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (innerDataGenerator as DataGenerationMixin).`cynosure$addProvider`(provider)
    }
    public fun <T: DataProvider> addProvider(factory: (PackOutput) -> T): T {
        val prov = factory.invoke(output)
        this.addProvider(provider  = prov)
        return prov
    }

}