package com.github.hedgehogsaga.block;

import com.github.hedgehogsaga.entity.PushMarbleEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PushMarbleBlock extends Block {
    public PushMarbleBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) PushMarbleEntity.fromBlock(world, pos).clickPush(player);
        return ActionResult.success(world.isClient);
    }
}
