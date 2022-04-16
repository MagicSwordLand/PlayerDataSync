package net.brian.playerdatasync.test;

import net.brian.playerdatasync.PlayerDataSync;
import net.brian.playerdatasync.gui.GuiElement;
import net.brian.playerdatasync.gui.InventoryGui;
import net.brian.playerdatasync.gui.ItemSlotElement;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class TestGui extends InventoryGui {
    public TestGui() {
        super(PlayerDataSync.getInstance(),"www",new String[]{
                "ab       "
        });
        setFiller(new ItemStack(Material.SPRUCE_SIGN));
        addElement(new ItemSlotElement('b',((humanEntity, itemStack) -> true), Material.AIR,0));
        addElement(new ItemSlotElement('a',((humanEntity, itemStack) -> true), Material.DIRT,0,"lol","www"));
    }
}
