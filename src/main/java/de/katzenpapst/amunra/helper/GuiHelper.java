package de.katzenpapst.amunra.helper;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.StatCollector;

import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class GuiHelper {

    protected static DecimalFormat numberFormat = new DecimalFormat("#.##");

    public static final String[] metricHigh = { "k", "M", "G", "T", "P", "E", "Z", "Y" };
    public static final String[] metricLow = { "m", "µ", "n", "p", "f", "a", "z", "y" };

    public static String formatMetric(final double number) {
        return formatMetric(number, "");
    }

    public static String formatMetric(final double number, final String unit) {
        return formatMetric(number, unit, false);
    }

    public static String formatMetric(double number, final String unit, final boolean addSpace) {
        if (number < 0) {
            return "-" + formatMetric(number * -1, unit);
        }
        if (number == 0) {
            if (addSpace) {
                return String.format("%s %s", numberFormat.format(number), unit);
            }
            return String.format("%s%s", numberFormat.format(number), unit);
        }
        String suffix = "";
        String result = "";
        final int numZeroes = (int) Math.floor(Math.log10(number));
        int numThousands = (int) Math.floor(numZeroes / 3);
        if (numThousands > 0) {

            if (numThousands > metricHigh.length) {
                numThousands = metricHigh.length;
            }
            number = number / Math.pow(1000, numThousands);
            suffix = metricHigh[numThousands - 1];
            // result = String.valueOf(number)+" "+metricHigh[numThousands-1];
        } else if (numThousands < 0) {
            numThousands *= -1;
            if (numThousands > metricLow.length) {
                numThousands = metricLow.length;
            }
            number = number / Math.pow(0.001, numThousands);
            // result = String.valueOf(number)+" "+metricLow[numThousands-1];
            suffix = metricLow[numThousands - 1];
        }

        // String.format
        result = numberFormat.format(number);
        if (!suffix.isEmpty()) {
            if (addSpace) {
                return String.format("%s %s%s", result, suffix, unit);
            }
            return String.format("%s%s%s", result, suffix, unit);
        }
        if (addSpace) {
            return String.format("%s %s", result, unit);
        }
        return String.format("%s%s", result, unit);
    }

    /**
     * Specialized version to format kilograms, because it's weird
     * 
     * @param number
     * @return
     */
    public static String formatKilogram(final double number) {

        return formatKilogram(number, false);

    }

    public static String formatKilogram(final double number, final boolean addSpace) {
        if (number < 0) {
            return "-" + formatKilogram(number * -1, addSpace);
        }
        if (number < 1000) {
            // for 0 <= n < 1000, format the number using grams
            // this should prepend the k if needed
            return formatMetric(number * 1000, "g", addSpace);
        }
        // over 1000, format this using tons
        return formatMetric(number / 1000, "t", addSpace);
    }

    /**
     * Formats a time (in ticks) to a hh:mm:ss format, with minecraft hours, minutes and seconds
     * 
     * @param number
     * @return
     */
    public static String formatTime(final long number) {
        return formatTime(number, false);
    }

    /**
     * Formats a time, and optionally a date, too, if the time is too high
     * 
     * @param number
     * @param formatDate
     * @return
     */
    public static String formatTime(final long number, final boolean formatDate) {

        double hoursFraction = number / 1000.0D;

        int hours = (int) hoursFraction;
        hoursFraction -= hours;
        hoursFraction *= 60.0D;

        final int minutes = (int) hoursFraction;

        hoursFraction -= minutes;
        hoursFraction *= 60.0D;

        final int seconds = (int) hoursFraction;

        if ((hours <= 24) || !formatDate) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        int days = hours / 24;
        hours -= days * 24.0D;

        if (days > 9) {
            if (days >= 30) {
                int months = days / 30;
                days -= months * 30.0D;
                if (months >= 12) {
                    final int years = months / 12;
                    months -= years * 12.0D;
                    if (years >= 10) {
                        return String.format("> %dy", years);
                    } else {
                        return String.format("%dy %dm %dd", years, months, days);
                    }
                } else {

                    return String.format("%dm %dd", months, days);
                }
            } else {
                return String.format("%dd", days);
            }
        } else {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);

        }
    }

    /**
     * Converts a speed from AU/t into AU/h and formats the number
     *
     * @param number
     * @return
     */
    public static String formatSpeed(final double number) {
        // which is rather simple, since one MC hour is 1000 ticks
        return formatSpeed(number, false);
    }

    public static String formatSpeed(final double number, final boolean addSpace) {
        // which is rather simple, since one MC hour is 1000 ticks
        return formatMetric(number * 1000, "AU/h", addSpace);
    }

    public static String getGasName(final IAtmosphericGas gas) {
        return GCCoreUtil.translate(getGasNameUntranslated(gas));
    }

    public static String getGasNameUntranslated(final IAtmosphericGas gas) {
        switch (gas) {
            case ARGON:
                return "gas.argon.name";
            case CO2:
                return "gas.carbondioxide.name";
            case HELIUM:
                return "gas.helium.name";
            case HYDROGEN:
                return "gas.hydrogen.name";
            case METHANE:
                return "gas.methane.name";
            case NITROGEN:
                return "gas.nitrogen.name";
            case OXYGEN:
                return "gas.oxygen.name";
            case WATER:
                return "tile.water.name";
            default:
                return "item.baseItem.tricorder.message.unknownGas";

        }
    }

    /**
     * Equivalent to micdoodle8.mods.galacticraft.core.util.GCCoreUtil.translateWithSplit(String), but will also add a
     * colorcode to every line
     * 
     * @param key
     * @return
     */
    public static List<String> translateWithSplitColor(final String key, final EnumColor color) {
        String translated = StatCollector.translateToLocal(key);
        final int comment = translated.indexOf('#');
        translated = comment > 0 ? translated.substring(0, comment).trim() : translated;
        final String[] parts = translated.split("\\$");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = color.getCode() + parts[i];
        }
        return Arrays.asList(parts);
    }

}
