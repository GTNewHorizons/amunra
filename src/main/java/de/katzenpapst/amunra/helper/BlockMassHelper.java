package de.katzenpapst.amunra.helper;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

import de.katzenpapst.amunra.block.BlockMetaPairHashable;
import de.katzenpapst.amunra.block.IMassiveBlock;

public class BlockMassHelper {

    private static final Map<BlockMetaPairHashable, Float> blockMassMap = new HashMap<>();

    public static float getBlockMass(final World world, final Block block, final int meta, final int x, final int y,
            final int z) {
        if (block == null) {
            return 0.0f;
        }
        // first, the mass
        if (block.isAir(world, x, y, z)) {
            return 0.0F;
        }
        if (block instanceof IMassiveBlock massiveBlock) {
            return massiveBlock.getMass(world, x, y, z, meta);
        }
        final BlockMetaPairHashable bmph = new BlockMetaPairHashable(block, (byte) meta);
        if (blockMassMap.containsKey(bmph)) {
            return blockMassMap.get(bmph);
        }
        final float guessedMass = guessBlockMass(world, block, meta, x, y, z);

        blockMassMap.put(bmph, guessedMass);

        return guessedMass;
    }

    public static float guessBlockMass(final World world, final Block block, final int meta, final int x, final int y,
            final int z) {

        if (block instanceof IFluidBlock fluidBlock) {
            return getMassForFluid(fluidBlock.getFluid());
        }
        if (block instanceof BlockLiquid) {
            // vanilla MC fluids
            if (block == Blocks.lava) {
                return getMassForFluid(FluidRegistry.LAVA);
            }
            return getMassForFluid(FluidRegistry.WATER);
        }

        // extra stuff
        if (block == Blocks.snow_layer) {
            return (meta + 1) * 0.025F;
        }
        if (block == Blocks.vine) {
            return 0.01F;
        }

        return getMassFromHardnessAndMaterial(block.getBlockHardness(world, x, y, z), block.getMaterial());

    }

    public static float getMassForFluid(final Fluid fluid) {
        final int density = fluid.getDensity();
        // assume density to be in grams until I have a better idea
        return density / 1000.0F;
    }

    public static float getMassFromHardnessAndMaterial(final float hardness, final Material material) {
        float m = hardness;
        if (m < 0.1F) {
            m = 0.1F;
        } else if (m > 30F) {
            m = 30F;
        }
        // Wood items have a high hardness compared with their presumed mass
        if (material == Material.wood) {
            m /= 4;
        }
        return m;
    }
}
