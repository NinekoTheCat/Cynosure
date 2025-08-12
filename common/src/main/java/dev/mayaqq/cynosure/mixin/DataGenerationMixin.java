package dev.mayaqq.cynosure.mixin;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.Set;

/**
 * Mixin to allow us to add our own generators on any platform
 */
@Mixin(DataGenerator.class)
public class DataGenerationMixin {
    @Shadow @Final
    Map<String, DataProvider> providersToRun;
    @Shadow @Final
    Set<String> allProviderIds;

    /**
     * adds a provider to the data generator
     * DO NOT USE OUTSIDE OF CYNOSURE!
     * @param provider to add
     * @return the provider which was added
     * @param <T> the provider's type
     */
    @Unique
    @SuppressWarnings("unused")
    public <T extends DataProvider> T cynosure$addProvider(@NotNull T provider) {
        String id = provider.getName();
        if (!allProviderIds.add(id))
            throw new IllegalStateException("Failed to add provider with the name of "+id+", because provider already exists");
        providersToRun.put(id,provider);
        return provider;
    }
}
