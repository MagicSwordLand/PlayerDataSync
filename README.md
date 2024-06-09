# PlayerDataSync
Saving and syncing PlayerData between server replicas has never been so easy.  
This plugin automatically handles data races and uses gson to serialize player data.  


## QuickStart
So as example, you have a StatusData class which each player has one.

```java
public class StatusData extends PlayerData{
    private String status;
    public String getStatus(){
        return status;
    }
}
```
Then you register it by 

```java
@Override
public void onEnable(){
    PlayerDataSync.getinstance().register("status",StatusData.class);
}
```
After the player joins the server replica, you can fetch the player's data by. 
```java
PlayerDataSync.getinstance().getData(playerUuid,StatusData.class);
```

That's all, pretty simple.
