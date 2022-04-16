package net.brian.playerdatasync.data.gson.adapter;

import com.google.gson.*;
import com.mojang.authlib.BaseUserAuthentication;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        World world = Bukkit.getWorld(jsonObject.get("world").getAsString());
        return new Location(world,jsonObject.get("x").getAsDouble(),jsonObject.get("y").getAsDouble(),jsonObject.get("z").getAsDouble());
    }

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject json = new JsonObject();
        json.addProperty("world",location.getWorld().getName());
        json.addProperty("x",location.getX());
        json.addProperty("y",location.getY());
        json.addProperty("z",location.getZ());
        return json.getAsJsonPrimitive();
    }
}
