package com.bavde1.lifespren.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DoubleProperty extends Property<Double> {
    private final ImmutableSet<Double> values;

    protected DoubleProperty(String name, double value) {
        super(name, Double.class);

        /*if (value >= Double.MAX_VALUE) {
            throw new IllegalArgumentException("Value of " + name + " is too great: " + value + " | Max double value: " + Double.MAX_VALUE);
        } else if (value <= Double.MIN_VALUE) {
            throw new IllegalArgumentException("Value of " + name + " is too low: " + value + " | Min double value: " + Double.MIN_VALUE);
        }*/

        Set<Double> set = Sets.newHashSet();
        set.add(value);
        set.add(RandomSource.create().nextDouble());

        this.values = ImmutableSet.copyOf(set);
    }

    public static DoubleProperty create(String name, double value) {
        return new DoubleProperty(name, value);
    }

    @Override
    public Collection<Double> getPossibleValues() {
        return this.values;
    }

    @Override
    public String getName(Double name) {
        int i = (int) Math.floor(name);
        return Integer.toString(i);
    }

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else if (pOther instanceof DoubleProperty && super.equals(pOther)) {
            DoubleProperty d = (DoubleProperty)pOther;
            return this.values.equals(d.values);
        } else {
            return false;
        }
    }

    @Override
    public Optional<Double> getValue(String value) {
        try {
            Double d = Double.valueOf(value);
            return Optional.of(d);
        } catch (NumberFormatException numberformatexception) {
            return Optional.empty();
        }
    }
}
