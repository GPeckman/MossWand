package net.gpeck.mosswand.item;

import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.block.*;
import net.minecraft.world.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.gpeck.mosswand.config.ModConfig;
import java.util.*;
import java.util.function.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class CrackedWandItem extends Item {
	static final Map<Block, BlockState> TARGET_BLOCKS = Maps.newHashMap(ImmutableMap.<Block, BlockState>builder()
															.put(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS.getDefaultState())
															.put(Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS.getDefaultState())
															.put(Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS.getDefaultState())
															.put(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()).build());
	static final Predicate<ItemStack> coalPred = stack -> stack.getItem() == Items.COAL;
	static final Predicate<ItemStack> charcoalPred = stack -> stack.getItem() == Items.CHARCOAL;
	static final Predicate<ItemStack> coalCharcoalPred = coalPred.or(charcoalPred);
	
	public CrackedWandItem(Settings settings) {
		super(settings);
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World w = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = TARGET_BLOCKS.get(w.getBlockState(pos).getBlock());
		
		if (state != null) {
			PlayerEntity user = context.getPlayer();
			ItemStack coalCharcoal = getCoalCharcoal(user);
			if (ModConfig.get().useCoalCharcoal && !user.abilities.creativeMode && coalCharcoal.isEmpty()) {
				return ActionResult.PASS;
			} else if (ModConfig.get().useCoalCharcoal && !user.abilities.creativeMode) {
				coalCharcoal.decrement(1);
				if (coalCharcoal.isEmpty()) {
					user.inventory.removeOne(coalCharcoal);
				}
			}
			
			if (!w.isClient) {
				w.setBlockState(pos, state, 0);
			}
			
			createParticles(w, pos, 15);
			return ActionResult.success(w.isClient);
		}
		return ActionResult.PASS;
	}
	
	public ItemStack getCoalCharcoal(PlayerEntity player) {
		for (int i = 0; i < player.inventory.size(); ++i) {
			ItemStack stack = player.inventory.getStack(i);
			if (coalCharcoalPred.test(stack)) return stack;
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
