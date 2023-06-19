package de.katzenpapst.amunra.mothership.fueldisplay;

import net.minecraft.util.IIcon;

import de.katzenpapst.amunra.helper.GuiHelper;

/**
 * Not really an item, just a pseudo thingy
 *
 * @author katzenpapst
 *
 */
abstract public class MothershipFuelDisplay {

    abstract public IIcon getIcon();

    abstract public String getDisplayName();

    abstract public int getSpriteNumber();

    abstract public String getUnit();

    abstract public float getFactor();

    public String formatValue(final float value) {
        return GuiHelper.formatMetric(value * this.getFactor(), this.getUnit(), true);
    }
}
