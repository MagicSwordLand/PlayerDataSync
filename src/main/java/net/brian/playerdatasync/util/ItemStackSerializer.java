package net.brian.playerdatasync.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface ItemStackSerializer {
    String toBase64(ItemStack var1) throws IOException;

    ItemStack fromBase64(String var1) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    Player loadPlayer(OfflinePlayer var1);
}
