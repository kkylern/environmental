package com.teamabnormals.environmental.common.levelgen.feature;

import com.mojang.serialization.Codec;
import com.teamabnormals.blueprint.common.levelgen.feature.BlueprintTreeFeature;
import com.teamabnormals.environmental.common.block.WisteriaLeavesBlock;
import com.teamabnormals.environmental.core.registry.EnvironmentalBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

import java.util.Set;

public class WisteriaTreeFeature extends BlueprintTreeFeature {

	public WisteriaTreeFeature(Codec<TreeConfiguration> config) {
		super(config);
	}

	@Override
	public void doPlace(FeaturePlaceContext<TreeConfiguration> context) {
		TreeConfiguration config = context.config();
		RandomSource random = context.random();
		BlockPos origin = context.origin();

		int trunkHeight = config.trunkPlacer.getTreeHeight(random);
		for (int y = 0; y < trunkHeight; y++) {
			this.addLog(origin.above(y));
		}

		MutableBlockPos pos = new MutableBlockPos();
		pos.set(origin.above(trunkHeight - 2).relative(Plane.HORIZONTAL.getRandomDirection(random)));

		for (int i = 0; i < 3; i++) {
			this.addLog(pos.set(pos.above()));
		}

		pos.set(pos.below().relative(Plane.HORIZONTAL.getRandomDirection(random)));
		for (int i = 0; i < 4; i++) {
			this.addLog(pos.set(pos.above()));
		}

		pos.set(pos.below());
		Direction direction = Plane.HORIZONTAL.getRandomDirection(random);
		Direction offset = direction.getClockWise().getOpposite();
		Set<Direction> directions = Set.of(direction, direction.getOpposite(), direction.getClockWise());
		directions.forEach(branchDirection -> {
			BlockPos branchPos = branchDirection == direction ? pos.relative(offset) : pos;
			createBranch(branchPos.relative(branchDirection).above(random.nextInt(2)), branchDirection, random, config);
		});
	}

	private void createBranch(BlockPos pos, Direction direction, RandomSource random, TreeConfiguration config) {
		MutableBlockPos mutablePos = new MutableBlockPos();
		mutablePos.set(pos);

		this.addLog(mutablePos);
		this.addLog(mutablePos.set(mutablePos.relative(direction)));
		this.addLog(mutablePos.set(mutablePos.relative(direction).above()));

		this.createLeafLayer(mutablePos.above(), false, random, EnvironmentalBlocks.WISTERIA_LEAVES.get().defaultBlockState());
		this.createLeafLayer(mutablePos.above(), true, random, EnvironmentalBlocks.WISTERIA_LEAVES.get().defaultBlockState(), 3);
		this.createLeafLayer(mutablePos, true, random, config.foliageProvider.getState(random, pos).setValue(WisteriaLeavesBlock.HALF, Half.TOP));
		this.createLeafLayer(mutablePos.below(), true, random, config.foliageProvider.getState(random, pos));
		this.createLeafLayer(mutablePos.below(2), true, random, config.foliageProvider.getState(random, pos), 3);
	}

	private void createLeafLayer(BlockPos pos, boolean square, RandomSource random, BlockState state) {
		this.createLeafLayer(pos, square, random, state, 0);
	}

	private void createLeafLayer(BlockPos pos, boolean square, RandomSource random, BlockState state, int chance) {
		int leafSize = 1;
		for (int i = -leafSize; i <= leafSize; ++i) {
			for (int k = -leafSize; k <= leafSize; ++k) {
				if (chance == 0 || random.nextInt(chance) == 0) {
					if (square) {
						this.addSpecialFoliage(pos.offset(i, 0, k), state);
					} else if ((Math.abs(i) != leafSize || Math.abs(k) != leafSize)) {
						this.addSpecialFoliage(pos.offset(i, 0, k), state);
					}
				}
			}
		}
	}

	@Override
	public BlockState getSapling() {
		return EnvironmentalBlocks.BLUE_WISTERIA_SAPLING.get().defaultBlockState();
	}
}