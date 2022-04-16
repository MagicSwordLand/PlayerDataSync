package net.brian.playerdatasync.data.gson.adapter;

import com.google.gson.*;
import net.brian.playerdatasync.util.ItemStackSerializer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
    private final ItemStackSerializer itemStackSerializer;

    public ItemStackAdapter(ItemStackSerializer itemStackSerializer){
        this.itemStackSerializer = itemStackSerializer;
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            return itemStackSerializer.fromBase64(jsonElement.getAsString());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext jsonSerializationContext) {
        try {
            return new JsonPrimitive(itemStackSerializer.toBase64(itemStack));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
