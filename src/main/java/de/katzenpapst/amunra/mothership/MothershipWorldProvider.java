package de.katzenpapst.amunra.mothership;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.util.Constants;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.astronomy.AngleDistance;
import de.katzenpapst.amunra.block.IMetaBlock;
import de.katzenpapst.amunra.block.SubBlock;
import de.katzenpapst.amunra.block.machine.mothershipEngine.MothershipEngineJetBase;
import de.katzenpapst.amunra.helper.AstronomyHelper;
import de.katzenpapst.amunra.helper.BlockMassHelper;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelRequirements;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR.EnumSimplePacket;
import de.katzenpapst.amunra.tick.TickHandlerServer;
import de.katzenpapst.amunra.tile.ITileMothershipEngine;
import de.katzenpapst.amunra.vec.Vector2int;
import de.katzenpapst.amunra.vec.Vector3int;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IExitHeight;
import micdoodle8.mods.galacticraft.api.world.ISolarLevel;
import micdoodle8.mods.galacticraft.api.world.IZeroGDimension;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;

public class MothershipWorldProvider extends WorldProviderSpace implements IZeroGDimension, ISolarLevel, IExitHeight {

    /**
     * Just to hold some stuff for transits
     *
     */
    public class TransitData {

        // the direction in which the ship will travel, relevant for skybox rendering
        public int direction = 0;
        // the max thrust the engines can reach, maybe to indicate how many more blocks you can add?
        public double thrust = 0;
        // optional, how much fuel we would need
        public MothershipFuelRequirements fuelReqData = null;

        public TransitData(final int direction, final double thrust) {
            this.direction = direction;
            this.thrust = thrust;
        }

        public TransitData() {
            this.direction = 0;
            this.thrust = 0;
        }

        public boolean isEmpty() {
            return this.thrust <= 0;
        }

        public void readFromNBT(final NBTTagCompound nbt) {
            this.direction = nbt.getInteger("direction");
            this.thrust = nbt.getDouble("thrust");
        }

        public void writeToNBT(final NBTTagCompound nbt) {
            nbt.setInteger("direction", this.direction);
            nbt.setDouble("thrust", this.thrust);
        }
    }

    public class TransitDataWithDuration extends TransitData {

        public long duration = 0;

        @Override
        public boolean isEmpty() {
            return this.duration <= 0 || super.isEmpty();
        }
    }

    /**
     * On client, this is the sole authority regarding the day length On server, this is changed and sent to client as
     * needed
     */
    protected long dayLength = 24000L;

    protected float thermalLevel = 0;

    protected double solarLevel = 1;

    // this is to workaround this case where a player logs in before whe have read from nbt
    protected boolean mustSendPacketToClients = false;
    protected boolean haveReadFromNBT = false;

    // TODO refactor
    protected boolean hasLoadedWorldData = false;

    // how many ticks have passed since the last time I counted all the blocks here
    protected int ticksSinceLastUpdate = 0;

    public static final int MIN_TICKS_BETWEEN_UPDATES = 200;

    protected MothershipWorldProviderSaveFile mothershipSaveFile;

    protected HashSet<Vector2int> checkedChunks = new HashSet<>();
    protected HashSet<Vector3int> engineLocations = new HashSet<>();

    protected boolean needParentParamUpdate = false;

    protected float totalMass;
    protected long totalNumBlocks;

    protected TransitData potentialTransitData;

    protected boolean isAsyncUpdateRunning = false;

    // to compare with the value of the mothershipObj to see if we started/ended transit
    protected boolean isInTransit = false;

    protected Mothership mothershipObj;

    public MothershipWorldProvider() {}

    public float getTotalMass() {
        return this.totalMass;
    }

    public long getNumBlocks() {
        return this.totalNumBlocks;
    }

    public TransitData getTheoreticalTransitData() {

        return this.potentialTransitData;
    }

    @Override
    public void setDimension(int dim) {
        // this really shouldn't happen...
        if (TickHandlerServer.mothershipData == null) {
            throw new RuntimeException(
                    "Premature Mothership dimension creation! This *MIGHT* be due to a configuration error. "
                            + "Please try changing I:mothershipProviderID in GalacticraftAmunRa.cfg and try again. "
                            + "If error persists, please report a bug including a complete list of your mods");
        }
        this.mothershipObj = TickHandlerServer.mothershipData.getByDimensionId(dim);
        if (this.mothershipObj == null) {
            throw new RuntimeException("Mothership with dim ID " + dim + " has no celestial body. This is bad!");
        }
        super.setDimension(dim);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getSunBrightness(float par1) {
        if (this.mothershipObj.isInTransit()) {
            return 0.0F; // dunno
        }
        if (AstronomyHelper.isStar(this.mothershipObj.getParent())) {
            return 1.0F; // always
        }
        // somewhat of a hack
        if (AstronomyHelper.getSolarSystem(this.mothershipObj.getParent()).equals(AmunRa.instance.systemAmunRa)) {
            float factor = this.worldObj.getSunBrightnessFactor(par1) + this.getAmunBrightnessFactor(par1);

            if (factor > 1.0F) {
                factor = 1.0F;
            }

            return factor;
        }
        return this.worldObj.getSunBrightnessBody(par1);
    }

    protected float getAmunBrightnessFactor(final float partialTicks) {
        CelestialBody curBody = this.getCelestialBody();
        if (curBody instanceof Moon) {
            curBody = ((Moon) curBody).getParentPlanet();
        }
        final AngleDistance ad = AstronomyHelper
                .projectBodyToSky(curBody, AmunRa.instance.starAmun, partialTicks, this.worldObj.getWorldTime());
        // ad.angle is in pi

        // the angle I get is relative to celestialAngle
        float brightnessFactor = 1.0F
                - (MathHelper.cos(this.worldObj.getCelestialAngle(partialTicks) * (float) Math.PI * 2.0F + ad.angle)
                        * 2.0F + 0.5F);

        if (brightnessFactor < 0) {
            brightnessFactor = 0;
        }
        if (brightnessFactor > 1) {
            brightnessFactor = 1;
        }

        brightnessFactor = 1.0F - brightnessFactor;

        // let's say brightnessFactor == 1 -> 0.5 of brightness
        return (float) (brightnessFactor * 0.5 / ad.distance);
    }

    @Override
    public CelestialBody getCelestialBody() {
        return this.mothershipObj;
    }

    @Override
    public long getDayLength() {
        return this.dayLength;
    }

    @Override
    public Class<? extends IChunkProvider> getChunkProviderClass() {
        return MothershipChunkProvider.class;
    }

    @Override
    public float calculateCelestialAngle(long par1, float par3) {
        if (this.getDayLength() == 0) {
            return 0.0F;
        }
        return super.calculateCelestialAngle(par1, par3);
    }

    @Override
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass() {
        return MothershipWorldChunkManager.class;
    }

    @Override
    public boolean isDaytime() {
        if (!this.mothershipObj.isInTransit() && this.mothershipObj.getParent() instanceof Star) {
            return true;
        }

        final float a = this.worldObj.getCelestialAngle(0F);
        // TODO: adjust this according to size of planet below. Or don't? I can say, we get closer for smaller planets
        return a < 0.42F || a > 0.58F;
    }

    @Override
    public String getDimensionName() {
        return this.mothershipObj == null ? "Mothership" : this.mothershipObj.getLocalizedName();
    }

    @Override
    public void updateWeather() {
        // I purposefully do not call super.updateWeather here, for now

        if (this.ticksSinceLastUpdate <= MIN_TICKS_BETWEEN_UPDATES) {
            // no point in counting them afterwards, I only need to know if it's larger than the constant
            this.ticksSinceLastUpdate++;
        }

        this.worldObj.getWorldInfo().setRainTime(0);
        this.worldObj.getWorldInfo().setRaining(false);
        this.worldObj.getWorldInfo().setThunderTime(0);
        this.worldObj.getWorldInfo().setThundering(false);
        this.worldObj.rainingStrength = 0.0F;
        this.worldObj.thunderingStrength = 0.0F;

        if (!this.worldObj.isRemote && !this.hasLoadedWorldData) {
            // kinda hack
            // updateParamsFromParent();
            this.mothershipSaveFile = MothershipWorldProviderSaveFile.getSaveFile(this.worldObj);
            this.readFromNBT(this.mothershipSaveFile.data);

            if (this.mustSendPacketToClients) {
                this.mustSendPacketToClients = false;
                // so apparently someone wanted to have the data before we read it
                // now just send it to everyone in the dimension
                // re-write the nbt for this, in case there hasn't been a save
                final NBTTagCompound newNbt = new NBTTagCompound();
                this.writeToNBT(newNbt);
                AmunRa.packetPipeline.sendToDimension(
                        new PacketSimpleAR(EnumSimplePacket.C_MOTHERSHIP_DATA, this.dimensionId, newNbt),
                        this.dimensionId);
            }
            this.hasLoadedWorldData = true;
        }

    }

    /**
     * Does the last minute MS check and starts the transit here and in the MS object. Returns true if this worked This
     * should only ever happen on server
     *
     * @param cheat if true, no checks are performed, no engines are actually started, the ship is moved anyway
     */
    public boolean startTransit(final CelestialBody target, final boolean cheat) {
        if (this.worldObj.isRemote) {
            // client
            return false;
        }

        if (cheat) {
            if (!this.mothershipObj.startTransit(target, 100)) {
                return false;
            }
            this.applyTransitParams();
            return true;
        }
        // first, do the check
        this.updateMothership(true);

        // now check if we can really reach the target
        final TransitDataWithDuration td = this.getTransitDataTo(target);
        if (td.isEmpty()) {
            return false;
        }

        // double distance = this.mothershipObj.getTravelDistanceTo(target);

        final long travelTime = td.duration;
        // now, the object

        if (!this.mothershipObj.startTransit(target, travelTime)) {
            return false;
        }

        this.applyTransitParams();
        // okay, seems like we can continue
        // we will need all engines
        for (final Vector3int loc : this.engineLocations) {
            final TileEntity tile = this.worldObj.getTileEntity(loc.x, loc.y, loc.z);

            if (tile instanceof ITileMothershipEngine engine && engine.isEnabled()
                    && engine.getDirection() == td.direction) {
                // double curSpeed = engine.getSpeed(worldObj, loc.x, loc.y, loc.z, meta);
                final double curThrust = engine.getThrust();
                if (curThrust <= 0) {
                    continue;
                }
                engine.beginTransit(travelTime);
            }
        }

        return true;
    }

    public boolean isInTransit() {
        return this.mothershipObj.isInTransit();
    }

    public void endTransit() {
        this.mothershipObj.endTransit();

        if (this.worldObj.isRemote) {
            return; // I think the rest will be synched over anyway?
        }

        for (final Vector3int loc : this.engineLocations) {
            final TileEntity t = this.worldObj.getTileEntity(loc.x, loc.y, loc.z);

            if (t instanceof ITileMothershipEngine engine && engine.isInUse()) {
                engine.endTransit();
            }
        }
        // other stuff
        this.updateParamsFromParent(true);
    }

    protected void applyTransitParams() {
        this.dayLength = 0;
        this.thermalLevel = 0;
        this.solarLevel = 0;

        this.saveData(true);
    }

    protected void updateParamsFromParent(final boolean save) {
        if (this.mothershipObj.isInTransit()) {
            return;
        }
        final CelestialBody parent = this.mothershipObj.getParent();
        if (AstronomyHelper.isStar(parent)) {
            this.dayLength = 0;
            this.thermalLevel = AstronomyHelper.maxTemperature;
            this.solarLevel = AstronomyHelper.maxSolarLevel;
        } else {

            this.thermalLevel = AstronomyHelper.getThermalLevel(parent);
            this.solarLevel = AstronomyHelper.getSolarEnergyMultiplier(parent, false);
            this.dayLength = 24000L;
            if (parent.getReachable()) {
                final WorldProvider p = WorldUtil.getProviderForDimensionServer(parent.getDimensionID());
                if (p instanceof WorldProviderSpace) {
                    // read stuff from the worldprovider
                    this.dayLength = ((WorldProviderSpace) p).getDayLength();
                }
            }
        }

        if (save) {
            this.saveData(true);
        }
    }

    /**
     * The currently orbited celestial body or null if in transit
     */
    public CelestialBody getParent() {
        return ((Mothership) this.getCelestialBody()).getParent();
    }

    @Override
    public String getSaveFolder() {
        return "DIM_MOTHERSHIP" + this.dimensionId;
    }

    @Override
    public double getSolarEnergyMultiplier() {
        return this.solarLevel;
    }

    @Override
    public float getThermalLevelModifier() {
        return this.thermalLevel;
    }

    @Override
    public double getHorizon() {
        return 0.0D;
    }

    /**
     * Figures out the direction of this ship, and the displayed amount of thrust, to check against the actual.
     */
    protected TransitData calcTheoreticalTransitData() {

        final TransitData[] tDatas = new TransitData[4];

        for (final Vector3int loc : this.engineLocations) {
            final TileEntity tile = this.worldObj.getTileEntity(loc.x, loc.y, loc.z);
            if (tile instanceof ITileMothershipEngine engine) {
                if (!engine.isEnabled()) {
                    continue;
                }

                final int direction = engine.getDirection();
                if (tDatas[direction] == null) {
                    tDatas[direction] = new TransitData(direction, 0);
                }
                // double curSpeed = engine.getSpeed(worldObj, loc.x, loc.y, loc.z, meta);
                final double curThrust = engine.getThrust();
                if (curThrust <= 0) {
                    continue; // not sure when this could happen, but in that case, this engine doesn't count
                }
                /*
                 * if(tDatas[direction].speed == -1 || curSpeed < tDatas[direction].speed) { tDatas[direction].speed =
                 * curSpeed; }
                 */
                tDatas[direction].thrust += curThrust;
            }
        }
        // now check which one will actually be relevant
        // pick the one with the highest thrust
        int resultDirection = -1;
        double maxThrust = 0;
        for (int i = 0; i < tDatas.length; i++) {
            if (tDatas[i] == null || tDatas[i].isEmpty()) {
                continue;
            }
            if (tDatas[i].thrust > maxThrust) {
                maxThrust = tDatas[i].thrust;
                resultDirection = i;
            }
        }

        if (resultDirection == -1) {
            return new TransitData();
        }
        return tDatas[resultDirection];
    }

    public MothershipFuelRequirements getPotentialFuelReqs(final CelestialBody target) {
        final TransitDataWithDuration data = this.getTransitDataTo(target, true);

        return data.fuelReqData;
    }

    public TransitDataWithDuration getTransitDataTo(final CelestialBody target) {
        return this.getTransitDataTo(target, false);
    }

    /**
     * @param potentialData if true, potential data will be returned
     */
    public TransitDataWithDuration getTransitDataTo(final CelestialBody target, final boolean potentialData) {
        TransitData generalData = this.calcTheoreticalTransitData();
        final double distance = this.mothershipObj.getTravelDistanceTo(target);
        long travelTime = AstronomyHelper.getTravelTimeAU(this.totalMass, generalData.thrust, distance);

        // now check if all engines in the set can burn for that long
        @SuppressWarnings("unchecked")
        HashSet<Vector3int> curEngineLocations = (HashSet<Vector3int>) this.engineLocations.clone();
        boolean success = false;
        final MothershipFuelRequirements fuelReqs = new MothershipFuelRequirements();
        TransitData newData = null;
        while (!success) {
            final HashSet<Vector3int> nextEngineLocations = new HashSet<>();
            success = true;
            newData = new TransitData();
            newData.direction = generalData.direction;
            fuelReqs.clear();

            for (final Vector3int loc : curEngineLocations) {

                final TileEntity tile = this.worldObj.getTileEntity(loc.x, loc.y, loc.z);
                if (tile instanceof ITileMothershipEngine engine) {
                    if (engine.getDirection() != generalData.direction || !engine.isEnabled()) {
                        continue;
                    }
                    if (!potentialData) {
                        // real data
                        if (!engine.canRunForDuration(travelTime)) {
                            // fail
                            success = false;
                        } else {
                            nextEngineLocations.add(loc);
                            newData.thrust += engine.getThrust();
                            fuelReqs.merge(engine.getFuelRequirements(travelTime));
                        }
                    } else {
                        // potential data
                        newData.thrust += engine.getThrust();
                        fuelReqs.merge(engine.getFuelRequirements(travelTime));
                    }
                }
            }
            if (!success) {
                // prepare stuff for next iteration
                curEngineLocations = nextEngineLocations;
                travelTime = AstronomyHelper.getTravelTimeAU(this.totalMass, newData.thrust, distance);
                generalData = newData;
            }
        }
        final TransitDataWithDuration result = new TransitDataWithDuration();
        result.duration = travelTime;
        result.direction = newData.direction;
        result.thrust = newData.thrust;
        if (!fuelReqs.isEmpty()) {
            result.fuelReqData = fuelReqs;
        }

        return result;
    }

    /**
     * Send my data to the client. Just sends it if it's considered fresh enough, recalcs it if it's too old
     */
    public void asyncSendMothershipDataToClient() {
        if (this.ticksSinceLastUpdate <= MIN_TICKS_BETWEEN_UPDATES) {
            // just send the players what we have
            this.sendDataToClients();
        } else {
            // do that
            this.asyncMothershipUpdate();
        }
    }

    /**
     * This starts a thread with the updateMothership method
     */
    public void asyncMothershipUpdate() {
        // I hope this works...
        if (this.isAsyncUpdateRunning) return;
        this.isAsyncUpdateRunning = true;

        final MothershipWorldProvider self = this;
        final Runnable r = () -> {
            self.updateMothership(true);
            self.isAsyncUpdateRunning = false;
            self.asyncMothershipUpdateFinished();
        };
        final Thread t = new Thread(r);
        t.start();
    }

    protected void asyncMothershipUpdateFinished() {

    }

    /**
     * This should recalculate the size and mass of the ship, and find all the engines
     *
     * @param notifyClients whenever to send a packet to notify clients afterwards
     */
    public void updateMothership(final boolean notifyClients) {
        // I have absolutely no idea whenever I can trust this...

        // worldObj.getChunkProvider().getLoadedChunkCount()
        this.checkedChunks.clear();
        this.engineLocations.clear();
        this.totalMass = 0;
        this.totalNumBlocks = 0;

        this.potentialTransitData = new TransitData();
        this.processChunk(0, 0);

        // also recalc transit data
        this.potentialTransitData = this.calcTheoreticalTransitData();

        // save
        this.saveData(notifyClients);
    }

    protected void saveData(final boolean notifyClients) {
        if (this.mothershipSaveFile == null) {
            this.mothershipSaveFile = MothershipWorldProviderSaveFile.getSaveFile(this.worldObj);
        }

        this.writeToNBT(this.mothershipSaveFile.data);
        this.mothershipSaveFile.markDirty();
        this.ticksSinceLastUpdate = 0;

        if (notifyClients) {
            this.sendDataToClients();
        }
    }

    /**
     * Sends my current data to all clients in my dimension, as-is
     */
    protected void sendDataToClients() {
        final NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        AmunRa.packetPipeline.sendToDimension(
                new PacketSimpleAR(EnumSimplePacket.C_MOTHERSHIP_DATA, this.dimensionId, nbt),
                this.dimensionId);
    }

    /**
     * Processes one chunk for the mothership update, and recursively it's neighbours, until a nonexisting chunk is
     * found.
     *
     * @param x
     * @param z
     */
    protected void processChunk(final int x, final int z) {
        final Vector2int curCoords = new Vector2int(x, z);
        if (this.checkedChunks.contains(curCoords)) {
            return;
        }
        this.checkedChunks.add(curCoords);
        if (!this.worldObj.getChunkProvider().chunkExists(x, z)) {
            return;
        }
        // actually process the chunk here
        final Chunk c = this.worldObj.getChunkFromChunkCoords(x, z);
        final ExtendedBlockStorage[] storage = c.getBlockStorageArray();
        int minY = 256;
        int maxY = -1;
        for (final ExtendedBlockStorage st : storage) {
            if (st == null) continue;
            /*
             * yLocation: Contains the bottom-most Y block represented by this ExtendedBlockStorage. Typically a
             * multiple of 16.
             */
            if (st.getYLocation() < minY) {
                minY = st.getYLocation();
            }
            if (st.getYLocation() > maxY) {
                maxY = st.getYLocation();
            }
        }
        if (minY > maxY) {
            // seems this chunk is empty
        } else {
            maxY += 15; // because there are 16 blocks in that storage

            for (int blockX = 0; blockX < 16; blockX++) {
                for (int blockZ = 0; blockZ < 16; blockZ++) {
                    for (int blockY = minY; blockY <= maxY; blockY++) {
                        final Block b = c.getBlock(blockX, blockY, blockZ);
                        int meta;
                        if (b != Blocks.air) {
                            meta = c.getBlockMetadata(blockX, blockY, blockZ);
                            // figure out it's... stuff
                            this.processBlock(
                                    b,
                                    meta,
                                    CoordHelper.rel2abs(blockX, x),
                                    blockY,
                                    CoordHelper.rel2abs(blockZ, z));
                        }
                    }
                }
            }
        }

        // recursion steps for the neighbours
        this.processChunk(x + 1, z);
        this.processChunk(x - 1, z);
        this.processChunk(x, z + 1);
        this.processChunk(x, z - 1);
    }

    /**
     * This should process one block for the mothership update, figuring out it's mass and stuff The coordinates must be
     * world-global, not chunk-local
     */
    protected void processBlock(final Block block, final int meta, final int x, final int y, final int z) {
        // first, the mass
        final float m = BlockMassHelper.getBlockMass(this.worldObj, block, meta, x, y, z);

        this.totalMass += m;
        this.totalNumBlocks++;
        // do I still need center of mass and such? I don't care for now.

        // now, engines
        if (block instanceof IMetaBlock) {
            final SubBlock actualBlock = ((IMetaBlock) block).getSubBlock(meta);
            if (actualBlock instanceof MothershipEngineJetBase) {
                // just save their positions
                this.engineLocations.add(new Vector3int(x, y, z));
            }
        }
    }

    /**
     * Call this when player first login/transfer to this dimension
     * <p/>
     * TODO how can this code be called by other mods / plugins with teleports (e.g. Bukkit)? See
     * WorldUtil.teleportEntity()
     */
    public void sendPacketsToClient(final EntityPlayerMP player) {
        // so apparently, this can happen even before the worldprovider itself has readFromNbt...
        if (!this.haveReadFromNBT) {
            this.mustSendPacketToClients = true;
            return;
        }
        final NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);

        AmunRa.packetPipeline
                .sendTo(new PacketSimpleAR(EnumSimplePacket.C_MOTHERSHIP_DATA, this.dimensionId, nbt), player);
    }

    public void readFromNBT(final NBTTagCompound nbt) {
        this.totalMass = nbt.getFloat("totalMass");
        this.totalNumBlocks = nbt.getLong("totalNumBlocks");

        final NBTTagList list = nbt.getTagList("engineLocations", Constants.NBT.TAG_COMPOUND);

        this.engineLocations.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound posData = list.getCompoundTagAt(i);
            final Vector3int pos = new Vector3int(
                    posData.getInteger("x"),
                    posData.getInteger("y"),
                    posData.getInteger("z"));
            this.engineLocations.add(pos);
        }

        if (this.potentialTransitData == null) {
            this.potentialTransitData = new TransitData();
        }
        this.potentialTransitData.readFromNBT(nbt.getCompoundTag("transitData"));

        if (nbt.hasKey("dayLength") && nbt.hasKey("solarLevel") && nbt.hasKey("solarLevel")) {

            this.dayLength = nbt.getLong("dayLength");
            this.solarLevel = nbt.getDouble("solarLevel");
            this.thermalLevel = nbt.getFloat("thermalLevel");
        } else if (!this.worldObj.isRemote) {
            this.updateParamsFromParent(false);
        }

        this.haveReadFromNBT = true;
    }

    @Override
    public void resetRainAndThunder() {
        super.resetRainAndThunder();
        // this is a hack again. I *think* that resetRainAndThunder is only ever called from wakeAllPlayers

        this.mothershipObj.forceArrival();
    }

    public void writeToNBT(final NBTTagCompound nbt) {
        nbt.setFloat("totalMass", this.totalMass);
        nbt.setLong("totalNumBlocks", this.totalNumBlocks);

        // engine locations
        final NBTTagList list = new NBTTagList();
        for (final Vector3int v : this.engineLocations) {
            final NBTTagCompound pos = new NBTTagCompound();
            pos.setInteger("x", v.x);
            pos.setInteger("y", v.y);
            pos.setInteger("z", v.z);

            list.appendTag(pos);
        }
        nbt.setTag("engineLocations", list);

        final NBTTagCompound tData = new NBTTagCompound();
        if (this.potentialTransitData == null) {
            this.potentialTransitData = this.calcTheoreticalTransitData();
        }
        this.potentialTransitData.writeToNBT(tData);
        nbt.setTag("transitData", tData);

        nbt.setFloat("thermalLevel", this.thermalLevel);
        nbt.setDouble("solarLevel", this.solarLevel);
        nbt.setLong("dayLength", this.dayLength);
    }

    @Override
    public boolean shouldForceRespawn() {
        return !ConfigManagerCore.forceOverworldRespawn;
    }

    @Override
    public ChunkCoordinates getSpawnPoint() {
        return new ChunkCoordinates(0, 64, 0);
    }

    @Override
    public ChunkCoordinates getRandomizedSpawnPoint() {
        return this.getSpawnPoint();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setCloudRenderer(IRenderHandler renderer) {
        super.setCloudRenderer(renderer);
    }

    /**
     * Returns if given player is the owner of this mothership
     */
    public boolean isPlayerOwner(final EntityPlayer player) {
        return this.mothershipObj.isPlayerOwner(player);
    }

    public boolean isPlayerLandingPermitted(final EntityPlayer player) {
        return this.mothershipObj.isPlayerLandingPermitted(player);
    }

    public boolean isPlayerUsagePermitted(final EntityPlayer player) {
        return this.mothershipObj.isPlayerUsagePermitted(player);
    }

    @Override
    public float getGravity() {
        return 0.075F;
    }

    @Override
    public double getMeteorFrequency() {
        return 0;
    }

    @Override
    public double getFuelUsageMultiplier() {
        return 0.5D;
    }

    @Override
    public boolean canSpaceshipTierPass(final int tier) {
        return tier >= 0;
    }

    @Override
    public float getFallDamageModifier() {
        return 0.4F;
    }

    @Override
    public float getSoundVolReductionAmount() {
        return 50.0F;
    }

    @Override
    public float getWindLevel() {
        return 0.1F;
    }

    @Override
    public Vector3 getFogColor() {
        return new Vector3(0, 0, 0);
    }

    @Override
    public Vector3 getSkyColor() {
        return new Vector3(0, 0, 0);
    }

    @Override
    public boolean canRainOrSnow() {
        return false;
    }

    @Override
    public boolean hasSunset() {
        return false;
    }

    @Override
    public double getYCoordinateToTeleport() {
        return 1200;
    }
}
