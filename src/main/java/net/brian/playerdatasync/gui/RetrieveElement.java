package net.brian.playerdatasync.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RetrieveElement extends StaticGuiElement {

    ItemStack origin,reward;

    public RetrieveElement(char slotChar, ItemStack item,Action action,ItemStack reward, String... text) {
        super(slotChar, item, click -> true, text);
        origin = getRawItem();
        this.reward = reward;
        setItem(reward);
        setAction(click -> {
            if(action.onClick(click)){
                ItemStack cursorItem = click.getEvent().getView().getCursor();
                if(cursorItem == null || cursorItem.getType().equals(Material.AIR)){
                    click.getEvent().getView().setCursor(this.reward);
                    setItem(origin);
                    this.reward = origin;
                    gui.draw();
                }
                /*
                if(click.getWhoClicked().getInventory().addItem(this.reward).isEmpty()){
                    setItem(origin);
                    this.reward = origin;
                    gui.draw();
                }
                else {
                    click.getWhoClicked().sendMessage("你的包包已滿");
                }
                 */
            }
            return true;
        });
    }

    public void setReward(ItemStack itemStack){
        this.reward = itemStack;
        setItem(reward);
        gui.draw();
    }

    public ItemStack getReward(){
        return reward;
    }

}
