package azzy.fabric.lookingglass.block;

import azzy.fabric.lookingglass.entity.ChunkLoaderEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

public class ChunkLoaderBlock extends Block implements BlockEntityProvider {

    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 14, 14);

    public ChunkLoaderBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity entity = world.getBlockEntity(pos);
        ItemStack stack = player.getStackInHand(hand);
        if(entity instanceof ChunkLoaderEntity && stack.getItem() == Items.GHAST_TEAR){
            ChunkLoaderEntity loaderEntity = (ChunkLoaderEntity) entity;
            ItemStack inventory = loaderEntity.getStack(0);
            if(inventory.getCount() < loaderEntity.getMaxCountPerStack() && !player.isInSneakingPose()){
                stack.decrement(1);
                if(inventory.isEmpty()){
                    loaderEntity.setStack(0, new ItemStack(Items.GHAST_TEAR));
                }
                else{
                    inventory.increment(1);
                }
            }
            else if(!inventory.isEmpty() && player.isInSneakingPose()){
                inventory.decrement(1);
                if(stack.isEmpty()){
                    player.setStackInHand(hand, new ItemStack(Items.GHAST_TEAR));
                }
                else{
                    stack.increment(1);
                }
            }
            return ActionResult.CONSUME;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        ChunkLoaderEntity loaderEntity = (ChunkLoaderEntity) world.getBlockEntity(pos);
        loaderEntity.requestCheck();
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient() && this.hasBlockEntity() && !state.isOf(newState.getBlock())) {
            ChunkLoaderEntity loaderEntity = (ChunkLoaderEntity) world.getBlockEntity(pos);
            loaderEntity.recalcChunks(16, (ServerWorld) world, ChunkLoaderEntity.UnloadAction.BREAKUNLOAD);
            ItemScatterer.spawn(world, pos, loaderEntity);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new ChunkLoaderEntity();
    }
}
