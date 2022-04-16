package net.brian.playerdatasync.data;

import java.util.UUID;

public abstract class PlayerData {
    protected UUID uuid;

    public PlayerData(UUID uuid){
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
