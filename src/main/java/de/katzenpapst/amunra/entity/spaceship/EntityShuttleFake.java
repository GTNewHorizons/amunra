package de.katzenpapst.amunra.entity.spaceship;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.ShuttleTeleportHelper;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR.EnumSimplePacket;
import micdoodle8.mods.galacticraft.core.entities.EntityCelestialFake;

/**
 * This is my version of EntityCelestialFake, for special stuff
 *
 * @author katzenpapst
 *
 */
public class EntityShuttleFake extends EntityCelestialFake {

    private String cachedDimList = null;

    public EntityShuttleFake(final World world) {
        super(world);
    }

    public EntityShuttleFake(final World world, final float yOffset) {
        super(world, yOffset);
    }

    public EntityShuttleFake(final EntityPlayerMP player, final float yOffset) {
        super(player, yOffset);
    }

    public EntityShuttleFake(final World world, final double x, final double y, final double z, final float yOffset) {
        super(world, x, y, z, yOffset);
    }

    @Override
    public void onUpdate() {
        // stuff
        if ((!this.worldObj.isRemote && this.ticks % 40 == 0)
                && (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayerMP player)) {
            if (this.ticks % 160 == 0 || this.cachedDimList == null) {
                this.cachedDimList = this.getDimList(player);
            }

            AmunRa.packetPipeline.sendTo(
                    new PacketSimpleAR(
                            EnumSimplePacket.C_OPEN_SHUTTLE_GUI,
                            player.getGameProfile().getName(),
                            this.cachedDimList),
                    player);
        }
        super.onUpdate();
    }

    private String getDimList(final EntityPlayerMP player) {
        final Map<String, Integer> map = ShuttleTeleportHelper.getArrayOfPossibleDimensions(player);
        String dimensionList = "";
        int count = 0;
        for (final Entry<String, Integer> entry : map.entrySet()) {
            dimensionList = dimensionList.concat(entry.getKey() + (count < map.entrySet().size() - 1 ? "?" : ""));
            count++;
        }
        return dimensionList;
    }
}
