package com.github.hedgehogsaga.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpriteButton extends ButtonWidget {
    private final Identifier texture;
    private final int u, v, hoverV;

    public SpriteButton(int x, int y, Identifier texture, int u, int v, int hoverV, Text tooltip, PressAction onPress) {
        super(x, y, 20, 18, tooltip, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.hoverV = hoverV;
        setTooltip(Tooltip.of(tooltip));
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.drawTexture(texture, getX(), getY(), u, isSelected() ? hoverV : v, 20, 18);
    }
}
