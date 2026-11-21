package com.github.hedgehogsaga.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class FlamethrowerBlock extends Block {
    private static final int HEIGHT = 3, RATE = 2;

    public FlamethrowerBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos from, boolean notify) {
        if (world.isReceivingRedstonePower(pos)) world.scheduleBlockTick(pos, this, 1);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.isReceivingRedstonePower(pos)) return;
        for (int i = 0; i < 6; i++) {
            world.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                    0, (random.nextDouble() - 0.5) * 0.05, 0.35 + random.nextDouble() * 0.1, (random.nextDouble() - 0.5) * 0.05, 1);
        }
        if (random.nextInt(10) == 0) world.playSound(null, pos, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 0.5f, 1f);
        Box jet = new Box(pos.up()).stretch(0, HEIGHT - 1, 0).contract(0.2, 0, 0.2);
        for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, jet, e -> !e.isFireImmune())) {
            e.setOnFireFor(4);
            e.damage(world.getDamageSources().inFire(), 2f);
        }
        world.scheduleBlockTick(pos, this, RATE);
    }
}
