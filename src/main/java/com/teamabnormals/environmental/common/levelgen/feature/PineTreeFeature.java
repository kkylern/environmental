package com.teamabnormals.environmental.common.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.teamabnormals.blueprint.common.block.wood.LogBlock;
import com.teamabnormals.blueprint.common.levelgen.feature.BlueprintTreeFeature;
import com.teamabnormals.environmental.core.registry.EnvironmentalBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

import java.util.List;

public class PineTreeFeature extends BlueprintTreeFeature {

	public PineTreeFeature(Codec<TreeConfiguration> config) {
		super(config);
	}

	@Override
	public void doPlace(FeaturePlaceContext<TreeConfiguration> context) {
		TreeConfiguration config = context.config();
		BlockPos origin = context.origin();
		RandomSource random = context.random();

		int trunkheight = config.trunkPlacer.getTreeHeight(random);
		for (int y = 0; y < trunkheight; y++) {
			this.addLog(origin.above(y));
		}

		float f = random.nextFloat();
		int leafbranches = f > 0.6F ? 4 : f > 0.25F ? 3 : f > 0.05F ? 2 : 1;
        
        if (trunkheight >= 14 && random.nextBoolean())
            leafbranches++;

		List<Direction> branchdirections = Lists.newArrayList();
		Plane.HORIZONTAL.forEach(branchdirections::add);

		int y = trunkheight - 4;
		while (y > 0) {
			BlockPos blockpos = origin.above(y);

			if (leafbranches > 0) {
				Direction direction = branchdirections.remove(random.nextInt(branchdirections.size()));
				this.createBranchWithLeaves(blockpos, direction, random, config);
				leafbranches--;

				if (branchdirections.isEmpty()) {
					Plane.HORIZONTAL.forEach(branchdirections::add);
					branchdirections.remove(direction);
				}
			} else if (random.nextInt(6) == 0) {
				this.createBranch(blockpos, Plane.HORIZONTAL.getRandomDirection(random), random, config);
			}

			if (leafbranches == 0)
				y -= 2 + random.nextInt(2);
			else if (leafbranches == 1 && random.nextInt(3) == 0)
				y -= 2;
			else
				y--;
		}

		this.createTopLeaves(origin.above(trunkheight));
	}

	@Override
	public BlockState getSapling() {
		return EnvironmentalBlocks.PINE_SAPLING.get().defaultBlockState();
	}

	private void createBranch(BlockPos pos, Direction direction, RandomSource random, TreeConfiguration config) {
		BlockPos blockpos = pos.relative(direction);
		this.addSpecialLog(blockpos, config.trunkProvider.getState(random, blockpos).setValue(LogBlock.AXIS, direction.getAxis()));
	}

	private void createBranchWithLeaves(BlockPos pos, Direction direction, RandomSource random, TreeConfiguration config) {
		BlockPos.MutableBlockPos mutablepos = new BlockPos.MutableBlockPos();
		BlockPos blockpos = pos.relative(direction);

		this.createBranch(pos, direction, random, config);

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				mutablepos.setWithOffset(blockpos, x, 0, z);
				this.addFoliage(mutablepos);
				if ((x == 0 || z == 0)) {
					this.addFoliage(mutablepos.above());
				}
			}
		}

		if (random.nextInt(3) == 0) {
			this.addFoliage(blockpos.relative(direction, 2));
		}
	}

	private void createTopLeaves(BlockPos pos) {
		BlockPos.MutableBlockPos mutablepos = new BlockPos.MutableBlockPos();

		for (int y = 0; y <= 3; y++) {
			int r = y < 3 ? y : 1;
			for (int x = -r; x <= r; x++) {
				for (int z = -r; z <= r; z++) {
					if (Math.abs(x) + Math.abs(z) <= r) {
						mutablepos.setWithOffset(pos, x, 1 - y, z);
						this.addFoliage(mutablepos);
					}
				}
			}
		}
	}
}