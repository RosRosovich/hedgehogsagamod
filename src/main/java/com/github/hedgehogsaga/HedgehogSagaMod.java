package com.github.hedgehogsaga;

import com.github.hedgehogsaga.block.GreenHillInteractionHandler;
import com.github.hedgehogsaga.block.PalmCoreInteractionHandler;
import com.github.hedgehogsaga.block.ThornyPalmCoreBlock;
import com.github.hedgehogsaga.entity.PushMarbleEntity;
import net.fabricmc.api.ModInitializer;

public class HedgehogSagaMod implements ModInitializer {
    public static final String MOD_ID = "hedgehogsaga";

    @Override
    public void onInitialize() {
        ModBlocks.registerAll();
        ModItems.registerAll();
        PushMarbleEntity.register();
        ChaosEmeralds.register();
        ModFeatures.registerAll();
        ModBiomes.registerAll();
        ModComposter.register();
        ModFlammableBlocks.register();
        ModFuelRegistry.register();
        ModTrades.register();
        GreenHillInteractionHandler.register();
        ThornyPalmCoreBlock.registerDamageHandler();
        PalmCoreInteractionHandler.register();
    }
}
