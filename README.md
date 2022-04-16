# PlayerDataSync

This plugin helps handle player data that needs to be stored in the database<br>
To use this plugin to develope ,you have to depend PlayerDataSync.<br>
Then you register the table name and playerdata class onEnable.

The plugin uses gson to serialize objects. Be sure to understand how it works.<br>
There's a interface PostProccessable , it might be useful.

For example you have an status data class<br>

```java
public class StatusData extends PlayerData{
    private String status;
    public String getStatus(){
        return status;
    }
}
```
Then you register it by <br>

```java
@Override
public void onEnable(){
    PlayerDataSync.getinstance().register("status",StatusData.class);
}
```

You fetch players data by <br>
```java
PlayerDataSync.getinstance().getData(uuid,StatusData.class);
```
That's all, pretty simple.
