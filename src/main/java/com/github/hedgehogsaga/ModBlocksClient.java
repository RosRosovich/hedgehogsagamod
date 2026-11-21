package com.github.hedgehogsaga;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import com.github.hedgehogsaga.entity.PushMarbleEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class ModBlocksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GREEN_HILL_GRASS, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CLOVER_FLOWER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GREEN_HILL_SUNFLOWER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GREEN_HILL_BUSH, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TOTEM_WINGS, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LAVA_GLASS, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PALM_LOG, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.THORNY_PALM_CORE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PALM_LEAVES, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PALM_SAPLING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WOODEN_SPIKE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.METAL_SPIKE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PALM_FENCE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PALM_DOOR, RenderLayer.getCutout());

        EntityRendererRegistry.register(PushMarbleEntity.TYPE, ctx -> new EntityRenderer<PushMarbleEntity>(ctx) {
            @Override
            public void render(PushMarbleEntity e, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
                matrices.push();
                matrices.translate(-0.49, 0, -0.49);
                matrices.scale(0.98f, 0.98f, 0.98f);
                ctx.getBlockRenderManager().renderBlockAsEntity(ModBlocks.GREEN_PUSH_MARBLE.getDefaultState(), matrices, vcp, light, OverlayTexture.DEFAULT_UV);
                matrices.pop();
            }

            @Override
            public Identifier getTexture(PushMarbleEntity e) {
                return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity p = client.player;
            if (p == null || !p.horizontalCollision || p.input.movementForward <= 0) return;
            Direction dir = p.getHorizontalFacing();
            if (PushMarbleEntity.walkPush(p, dir)) {
                ClientPlayNetworking.send(new PushMarbleEntity.WalkPush(dir.getHorizontal()));
            }
        });
    }
}
