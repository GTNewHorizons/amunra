package de.katzenpapst.amunra.item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.UseHoeEvent;

import buildcraft.api.tools.IToolWrench;
import cofh.api.block.IDismantleable;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.api.tool.ITool;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.GuiIds;
import de.katzenpapst.amunra.helper.InteroperabilityHelper;

@Optional.InterfaceList({
        @Optional.Interface(iface = "crazypants.enderio.api.tool.ITool", modid = "EnderIO", striprefs = true),
        @Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraft|Core", striprefs = true) })
public class ItemNanotool extends ItemAbstractBatteryUser implements ITool, IToolWrench {

    protected IIcon[] icons = null;

    protected float efficiencyOnProperMaterial = 6.0F;

    protected String[] textures = { "nanotool", "nanotool-axe", "nanotool-hoe", "nanotool-pickaxe", "nanotool-shears",
            "nanotool-shovel", "nanotool-wrench" };

    public enum Mode {
        WORKBENCH,
        AXE,
        HOE,
        PICKAXE,
        SHEARS,
        SHOVEL,
        WRENCH
    }

    // total power with default battery = 15000
    // a diamond tool has 1562 small uses
    // with small = 20, it will be 750 uses
    public final float energyCostUseSmall = 20.0F;
    public final float energyCostUseBig = 40.0F;
    public final float energyCostSwitch = 60.0F;

    protected Map<Mode, Set<String>> toolClassesSet;

    public ItemNanotool(final String name) {
        this.setUnlocalizedName(name);

        this.icons = new IIcon[this.textures.length];

        this.setTextureName(AmunRa.TEXTUREPREFIX + "nanotool-empty");

        // init this stuff
        this.toolClassesSet = new HashMap<>();

        final Set<String> axe = new HashSet<>();
        axe.add("axe");
        this.toolClassesSet.put(Mode.AXE, axe);

        final Set<String> hoe = new HashSet<>();
        hoe.add("hoe");
        this.toolClassesSet.put(Mode.HOE, hoe);

        final Set<String> pick = new HashSet<>();
        pick.add("pickaxe");
        this.toolClassesSet.put(Mode.PICKAXE, pick);

        final Set<String> shovel = new HashSet<>();
        shovel.add("shovel");
        this.toolClassesSet.put(Mode.SHOVEL, shovel);

        final Set<String> empty = new HashSet<>();
        this.toolClassesSet.put(Mode.SHEARS, empty);
        this.toolClassesSet.put(Mode.WRENCH, empty);
        this.toolClassesSet.put(Mode.WORKBENCH, empty);
    }

    protected void setMode(final ItemStack stack, final Mode m) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger("toolMode", m.ordinal());
    }

    public Mode getMode(final ItemStack stack) {
        final int ord = this.getModeInt(stack);
        return Mode.values()[ord];
    }

    protected int getModeInt(final ItemStack stack) {
        final NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return 0;
        }
        return nbt.getInteger("toolMode");
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return AmunRa.arTab;
    }

    public boolean hasEnoughEnergyAndMode(final ItemStack stack, final float energy, final Mode mode) {
        return this.getMode(stack) == mode && this.hasEnoughEnergy(stack, energy);
    }

    public boolean hasEnoughEnergy(final ItemStack stack, final float energy) {
        final float storedEnergy = this.getElectricityStored(stack);
        return storedEnergy >= energy;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (player.isSneaking()) {
            // the wrench sometimes works when sneak-rightclicking
            if (this.hasEnoughEnergyAndMode(itemStackIn, this.energyCostUseBig, Mode.WRENCH)
                    && Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                return super.onItemRightClick(itemStackIn, worldIn, player);
            }
            // try switching
            if (this.hasEnoughEnergy(itemStackIn, this.energyCostSwitch)) {
                Mode m = this.getMode(itemStackIn);
                m = this.getNextMode(m);
                this.consumePower(itemStackIn, player, this.energyCostSwitch);
                this.setMode(itemStackIn, m);
            }
            return itemStackIn;
        }
        if (this.hasEnoughEnergyAndMode(itemStackIn, this.energyCostUseBig, Mode.WORKBENCH)) {
            this.consumePower(itemStackIn, player, this.energyCostUseBig);
            player.openGui(
                    AmunRa.instance,
                    GuiIds.GUI_CRAFTING,
                    worldIn,
                    (int) player.posX,
                    (int) player.posY,
                    (int) player.posZ);
            return itemStackIn;
        }
        //
        return super.onItemRightClick(itemStackIn, worldIn, player);
    }

    public Mode getNextMode(final Mode fromMode) {
        return switch (fromMode) {
            case WORKBENCH -> Mode.PICKAXE;
            case PICKAXE -> Mode.SHOVEL;
            case SHOVEL -> Mode.AXE;
            case AXE -> Mode.HOE;
            case HOE -> Mode.SHEARS;
            case SHEARS -> Mode.WRENCH;
            case WRENCH -> Mode.WORKBENCH;
            default -> Mode.PICKAXE;
        };

    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        return this.getIconIndex(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        super.registerIcons(register);
        for (int i = 0; i < this.textures.length; i++) {
            this.icons[i] = register.registerIcon(AmunRa.TEXTUREPREFIX + this.textures[i]);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack p_77650_1_) {
        final float energy = this.getElectricityStored(p_77650_1_);
        if (energy <= 0) {
            return this.itemIcon;
        }

        return this.icons[this.getModeInt(p_77650_1_)];
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        final float energy = this.getElectricityStored(stack);
        if (energy > 0) {
            final Mode m = this.getMode(stack);
            return this.toolClassesSet.get(m);
        }
        return super.getToolClasses(stack);
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass) {
        final float energy = this.getElectricityStored(stack);
        if (energy < this.energyCostUseSmall) {
            return -1;
        }
        final Mode m = this.getMode(stack);
        if (!this.toolClassesSet.get(m).contains(toolClass)) {
            return -1;
        }
        return 5;
    }

    @Override
    public float getDigSpeed(ItemStack itemstack, Block block, int metadata) {
        if (!this.hasEnoughEnergy(itemstack, this.energyCostUseSmall)) {
            return 1.0F;
        }
        if (ForgeHooks.isToolEffective(itemstack, block, metadata)
                || this.isEffectiveAgainst(this.getMode(itemstack), block)) {
            return this.efficiencyOnProperMaterial;
        }

        return super.getDigSpeed(itemstack, block, metadata);
    }

    protected boolean isEffectiveAgainst(final Mode m, final Block b) {

        return switch (m) {
            case AXE -> b.getMaterial() == Material.wood || b.getMaterial() == Material.plants
                    || b.getMaterial() == Material.vine;
            case PICKAXE -> b.getMaterial() == Material.iron || b.getMaterial() == Material.anvil
                    || b.getMaterial() == Material.rock;
            case SHEARS -> b.getMaterial() == Material.leaves || b.getMaterial() == Material.cloth
                    || b.getMaterial() == Material.carpet
                    || b == Blocks.web
                    || b == Blocks.redstone_wire
                    || b == Blocks.tripwire;
            case SHOVEL -> b.getMaterial() == Material.clay || b.getMaterial() == Material.ground;
            case WRENCH, WORKBENCH, HOE -> false;
            default -> false;
        };
    }

    protected String getTypeString(final Mode m) {
        return switch (m) {
            case AXE -> "item.nanotool.mode.axe";
            case HOE -> "item.nanotool.mode.hoe";
            case PICKAXE -> "item.nanotool.mode.pickaxe";
            case SHEARS -> "item.nanotool.mode.shears";
            case SHOVEL -> "item.nanotool.mode.shovel";
            case WORKBENCH -> "item.nanotool.mode.workbench";
            case WRENCH -> "item.nanotool.mode.wrench";
            default -> "";
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_,
            @SuppressWarnings("rawtypes") List p_77624_3_, boolean p_77624_4_) {
        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);

        final Mode m = this.getMode(p_77624_1_);

        p_77624_3_.add(
                StatCollector.translateToLocal("item.nanotool.mode-prefix") + ": "
                        + StatCollector.translateToLocal(this.getTypeString(m)));
    }

    // damaging start
    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase p_77644_2_, EntityLivingBase p_77644_3_) {
        if (this.hasEnoughEnergy(stack, this.energyCostUseBig)) {
            this.consumePower(stack, p_77644_3_, this.energyCostUseBig);
            return true;
        }
        return false;
    }

    // damaging end

    // shearing
    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, Block blockIn, int p_150894_4_, int p_150894_5_,
            int p_150894_6_, EntityLivingBase p_150894_7_) {
        if (!this.hasEnoughEnergy(stack, this.energyCostUseSmall)) {
            return false;
        }
        this.consumePower(stack, p_150894_7_, this.energyCostUseSmall);
        if (this.getMode(stack) == Mode.SHEARS) {

            if (blockIn.getMaterial() != Material.leaves && blockIn != Blocks.web
                    && blockIn != Blocks.tallgrass
                    && blockIn != Blocks.vine
                    && blockIn != Blocks.tripwire
                    && !(blockIn instanceof IShearable)) {
                return super.onBlockDestroyed(
                        stack,
                        worldIn,
                        blockIn,
                        p_150894_4_,
                        p_150894_5_,
                        p_150894_6_,
                        p_150894_7_);
            }
            return true;
        }
        return super.onBlockDestroyed(stack, worldIn, blockIn, p_150894_4_, p_150894_5_, p_150894_6_, p_150894_7_);
    }

    @Override
    public boolean canHarvestBlock(Block par1Block, ItemStack itemStack) {
        return this.isEffectiveAgainst(this.getMode(itemStack), par1Block);
    }

    protected void consumePower(final ItemStack itemStack, final EntityLivingBase user, final float power) {
        EntityPlayer player = null;
        if (user instanceof EntityPlayer) {
            player = (EntityPlayer) user;

        }
        if (player == null || !player.capabilities.isCreativeMode) {
            this.setElectricity(itemStack, this.getElectricityStored(itemStack) - power);
        }
    }

    /**
     * Seems to be a variant of getDigSpeed?
     */
    @Override
    public float func_150893_a(ItemStack p_150893_1_, Block p_150893_2_) {
        if (this.hasEnoughEnergyAndMode(p_150893_1_, this.energyCostUseSmall, Mode.SHEARS)) {
            return Items.shears.func_150893_a(p_150893_1_, p_150893_2_);
        }

        return super.func_150893_a(p_150893_1_, p_150893_2_);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target) {
        if (this.hasEnoughEnergyAndMode(stack, this.energyCostUseBig, Mode.SHEARS)) {
            this.consumePower(stack, player, this.energyCostUseBig);
            return Items.shears.itemInteractionForEntity(stack, player, target);
        }
        return super.itemInteractionForEntity(stack, player, target);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player) {
        if (this.hasEnoughEnergyAndMode(itemstack, this.energyCostUseSmall, Mode.SHEARS)) {
            return Items.shears.onBlockStartBreak(itemstack, X, Y, Z, player);
        }
        return super.onBlockStartBreak(itemstack, X, Y, Z, player);
    }

    // hoeing
    @Override
    public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_,
            int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        if (this.hasEnoughEnergyAndMode(p_77648_1_, this.energyCostUseSmall, Mode.HOE)) {
            if (!p_77648_2_.canPlayerEdit(p_77648_4_, p_77648_5_, p_77648_6_, p_77648_7_, p_77648_1_)) {
                return false;
            }
            final UseHoeEvent event = new UseHoeEvent(
                    p_77648_2_,
                    p_77648_1_,
                    p_77648_3_,
                    p_77648_4_,
                    p_77648_5_,
                    p_77648_6_);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                return false;
            }

            if (event.getResult() == Result.ALLOW) {
                this.consumePower(p_77648_1_, p_77648_2_, this.energyCostUseSmall);
                return true;
            }

            final Block block = p_77648_3_.getBlock(p_77648_4_, p_77648_5_, p_77648_6_);

            if ((p_77648_7_ == 0)
                    || !p_77648_3_.getBlock(p_77648_4_, p_77648_5_ + 1, p_77648_6_)
                            .isAir(p_77648_3_, p_77648_4_, p_77648_5_ + 1, p_77648_6_)
                    || ((block != Blocks.grass) && (block != Blocks.dirt))) {
                return false;
            }
            final Block block1 = Blocks.farmland;
            p_77648_3_.playSoundEffect(
                    p_77648_4_ + 0.5F,
                    p_77648_5_ + 0.5F,
                    p_77648_6_ + 0.5F,
                    block1.stepSound.getStepResourcePath(),
                    (block1.stepSound.getVolume() + 1.0F) / 2.0F,
                    block1.stepSound.getPitch() * 0.8F);

            if (p_77648_3_.isRemote) {} else {
                p_77648_3_.setBlock(p_77648_4_, p_77648_5_, p_77648_6_, block1);
                this.consumePower(p_77648_1_, p_77648_2_, this.energyCostUseSmall);
            }
            return true;
        }
        return super.onItemUse(
                p_77648_1_,
                p_77648_2_,
                p_77648_3_,
                p_77648_4_,
                p_77648_5_,
                p_77648_6_,
                p_77648_7_,
                p_77648_8_,
                p_77648_9_,
                p_77648_10_);
    }

    // wrenching
    @Override
    public boolean canWrench(final EntityPlayer entityPlayer, final int x, final int y, final int z) {
        final ItemStack stack = entityPlayer.inventory.getCurrentItem();

        return this.hasEnoughEnergyAndMode(stack, this.energyCostUseSmall, Mode.WRENCH);
    }

    @Override
    public void wrenchUsed(final EntityPlayer entityPlayer, final int x, final int y, final int z) {
        final ItemStack stack = entityPlayer.inventory.getCurrentItem();

        if (this.hasEnoughEnergyAndMode(stack, this.energyCostUseSmall, Mode.WRENCH)) {
            this.consumePower(stack, entityPlayer, this.energyCostUseSmall);
        }
    }

    // EnderIO
    @Override
    public boolean canUse(final ItemStack stack, final EntityPlayer player, final int x, final int y, final int z) {
        return this.hasEnoughEnergyAndMode(stack, this.energyCostUseSmall, Mode.WRENCH);
    }

    @Override
    public void used(final ItemStack stack, final EntityPlayer player, final int x, final int y, final int z) {
        this.consumePower(stack, player, this.energyCostUseSmall);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    private boolean attemptDismantle(final EntityPlayer entityPlayer, final Block block, final World world, final int x,
            final int y, final int z) {
        if (InteroperabilityHelper.hasIDismantleable && block instanceof IDismantleable
                && ((IDismantleable) block).canDismantle(entityPlayer, world, x, y, z)) {

            ((IDismantleable) block).dismantleBlock(entityPlayer, world, x, y, z, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
        if (this.hasEnoughEnergyAndMode(stack, this.energyCostUseSmall, Mode.WRENCH)) {

            if (world.isRemote) return false;

            final Block blockID = world.getBlock(x, y, z);

            // try dismantle
            if (player.isSneaking() && this.attemptDismantle(player, blockID, world, x, y, z)) {

                return true;

            }
            if (blockID == Blocks.furnace || blockID == Blocks.lit_furnace
                    || blockID == Blocks.dropper
                    || blockID == Blocks.hopper
                    || blockID == Blocks.dispenser
                    || blockID == Blocks.piston
                    || blockID == Blocks.sticky_piston) {
                final int metadata = world.getBlockMetadata(x, y, z);

                int[] rotationMatrix = { 1, 2, 3, 4, 5, 0 };

                if (blockID == Blocks.furnace || blockID == Blocks.lit_furnace) {
                    rotationMatrix = ForgeDirection.ROTATION_MATRIX[0];
                }

                world.setBlockMetadataWithNotify(
                        x,
                        y,
                        z,
                        ForgeDirection.getOrientation(rotationMatrix[metadata]).ordinal(),
                        3);
                this.wrenchUsed(player, x, y, z);

                return true;
            }

            return false;
        }
        return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean shouldHideFacades(final ItemStack stack, final EntityPlayer player) {
        return this.getMode(stack) == Mode.WRENCH;
        // return true;
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
        final ItemStack stack = player.inventory.getCurrentItem();

        return this.hasEnoughEnergyAndMode(stack, this.energyCostUseSmall, Mode.WRENCH)
                && Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectType.BLOCK;
    }

    // try this

}
