package com.github.hedgehogsaga;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public class ModBiomes {

    public static final RegistryKey<Biome> GREEN_HILL_BIOME =
            RegistryKey.of(RegistryKeys.BIOME,
                    Identifier.of(HedgehogSagaMod.MOD_ID, "green_hill_biome"));

    public static void registerAll() {
    }
}