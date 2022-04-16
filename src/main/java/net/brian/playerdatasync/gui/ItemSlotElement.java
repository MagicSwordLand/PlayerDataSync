package net.brian.playerdatasync.gui;

import javafx.util.Pair;
import net.brian.playerdatasync.PlayerDataSync;
import net.brian.playerdatasync.util.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

public class ItemSlotElement extends GuiElement{


    HashMap<HumanEntity,ItemStack> items = new HashMap<>();
    CanPutFunc canPut;

    ItemStack emptySlotItem;
    public ItemSlotElement(char slotChar, CanPutFunc canPut, Material material, int model, String... text) {
        super(slotChar);
        this.canPut = canPut;
        emptySlotItem = new ItemStack(material);
        ItemMeta meta = emptySlotItem.getItemMeta();
        if(text.length > 0){
            meta.setDisplayName(IridiumColorAPI.process(text[0]));
        }
        if(text.length > 1){
            meta.setLore(IridiumColorAPI.process(Arrays.asList(Arrays.copyOfRange(text,1,text.length))));
        }
        emptySlotItem.setItemMeta(meta);

        setAction(click -> {
            InventoryAction action = click.getEvent().getAction();
            HumanEntity humanEntity = click.getWhoClicked();
            ItemStack cursor = click.getEvent().getCursor();
            ItemStack currentItem = click.getEvent().getCurrentItem();
            Inventory inv = click.getEvent().getView().getTopInventory();
            humanEntity.sendMessage(action.name());
            switch (action){
                case PLACE_ONE:
                case PLACE_ALL:
                    return put(humanEntity,currentItem,inv, click.getSlot());
                case PICKUP_ALL:
                    if(isEmpty(currentItem)){
                        return true;
                    }
                    if(!isNull(currentItem)){
                        return take(humanEntity,inv,click.getSlot());
                    }
                case SWAP_WITH_CURSOR:
                    if(isEmpty(currentItem)){
                        return put(humanEntity,cursor,inv, click.getSlot());
                    }
                    else{
                        return swap(humanEntity,cursor);
                    }
                default: return true;

            }
        });
    }

    boolean take(HumanEntity humanEntity,Inventory inv,int slot){
        Bukkit.getScheduler().runTaskLater(PlayerDataSync.getInstance(),()->{
            if(isEmpty(inv.getItem(slot))){
                inv.setItem(slot,emptySlotItem);
                items.remove(humanEntity);
            }
        },1L);

        return false;
    }

    //Can put returns false
    boolean swap(HumanEntity humanEntity,ItemStack cursor){
        if(canPut.canPut(humanEntity,cursor)){
            items.put(humanEntity,cursor);
            return false;
        }
        return true;
    }

    //Can put returns false
    boolean put(HumanEntity humanEntity, ItemStack itemStack, Inventory inv,int slot){
        if(canPut.canPut(humanEntity,itemStack)){
            inv.setItem(slot,new ItemStack(Material.AIR));
            items.put(humanEntity,itemStack);
            //Can put returns false
            return false;
        }
        return true;
    }

    boolean isEmpty(ItemStack itemStack){
        return itemStack == null || itemStack.isSimilar(emptySlotItem);
    }

    @Override
    public ItemStack getItem(HumanEntity who, int slot) {
        return items.getOrDefault(who,emptySlotItem);
    }

    boolean isNull(ItemStack itemStack){
        return itemStack == null || itemStack.getType().equals(Material.AIR);
    }

    public interface CanPutFunc{
        boolean canPut(HumanEntity humanEntity,ItemStack itemStack);
    }
}
