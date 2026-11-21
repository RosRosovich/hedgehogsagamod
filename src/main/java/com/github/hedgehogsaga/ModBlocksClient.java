package com.github.hedgehogsaga;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import com.github.hedgehogsaga.client.CharacterScreen;
import com.github.hedgehogsaga.client.SpriteButton;
import com.github.hedgehogsaga.entity.PushMarbleEntity;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
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
        BlockRenderLayerMap.INSTANCE.putBlock(Registries.BLOCK.get(Identifier.of(HedgehogSagaMod.MOD_ID, "hotbed")), RenderLayer.getCutout());

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

        Identifier buttons = Identifier.of(HedgehogSagaMod.MOD_ID, "textures/gui/inventory_buttons.png");
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            Text label = Text.translatable("gui.hedgehogsaga.character");
            if (screen instanceof InventoryScreen) {
                ButtonWidget recipe = Screens.getButtons(screen).stream()
                        .filter(b -> b instanceof TexturedButtonWidget).map(b -> (ButtonWidget) b).findFirst().orElse(null);
                Screens.getButtons(screen).add(new SpriteButton((w - 176) / 2 + 126, h / 2 - 22, buttons, 199, 0, 18, label,
                        b -> client.setScreen(new CharacterScreen())) {
                    @Override
                    protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
                        if (recipe != null) setPosition(recipe.getX() + 22, recipe.getY());
                        super.renderWidget(ctx, mx, my, delta);
                    }
                });
            } else if (screen instanceof CreativeInventoryScreen) {
                Screens.getButtons(screen).add(new SpriteButton((w - 195) / 2 + 197, (h - 136) / 2, buttons, 199, 0, 18, label,
                        b -> client.setScreen(new CharacterScreen())));
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
