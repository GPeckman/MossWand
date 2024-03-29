package net.gpeck.mosswand.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.gpeck.mosswand.config.ModConfig;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.block.enums.WallShape;
import net.minecraft.util.math.Direction;

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
			if (ModConfig.get().useBoneMeal && !user.getAbilities().creativeMode && boneMeal.isEmpty()) {
				return ActionResult.PASS;
			} else if (ModConfig.get().useBoneMeal && !user.getAbilities().creativeMode) {
				boneMeal.decrement(1);
				//If the bone meal stack is made empty, then remove it
				if (boneMeal.isEmpty()) {
					user.getInventory().removeOne(boneMeal);
				}
			}
			
			w.playSound(context.getPlayer(), pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
			//This part ensures that block data is preserved
			if (!w.isClient) {
				if (target.getBlock() instanceof StairsBlock) {
					Direction rot = target.get(StairsBlock.FACING);
					boolean water = target.get(StairsBlock.WATERLOGGED);
					BlockHalf half = target.get(StairsBlock.HALF);
					StairShape shape = target.get(StairsBlock.SHAPE);
					state = (BlockState)state.with(StairsBlock.FACING, rot).with(StairsBlock.WATERLOGGED, water).with(StairsBlock.HALF, half).with(StairsBlock.SHAPE, shape);
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
		for (int i = 0; i < player.getInventory().size(); ++i) {
			ItemStack stack = player.getInventory().getStack(i);
			if (boneMealPred.test(stack)) return stack;
		}
		return ItemStack.EMPTY;
	}
	
	@Environment(value=EnvType.CLIENT)
	public static void createParticles(WorldAccess world, BlockPos pos, int count) {
		Random random = world.getRandom();
		for (int i = 0; i < count; ++i) {
			//Add a small, random amount of velocity
			double xVel = random.nextGaussian() * 0.02;
			double yVel = random.nextGaussian() * 0.02;
			double zVel = random.nextGaussian() * 0.02;
			
			double x = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 3;
			double y = (double)pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 3;
			double z = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 3;
			
			world.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, xVel, yVel, zVel);
		}
	}
}
