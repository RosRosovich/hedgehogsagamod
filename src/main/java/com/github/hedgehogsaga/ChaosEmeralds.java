package com.github.hedgehogsaga;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChaosEmeralds {
    public static final List<Item> ALL = new ArrayList<>();

    public static void register() {
        for (String color : List.of("green", "red", "blue", "yellow", "cyan", "purple", "white")) {
            ALL.add(Registry.register(Registries.ITEM, Identifier.of(HedgehogSagaMod.MOD_ID, color + "_chaos_emerald"),
                    new Item(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof())));
        }
        ItemGroupEvents.modifyEntriesEvent(ModBlocks.ITEM_GROUP_KEY).register(entries -> ALL.forEach(entries::add));
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerManager().getPlayerList().forEach(ChaosEmeralds::removeDuplicates));
    }

    static void removeDuplicates(PlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        Set<Item> seen = new HashSet<>();
        for (int i = 0; i < inv.size(); i++) {
            Item item = inv.getStack(i).getItem();
            if (ALL.contains(item) && !seen.add(item)) inv.setStack(i, ItemStack.EMPTY);
        }
    }

    public static boolean has(PlayerEntity player, Item emerald) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isOf(emerald)) return true;
        }
        return false;
    }
}
