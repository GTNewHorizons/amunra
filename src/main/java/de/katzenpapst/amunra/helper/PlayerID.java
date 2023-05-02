package de.katzenpapst.amunra.helper;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class PlayerID {

    protected UUID userUUID;
    protected String userName;

    public PlayerID(final UUID userUUID, final String userName) {
        this.userUUID = userUUID;
        this.userName = userName;
    }

    public PlayerID(final EntityPlayer player) {
        this.userUUID = player.getUniqueID();
        this.userName = player.getDisplayName();
    }

    public PlayerID(final NBTTagCompound nbt) {
        final String uuid = nbt.getString("uuid");
        this.userUUID = UUID.fromString(uuid);
        this.userName = nbt.getString("name");
    }

    public UUID getUUID() {
        return userUUID;
    }

    public String getName() {
        return userName;
    }

    public NBTTagCompound getNbt() {
        final NBTTagCompound nbt = new NBTTagCompound();

        nbt.setString("uuid", userUUID.toString());
        nbt.setString("name", userName);

        return nbt;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PlayerID)) {
            return false;
        }
        return ((PlayerID) other).userUUID.equals(userUUID);
    }

    @Override
    public int hashCode() {
        return userUUID.hashCode();
    }

    public boolean isSameUser(final EntityPlayer player) {
        return player == null ? false : this.userUUID.equals(player.getUniqueID());
    }

}
