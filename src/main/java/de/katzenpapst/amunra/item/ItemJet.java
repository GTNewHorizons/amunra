package de.katzenpapst.amunra.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.block.BlockMachineMeta;
import de.katzenpapst.amunra.block.machine.mothershipEngine.MothershipEngineJetBase;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class ItemJet extends ItemBlockMulti {

    protected IIcon[] icons;

    public ItemJet(final BlockMachineMeta blockMothershipEngineRocket, final String assetName) {
        super(blockMothershipEngineRocket);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1); // why?
        this.setUnlocalizedName(assetName);
    }

    @Override
    public String getUnlocalizedName() {
        return this.field_150939_a.getUnlocalizedName();
    }

    @Override
    public EnumRarity getRarity(ItemStack p_77613_1_) {
        // colors the name
        return ClientProxyCore.galacticraftItem;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        final int length = ((BlockMachineMeta) this.field_150939_a).getNumPossibleSubBlocks();
        this.icons = new IIcon[length];
        for (int i = 0; i < length; i++) {
            final MothershipEngineJetBase sb = (MothershipEngineJetBase) ((BlockMachineMeta) this.field_150939_a)
                    .getSubBlock(i);
            if (sb != null) {
                this.icons[i] = register.registerIcon(sb.getItemIconName());
            }
        }
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return AmunRa.arTab;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_) {
        return this.icons[p_77617_1_];
    }

    @Override
    public int getMetadata(int p_77647_1_) {
        return p_77647_1_;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ, int metadata) {

        /**
         * 0 -> +Y 1 -> -Y 2 -> -Z 3 -> +Z 4 -> -X 5 -> +X
         *
         *
         ** value | motion direction | ------+----------------- + 0 | +Z | 1 | -X | 2 | -Z | 3 | +X |
         *
         */

        int blockRotation = 0;

        switch (side) {
            case 0:
            case 1:
                blockRotation = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
                break;
            case 2:
                blockRotation = 0;
                break;
            case 3:
                blockRotation = 2;
                break;
            case 4:
                blockRotation = 3;
                break;
            case 5:
                blockRotation = 1;
                break;
        }

        metadata = ARBlocks.metaBlockMothershipEngineJet.addRotationMeta(stack.getItemDamage(), blockRotation);

        if (!world.setBlock(x, y, z, this.field_150939_a, metadata, 3)) {
            return false;
        }

        if (world.getBlock(x, y, z) == this.field_150939_a) {
            this.field_150939_a.onBlockPlacedBy(world, x, y, z, player, stack);
            this.field_150939_a.onPostBlockPlaced(world, x, y, z, metadata);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_,
            @SuppressWarnings("rawtypes") List p_77624_3_, boolean p_77624_4_) {
        if (this.field_150939_a instanceof IBlockShiftDesc
                && ((IBlockShiftDesc) this.field_150939_a).showDescription(p_77624_1_.getItemDamage())) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                p_77624_3_.addAll(
                        FMLClientHandler.instance().getClient().fontRenderer.listFormattedStringToWidth(
                                ((IBlockShiftDesc) this.field_150939_a).getShiftDescription(p_77624_1_.getItemDamage()),
                                150));
            } else {
                p_77624_3_.add(
                        GCCoreUtil.translateWithFormat(
                                "itemDesc.shift.name",
                                GameSettings.getKeyDisplayString(
                                        FMLClientHandler.instance().getClient().gameSettings.keyBindSneak
                                                .getKeyCode())));
            }
        }
    }

}
