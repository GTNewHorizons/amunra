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
        return this.userUUID;
    }

    public String getName() {
        return this.userName;
    }

    public NBTTagCompound getNbt() {
        final NBTTagCompound nbt = new NBTTagCompound();

        nbt.setString("uuid", this.userUUID.toString());
        nbt.setString("name", this.userName);

        return nbt;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PlayerID playerID)) {
            return false;
        }
        return playerID.userUUID.equals(this.userUUID);
    }

    @Override
    public int hashCode() {
        return this.userUUID.hashCode();
    }

    public boolean isSameUser(final EntityPlayer player) {
        return player != null && this.userUUID.equals(player.getUniqueID());
    }

}
