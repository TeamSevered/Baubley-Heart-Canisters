package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CropsBlock extends BushBlock implements IGrowable {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_0_7;
   private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

   protected CropsBlock(Block.Properties builder) {
      super(builder);
      this.setDefaultState(this.stateContainer.getBaseState().with(this.getAgeProperty(), Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
      return SHAPE_BY_AGE[state.get(this.getAgeProperty())];
   }

   protected boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return state.getBlock() == Blocks.FARMLAND;
   }

   public IntegerProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 7;
   }

   protected int getAge(BlockState state) {
      return state.get(this.getAgeProperty());
   }

   public BlockState withAge(int age) {
      return this.getDefaultState().with(this.getAgeProperty(), Integer.valueOf(age));
   }

   public boolean isMaxAge(BlockState state) {
      return state.get(this.getAgeProperty()) >= this.getMaxAge();
   }

   public void func_225534_a_(BlockState p_225534_1_, ServerWorld p_225534_2_, BlockPos p_225534_3_, Random p_225534_4_) {
      super.func_225534_a_(p_225534_1_, p_225534_2_, p_225534_3_, p_225534_4_);
      if (!p_225534_2_.isAreaLoaded(p_225534_3_, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
      if (p_225534_2_.func_226659_b_(p_225534_3_, 0) >= 9) {
         int i = this.getAge(p_225534_1_);
         if (i < this.getMaxAge()) {
            float f = getGrowthChance(this, p_225534_2_, p_225534_3_);
            if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(p_225534_2_, p_225534_3_, p_225534_1_, p_225534_4_.nextInt((int)(25.0F / f) + 1) == 0)) {
               p_225534_2_.setBlockState(p_225534_3_, this.withAge(i + 1), 2);
               net.minecraftforge.common.ForgeHooks.onCropsGrowPost(p_225534_2_, p_225534_3_, p_225534_1_);
            }
         }
      }

   }

   public void grow(World worldIn, BlockPos pos, BlockState state) {
      int i = this.getAge(state) + this.getBonemealAgeIncrease(worldIn);
      int j = this.getMaxAge();
      if (i > j) {
         i = j;
      }

      worldIn.setBlockState(pos, this.withAge(i), 2);
   }

   protected int getBonemealAgeIncrease(World worldIn) {
      return MathHelper.nextInt(worldIn.rand, 2, 5);
   }

   protected static float getGrowthChance(Block blockIn, IBlockReader worldIn, BlockPos pos) {
      float f = 1.0F;
      BlockPos blockpos = pos.down();

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            float f1 = 0.0F;
            BlockState blockstate = worldIn.getBlockState(blockpos.add(i, 0, j));
            if (blockstate.canSustainPlant(worldIn, blockpos.add(i, 0, j), net.minecraft.util.Direction.UP, (net.minecraftforge.common.IPlantable)blockIn)) {
               f1 = 1.0F;
               if (blockstate.isFertile(worldIn, blockpos.add(i, 0, j))) {
                  f1 = 3.0F;
               }
            }

            if (i != 0 || j != 0) {
               f1 /= 4.0F;
            }

            f += f1;
         }
      }

      BlockPos blockpos1 = pos.north();
      BlockPos blockpos2 = pos.south();
      BlockPos blockpos3 = pos.west();
      BlockPos blockpos4 = pos.east();
      boolean flag = blockIn == worldIn.getBlockState(blockpos3).getBlock() || blockIn == worldIn.getBlockState(blockpos4).getBlock();
      boolean flag1 = blockIn == worldIn.getBlockState(blockpos1).getBlock() || blockIn == worldIn.getBlockState(blockpos2).getBlock();
      if (flag && flag1) {
         f /= 2.0F;
      } else {
         boolean flag2 = blockIn == worldIn.getBlockState(blockpos3.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.south()).getBlock() || blockIn == worldIn.getBlockState(blockpos3.south()).getBlock();
         if (flag2) {
            f /= 2.0F;
         }
      }

      return f;
   }

   public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
      return (worldIn.func_226659_b_(pos, 0) >= 8 || worldIn.func_226660_f_(pos)) && super.isValidPosition(state, worldIn, pos);
   }

   public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
      if (entityIn instanceof RavagerEntity && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entityIn)) {
         worldIn.func_225521_a_(pos, true, entityIn);
      }

      super.onEntityCollision(state, worldIn, pos, entityIn);
   }

   protected IItemProvider getSeedsItem() {
      return Items.WHEAT_SEEDS;
   }

   public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
      return new ItemStack(this.getSeedsItem());
   }

   /**
    * Whether this IGrowable can grow
    */
   public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
      return !this.isMaxAge(state);
   }

   public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state) {
      return true;
   }

   public void func_225535_a_(ServerWorld p_225535_1_, Random p_225535_2_, BlockPos p_225535_3_, BlockState p_225535_4_) {
      this.grow(p_225535_1_, p_225535_3_, p_225535_4_);
   }

   protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }
}