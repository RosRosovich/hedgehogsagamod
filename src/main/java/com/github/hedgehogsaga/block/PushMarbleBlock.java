package com.github.hedgehogsaga.block;

import com.github.hedgehogsaga.entity.PushMarbleEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PushMarbleBlock extends Block {
    public PushMarbleBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!PushMarbleEntity.isShovel(stack)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!world.isClient) {
            var dir = PushMarbleEntity.awayFromHit(hit.getPos().x - (pos.getX() + 0.5), hit.getPos().z - (pos.getZ() + 0.5));
            PushMarbleEntity.fromBlock(world, pos).shovelPush(player, dir);
        }
        return ItemActionResult.success(world.isClient);
    }
}
