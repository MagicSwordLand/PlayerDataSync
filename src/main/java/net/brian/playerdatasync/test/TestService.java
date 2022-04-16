package net.brian.playerdatasync.test;

import net.brian.playerdatasync.PlayerDataSync;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.junit.Test;

public class TestService implements Listener {

    TestGui testGui;
    public TestService(){
        //Bukkit.getPluginManager().registerEvents(this,PlayerDataSync.getInstance());
        testGui = new TestGui();
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event){
        testGui.show(event.getPlayer());
    }
}
