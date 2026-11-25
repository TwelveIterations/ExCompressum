package net.blay09.mods.excompressum;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.fluid.FluidTank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.math.BigInteger;

public class FluidUnits {
    public static final int BUCKET = Balm.platform().name().equals("fabric") ? 81000 : 1000;
    public static final int MILLIBUCKET = BUCKET / 1000;

    public static int toMillibuckets(int amount) {
        return amount / MILLIBUCKET;
    }

    public static void serialize(FluidTank tank, ValueOutput output) {
        output.putString("Fluid", BuiltInRegistries.FLUID.getKey(tank.getFluid(0)).toString());
        output.putLong("Amount", tank.getAmount(0));
        output.putString("Unit", BUCKET == 81000 ? "droplets" : "millibuckets");
    }

    public static void deserialize(FluidTank tank, ValueInput input, int capacity) {
        int savedBucketVolume = switch (input.getStringOr("Unit", "")) {
            case "buckets" -> 1;
            case "droplets" -> 81000;
            case "", "millibuckets" -> 1000;
            default -> BUCKET;
        };
        BigInteger amount = BigInteger.valueOf(input.getLongOr("Amount", 0))
                .multiply(BigInteger.valueOf(BUCKET))
                .divide(BigInteger.valueOf(savedBucketVolume));
        final var fluid = input.getString("Fluid").map(Identifier::tryParse).map(BuiltInRegistries.FLUID::getValue).orElse(Fluids.EMPTY);
        tank.setFluid(0, fluid, amount.max(BigInteger.ZERO).min(BigInteger.valueOf(capacity)).intValueExact());
    }
}
