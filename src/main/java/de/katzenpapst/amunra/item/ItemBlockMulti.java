package de.katzenpapst.amunra.item;

import micdoodle8.mods.galacticraft.core.items.ItemBlockDesc;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.block.IMetaBlock;
import de.katzenpapst.amunra.block.SubBlock;

/**
 * Item for multiblocks
 * 
 * @author katzenpapst
 *
 */
public class ItemBlockMulti extends ItemBlockDesc {

    public ItemBlockMulti(Block block) {
        super(block); // it ends up in field_150939_a
        this.setMaxDamage(0); // ?
        this.setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack par1ItemStack) {
        // colors the name
        return ClientProxyCore.galacticraftItem;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        String subBlockName = ((IMetaBlock) field_150939_a).getUnlocalizedSubBlockName(itemstack.getItemDamage());
        return "tile." + subBlockName;
    }

    @Override
    public String getUnlocalizedName() {
        return this.field_150939_a.getUnlocalizedName() + ".0";
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        // dafuq

        SubBlock sb = ((IMetaBlock) field_150939_a).getSubBlock(damage);

        if (sb == null) {
            System.out.println("fuuq");
        }

        return sb.getIcon(1, 0);
        // return this.field_150938_b != null ? this.field_150938_b : this.field_150939_a.getBlockTextureFromSide(1);
    }
}
