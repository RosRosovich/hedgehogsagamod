package com.github.hedgehogsaga.client;

import com.github.hedgehogsaga.ChaosEmeralds;
import com.github.hedgehogsaga.HedgehogSagaMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CharacterScreen extends Screen {
    static final Identifier TEX = Identifier.of(HedgehogSagaMod.MOD_ID, "textures/gui/character_select.png");
    static final int W = 176, H = 166;

    public enum Shield { BASIC, LIGHTNING, BUBBLE, FLAME }

    private int left, top;

    public CharacterScreen() {
        super(Text.translatable("gui.hedgehogsaga.character"));
    }

    static Shield currentShield() { return null; }
    static int superFormUses() { return 0; }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        addDrawableChild(new SpriteButton(left + 126, top + 61, TEX, 222, 62, 80,
                Text.translatable("gui.hedgehogsaga.back"), b -> client.setScreen(new InventoryScreen(client.player))));
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawTexture(TEX, left, top, 0, 0, W, H);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        List<Text> tooltip = null;

        int collected = (int) ChaosEmeralds.ALL.stream().filter(e -> ChaosEmeralds.has(client.player, e)).count();
        for (int i = 0; i < ChaosEmeralds.ALL.size(); i++) {
            Item emerald = ChaosEmeralds.ALL.get(i);
            int x = left + 25 + 18 * i, y = top + 12;
            boolean has = ChaosEmeralds.has(client.player, emerald);
            if (has) ctx.drawItem(new ItemStack(emerald), x, y);
            else ctx.drawTexture(TEX, x, y, 176, 16, 16, 16);
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                tooltip = new ArrayList<>();
                tooltip.add(has ? emerald.getName() : Text.translatable("gui.hedgehogsaga.emerald_missing").formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("gui.hedgehogsaga.emeralds", collected).formatted(Formatting.GRAY));
                if (collected == ChaosEmeralds.ALL.size()) {
                    tooltip.add(Text.translatable("gui.hedgehogsaga.super_uses", superFormUses()).formatted(Formatting.GOLD));
                }
            }
        }

        InventoryScreen.drawEntity(ctx, left + 61, top + 30, left + 113, top + 100, 30, 0.0625f, mouseX, mouseY, client.player);
        if (inside(mouseX, mouseY, left + 61, top + 30, 52, 70)) {
            tooltip = List.of(Text.translatable("gui.hedgehogsaga.sonic").formatted(Formatting.BLUE),
                    Text.translatable("gui.hedgehogsaga.abilities").formatted(Formatting.GRAY));
        }

        Shield shield = currentShield();
        if (shield != null) ctx.drawTexture(TEX, left + 79, top + 102, 176 + 16 * shield.ordinal(), 32, 16, 16);
        if (inside(mouseX, mouseY, left + 79, top + 102, 16, 16)) {
            tooltip = List.of(Text.translatable(shield == null ? "gui.hedgehogsaga.shield.none"
                    : "gui.hedgehogsaga.shield." + shield.name().toLowerCase()));
        }

        ctx.drawTexture(TEX, left + 61, top + 104, 176, 113, 16, 14);
        ctx.drawTexture(TEX, left + 97, top + 104, 176, 97, 16, 14);
        if (inside(mouseX, mouseY, left + 61, top + 104, 16, 14) || inside(mouseX, mouseY, left + 97, top + 104, 16, 14)) {
            tooltip = List.of(Text.translatable("gui.hedgehogsaga.locked"));
        }

        if (tooltip != null) ctx.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
