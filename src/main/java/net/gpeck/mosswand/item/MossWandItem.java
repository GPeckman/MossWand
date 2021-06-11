package net.gpeck.mosswand.item;

import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.block.*;
import net.minecraft.block.enums.*;
import net.minecraft.world.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.gpeck.mosswand.config.ModConfig;
import java.util.*;
import java.util.function.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class MossWandItem extends Item {
	static final Map<Block, BlockState> TARGET_BLOCKS = Maps.newHashMap(ImmutableMap.<Block, BlockState>builder()
															.put(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS.getDefaultState())
															.put(Blocks.STONE_BRICK_STAIRS, Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState())
															.put(Blocks.STONE_BRICK_WALL, Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState())
															.put(Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS.getDefaultState())
															.put(Blocks.STONE_BRICK_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState())
															.put(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE.getDefaultState())
															.put(Blocks.COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB.getDefaultState())
															.put(Blocks.COBBLESTONE_STAIRS, Blocks.MOSSY_COBBLESTONE_STAIRS.getDefaultState())
															.put(Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL.getDefaultState()).build());
	static final Predicate<ItemStack> boneMealPred = stack -> stack.getItem() == Items.BONE_MEAL;
	//static boolean needBoneMeal = ModConfig.get().useBoneMeal;
	//static boolean needBoneMeal = true;
	
	public MossWandItem(Settings settings) {
		super(settings);
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World w = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = TARGET_BLOCKS.get(w.getBlockState(pos).getBlock());
		
		if (state != null) {
			PlayerEntity user = context.getPlayer();
			BlockState target = w.getBlockState(pos);
			ItemStack boneMeal = getBoneMeal(user);
			//If bone meal is needed and isn't present, then return. Otherwise, decrement
			//the bone meal stack (if bone meal is needed) and continue
			if (ModConfig.get().useBoneMeal && !user.abilities.creativeMode && boneMeal.isEmpty()) {
				return ActionResult.PASS;
			} else if (ModConfig.get().useBoneMeal && !user.abilities.creativeMode) {
				boneMeal.decrement(1);
				//If the bone meal stack is made empty, then remove it
				if (boneMeal.isEmpty()) {
					user.inventory.removeOne(boneMeal);
				}
			}
			
			//This part ensures that block data is preserved
			if (!w.isClient) {
				if (target.getBlock() instanceof StairsBlock) {
					Direction rot = target.get(StairsBlock.FACING);
					boolean water = target.get(StairsBlock.WATERLOGGED);
					BlockHalf half = target.get(StairsBlock.HALF);
					state = (BlockState)state.with(StairsBlock.FACING, rot).with(StairsBlock.WATERLOGGED, water).with(StairsBlock.HALF, half);
				} else if (target.getBlock() instanceof WallBlock) {
					boolean up = target.get(WallBlock.UP);
					WallShape eastShape = target.get(WallBlock.EAST_SHAPE);
					WallShape northShape = target.get(WallBlock.NORTH_SHAPE);
					WallShape southShape = target.get(WallBlock.SOUTH_SHAPE);
					WallShape westShape = target.get(WallBlock.WEST_SHAPE);
					boolean water = target.get(WallBlock.WATERLOGGED);
					state = (BlockState)state.with(WallBlock.UP, up).with(WallBlock.EAST_SHAPE, eastShape).with(WallBlock.NORTH_SHAPE, northShape).with(WallBlock.SOUTH_SHAPE, southShape).with(WallBlock.WEST_SHAPE, westShape).with(WallBlock.WATERLOGGED, water);
				} else if (target.getBlock() instanceof SlabBlock) {
					SlabType type = target.get(SlabBlock.TYPE);
					boolean water = target.get(SlabBlock.WATERLOGGED);
					state = (BlockState)state.with(SlabBlock.TYPE, type).with(SlabBlock.WATERLOGGED, water);
				}
				w.setBlockState(pos, state, 0);
			}
			createParticles(w, pos, 15);
			return ActionResult.success(w.isClient);
		}
		//createParticles(w, pos, 15); //debug
		return ActionResult.PASS;
	}
	
	public ItemStack getBoneMeal(PlayerEntity player) {
		for (int i = 0; i < player.inventory.size(); ++i) {
			ItemStack stack = player.inventory.getStack(i);
			if (boneMealPred.test(stack)) return stack;
		}
		return ItemStack.EMPTY;
	}
	
	@Environment(value=EnvType.CLIENT)
	public static void createParticles(WorldAccess world, BlockPos pos, int count) {
		for (int i = 0; i < count; ++i) {
			//Add a small, random amount of velocity
			double xVel = RANDOM.nextGaussian() * 0.02;
			double yVel = RANDOM.nextGaussian() * 0.02;
			double zVel = RANDOM.nextGaussian() * 0.02;
			
			double x = (double)pos.getX() + 0.5 + (RANDOM.nextDouble() - 0.5) * 3;
			double y = (double)pos.getY() + 0.5 + (RANDOM.nextDouble() - 0.5) * 3;
			double z = (double)pos.getZ() + 0.5 + (RANDOM.nextDouble() - 0.5) * 3;
			
			world.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, xVel, yVel, zVel);
		}
	}
}
