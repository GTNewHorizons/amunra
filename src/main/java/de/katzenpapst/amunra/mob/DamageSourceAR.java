package de.katzenpapst.amunra.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.IChatComponent;

import de.katzenpapst.amunra.entity.EntityBaseLaserArrow;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;

public class DamageSourceAR {

    public static DamageSource dsSuffocate = new DamageSource("wrongAtmoSuffocate").setDamageBypassesArmor();
    public static DamageSource dsFallOffShip = new DamageSource("fallOffMothership").setDamageBypassesArmor()
            .setDamageAllowedInCreativeMode();
    public static DamageSource dsEngine = new DamageSource("death.attack.killedByEngine");

    public static DamageSource getDSCrashIntoPlanet(final CelestialBody body) {
        return new DamageSourceCrash(body.getUnlocalizedName());
    }

    public static class DamageSourceCrash extends DamageSource {

        protected String bodyName;

        public DamageSourceCrash(final String bodyName) {
            super("fallOffMothershipIntoPlanet");
            this.setDamageBypassesArmor();
            this.setDamageAllowedInCreativeMode();
            this.bodyName = bodyName;
        }

        @Override
        public IChatComponent func_151519_b(EntityLivingBase p_151519_1_) {
            // EntityLivingBase entitylivingbase1 = p_151519_1_.func_94060_bK();
            final String s = "death.attack." + this.damageType;
            return new ChatComponentTranslation(
                    s,
                    p_151519_1_.func_145748_c_(),
                    new ChatComponentTranslation(this.bodyName));
        }

    }

    /**
     * returns EntityDamageSourceIndirect of an arrow
     */
    public static DamageSource causeLaserDamage(final String langKey, final EntityBaseLaserArrow arrow,
            final Entity shooter) {
        return new EntityDamageSourceIndirect(langKey, arrow, shooter).setProjectile();
    }
}
