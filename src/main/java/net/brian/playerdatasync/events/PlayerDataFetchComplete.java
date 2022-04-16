package net.brian.playerdatasync.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerDataFetchComplete extends Event {

    private final Player player;
    private final HandlerList handlerList = new HandlerList();

    public PlayerDataFetchComplete(Player player){
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public Player getPlayer() {
        return player;
    }
}
