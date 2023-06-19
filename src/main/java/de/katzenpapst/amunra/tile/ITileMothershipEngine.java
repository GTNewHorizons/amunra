package de.katzenpapst.amunra.tile;

import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelRequirements;

public interface ITileMothershipEngine {

    /**
     * This should return the force this engine can provide, in Newtons. This value should be independent of fuel
     * levels, but can be dependent on the block's current configuration
     */
    double getThrust();

    /**
     * Should figure out whenever it has enough fuel or energy or whatever to continuously work for the given duration
     */
    boolean canRunForDuration(long duration);

    /**
     * Should return a map of all the fuel types that are needed for this transit
     */
    MothershipFuelRequirements getFuelRequirements(long duration);

    /**
     * Should return the direction in which the engine is pointing, and, by that, where it would push the ship
     * <p>
     * value | motion direction | ------+----------------- + 0 | +Z | 1 | -X | 2 | -Z | 3 | +X |
     */
    int getDirection();

    /**
     * Should consume the fuel needed for the transition, on client side also start any animation or something alike.
     * This will be called for all engines which are actually being used
     */
    void beginTransit(long duration);

    /**
     * Will be called on all which return true from isInUse on transit end
     */
    void endTransit();

    /**
     * Should return whenever beginTransit has been called on this engine, and endTransit hasn't yet
     */
    boolean isInUse();

    /**
     * Return false if this engine should just not be considered
     */
    boolean isEnabled();
}
