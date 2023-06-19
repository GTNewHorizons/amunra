package de.katzenpapst.amunra.world;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

import cpw.mods.fml.common.FMLCommonHandler;
import de.katzenpapst.amunra.tick.TickHandlerServer;
import de.katzenpapst.amunra.tile.TileEntityShuttleDock;
import de.katzenpapst.amunra.vec.Vector3int;

public class ShuttleDockHandler extends WorldSavedData {

    public static final String saveDataID = "ShuttleDock";

    // map: dimensionID => (map: position => isAvailable)
    private static final Map<Integer, Map<Vector3int, Boolean>> tileMap = new HashMap<>();

    public ShuttleDockHandler(final String id) {
        super(id);
    }

    protected static void markInstanceDirty() {
        TickHandlerServer.dockData.setDirty(true);
    }

    @Override
    public void readFromNBT(NBTTagCompound p_76184_1_) {
        // this should be a list of lists now
        final NBTTagList tagList = p_76184_1_.getTagList("DockList", NBT.TAG_COMPOUND);
        tileMap.clear();

        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound dimensionNbt = tagList.getCompoundTagAt(i);

            final int dimID = dimensionNbt.getInteger("DimID");
            final NBTTagList posList = dimensionNbt.getTagList("PosList", NBT.TAG_COMPOUND);

            final Map<Vector3int, Boolean> curList = new HashMap<>();

            for (int j = 0; j < posList.tagCount(); j++) {
                final NBTTagCompound posTag = posList.getCompoundTagAt(j);
                final int posX = posTag.getInteger("PosX");
                final int posY = posTag.getInteger("PosY");
                final int posZ = posTag.getInteger("PosZ");
                final boolean available = posTag.getBoolean("isAvailable");
                final Vector3int pos = new Vector3int(posX, posY, posZ);

                curList.put(pos, available);
            }
            tileMap.put(dimID, curList);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound p_76187_1_) {

        final NBTTagList totalNbtList = new NBTTagList();

        for (final int dimID : tileMap.keySet()) {

            final NBTTagCompound dimTag = new NBTTagCompound();
            dimTag.setInteger("DimID", dimID);

            final Map<Vector3int, Boolean> curList = tileMap.get(dimID);
            final NBTTagList posNbtList = new NBTTagList();
            for (final Vector3int pos : curList.keySet()) {
                final boolean avail = curList.get(pos);
                final NBTTagCompound posTag = new NBTTagCompound();

                posTag.setInteger("PosX", pos.x);
                posTag.setInteger("PosY", pos.y);
                posTag.setInteger("PosZ", pos.z);
                posTag.setBoolean("isAvailable", avail);

                posNbtList.appendTag(posTag);
            }
            dimTag.setTag("PosList", posNbtList);
            totalNbtList.appendTag(dimTag);
        }

        p_76187_1_.setTag("DockList", totalNbtList);
    }

    public static boolean getStoredAvailability(final TileEntityShuttleDock dock) {
        if (!dock.getWorldObj().isRemote) {
            final int dimID = dock.getWorldObj().provider.dimensionId;

            final Vector3int pos = new Vector3int(dock.xCoord, dock.yCoord, dock.zCoord);
            if (!tileMap.containsKey(dimID)) {
                return false;
            }
            final Map<Vector3int, Boolean> set = tileMap.get(dimID);
            if (!set.containsKey(pos)) {
                return false;
            }
            return set.get(pos);
        }
        return false;
    }

    public static void setStoredAvailability(final TileEntityShuttleDock dock, final boolean isAvailable) {
        if (!dock.getWorldObj().isRemote) {
            if (dock.isInvalid()) {
                return;
            }
            final int dimID = dock.getWorldObj().provider.dimensionId;

            final Vector3int pos = new Vector3int(dock.xCoord, dock.yCoord, dock.zCoord);

            if (!tileMap.containsKey(dimID)) {
                final Map<Vector3int, Boolean> set = new HashMap<>();// pos
                set.put(pos, isAvailable);
                tileMap.put(dimID, set);
            } else {
                final Map<Vector3int, Boolean> set = tileMap.get(dimID);
                set.put(pos, isAvailable);
            }
            markInstanceDirty();
        }
    }

    public static void addDock(final TileEntityShuttleDock dock) {
        setStoredAvailability(dock, dock.isAvailable());
    }

    protected static void removeDock(final int dimID, final int x, final int y, final int z) {
        if (tileMap.containsKey(dimID)) {
            final Vector3int pos = new Vector3int(x, y, z);

            tileMap.get(dimID).remove(pos);
            markInstanceDirty();
        }
    }

    public static void removeDock(final TileEntityShuttleDock dock) {
        if (!dock.getWorldObj().isRemote) {
            final int dimID = dock.getWorldObj().provider.dimensionId;
            removeDock(dimID, dock.xCoord, dock.yCoord, dock.zCoord);
        }
    }

    public static Vector3int findAvailableDock(final int dimID) {

        if (tileMap.containsKey(dimID)) {
            final Map<Vector3int, Boolean> positions = tileMap.get(dimID);
            if (positions.size() > 0) {
                // actually look up
                final MinecraftServer theServer = FMLCommonHandler.instance().getMinecraftServerInstance();
                final World ws = theServer.worldServerForDimension(dimID);

                for (final Vector3int pos : positions.keySet()) {
                    // int chunkx = CoordHelper.blockToChunk(pos.x);
                    // int chunkz = CoordHelper.blockToChunk(pos.z);
                    // ws.checkChunksExist(p_72904_1_, p_72904_2_, p_72904_3_, p_72904_4_, p_72904_5_, p_72904_6_)
                    // if (ws.getChunkProvider().chunkExists(chunkx, chunkz)) {
                    // seems like there is no real way to figure out if a chunk has actually been really, really, loaded
                    final TileEntity te = ws.getTileEntity(pos.x, pos.y, pos.z);
                    if (te instanceof TileEntityShuttleDock) {
                        if (((TileEntityShuttleDock) te).isAvailable()) {
                            return pos;
                        }
                    } else {
                        final Boolean avail = positions.get(pos);
                        if (avail) {
                            return pos;
                        }
                        // now if this chunk is loaded, and te is still null or wrong, then something's bad
                        // removeDock(dimID, pos.x, pos.y, pos.z);
                    }
                    // } else {
                    // return the stored value
                    /*
                     * Boolean avail = positions.get(pos); if(avail) { return pos; }
                     */
                    // }
                }
            }
        }

        return null;
    }

}
