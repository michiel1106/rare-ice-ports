package me.shedaniel.rareice.world.gen.feature;

import com.mojang.serialization.*;
import me.shedaniel.rareice.RareIce;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public class RareIceCountPlacement extends RepeatingPlacement {
    public static final MapCodec<RareIceCountPlacement> CODEC = MapCodec.unit(RareIceCountPlacement::new);

    protected int count(RandomSource randomSource, BlockPos blockPos) {
        return RareIce.probabilityOfRareIce;
    }

    @Override
    public PlacementModifierType<?> type() {
        return RareIce.COUNT_PLACEMENT.get();
    }
}
