package de.katzenpapst.amunra.item;

import de.katzenpapst.amunra.block.SubBlock;
import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.block.IMetaBlock;
import micdoodle8.mods.galacticraft.core.items.ItemBlockDesc;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;

/**
 * Item for multiblocks
 *
 * @author katzenpapst
 */
public class ItemBlockMulti extends ItemBlockDesc {

    public ItemBlockMulti(final Block block) {
        super(block); // it ends up in field_150939_a
        this.setMaxDamage(0); // ?
        this.setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack p_77613_1_) {
        // colors the name
        return ClientProxyCore.galacticraftItem;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        final String subBlockName = ((IMetaBlock) this.field_150939_a)
                .getUnlocalizedSubBlockName(stack.getItemDamage());
        return "tile." + subBlockName;
    }

    @Override
    public String getUnlocalizedName() {
        return this.field_150939_a.getUnlocalizedName() + ".0";
    }

    @Override
    public int getMetadata(int p_77647_1_) {
        return p_77647_1_;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_) {
        SubBlock sb = ((IMetaBlock) this.field_150939_a).getSubBlock(p_77617_1_);
        if(sb == null){
            return null;
        }
        return sb.getIcon(1, 0);
    }
}
