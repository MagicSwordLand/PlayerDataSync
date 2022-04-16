package net.brian.playerdatasync.util.nms;

import com.mojang.authlib.GameProfile;
import net.brian.playerdatasync.util.ItemStackSerializer;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.lang.reflect.Method;

public class Version_1_16_R3 implements ItemStackSerializer {

    private static Method WRITE_NBT;
    private static Method READ_NBT;

    public Version_1_16_R3() {

    }

    public String toBase64(ItemStack itemStack) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(arrayOutputStream);
        CraftItemStack craftItemStack = getCraftVersion(itemStack);
        NBTTagCompound tagCompound = new NBTTagCompound();
        if (craftItemStack != null) {
            try {
                CraftItemStack.asNMSCopy(craftItemStack).save(tagCompound);
            } catch (NullPointerException ignored) {
            }
        }

        if (WRITE_NBT == null) {
            try {
                WRITE_NBT = NBTCompressedStreamTools.class.getDeclaredMethod("a", NBTBase.class, DataOutput.class);
                WRITE_NBT.setAccessible(true);
            } catch (Exception var10) {
                throw new IllegalStateException("Unable to find private write method.", var10);
            }
        }

        try {
            WRITE_NBT.invoke(null, tagCompound, dataOutputStream);
        } catch (Exception var9) {
            throw new IllegalArgumentException("Unable to write " + tagCompound + " to " + dataOutputStream, var9);
        }

        return Base64Coder.encodeLines(arrayOutputStream.toByteArray());
    }

    public ItemStack fromBase64(String var1) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(var1));
        NBTTagCompound nbtTagCompound = (NBTTagCompound) readNbt(new DataInputStream(byteArrayInputStream));
        return CraftItemStack.asCraftMirror(net.minecraft.server.v1_16_R3.ItemStack.a(nbtTagCompound));
    }

    private static NBTBase readNbt(DataInput var0) {
        if (READ_NBT == null) {
            try {
                READ_NBT = NBTCompressedStreamTools.class.getDeclaredMethod("a", DataInput.class, Integer.TYPE, NBTReadLimiter.class);
                READ_NBT.setAccessible(true);
            } catch (Exception var3) {
                throw new IllegalStateException("Unable to find private read method.", var3);
            }
        }

        try {
            return (NBTBase)READ_NBT.invoke(null, var0, 0, new NBTReadLimiter(9223372036854775807L));
        } catch (Exception var2) {
            throw new IllegalArgumentException("Unable to read from " + var0, var2);
        }
    }

    private static CraftItemStack getCraftVersion(ItemStack var0) {
        if (var0 instanceof CraftItemStack) {
            return (CraftItemStack)var0;
        } else if (var0 != null) {
            return CraftItemStack.asCraftCopy(var0);
        } else {
            return null;
        }
    }

    public Player loadPlayer(OfflinePlayer var1) {
        if (var1 != null && var1.hasPlayedBefore()) {
            GameProfile var2 = new GameProfile(var1.getUniqueId(), var1.getName());
            DedicatedServer var3 = ((CraftServer) Bukkit.getServer()).getServer();
            EntityPlayer var4 = new EntityPlayer(var3, var3.getWorldServer(World.OVERWORLD), var2, new PlayerInteractManager(var3.getWorldServer(World.OVERWORLD)));
            CraftPlayer var5 = var4.getBukkitEntity();
            if (var5 != null) {
                var5.loadData();
            }

            return var5;
        } else {
            return null;
        }
    }
}