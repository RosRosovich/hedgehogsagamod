package com.github.hedgehogsaga.block;

import com.github.hedgehogsaga.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class GreenHillBushBlock extends PlantBlock {

    public static final MapCodec<GreenHillBushBlock> CODEC =
            createCodec(GreenHillBushBlock::new);

    public GreenHillBushBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(ModBlocks.GREEN_HILL_GRASS_BLOCK)
                || floor.isOf(ModBlocks.GREEN_HILL_DIRT)
                || floor.isOf(ModBlocks.MARBLE_TILES_GRASS_BLOCK)
                || floor.isOf(ModBlocks.MARBLE_BRICKS_GRASS_BLOCK)
                || floor.isOf(ModBlocks.POLISHED_MARBLE_GRASS_BLOCK)
                || super.canPlantOnTop(floor, world, pos);
    }
}