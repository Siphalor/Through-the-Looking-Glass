package azzy.fabric.lookingglass.block;


import azzy.fabric.lookingglass.blockentity.BasePipeEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class PipeBlock extends LookingGlassBlock implements BlockEntityProvider {

    protected static final HashMap<Direction, BooleanProperty> FACING = new HashMap<>();
    public static final BooleanProperty CENTER = BooleanProperty.of("center");
    public static final DirectionProperty DIRECTION = Properties.FACING;
    public static final VoxelShape[] SHAPES;

    public PipeBlock(AbstractBlock.Settings settings, boolean loggable) {
        super(settings.nonOpaque(), loggable);
        for (Direction value : Direction.values()) {
            setDefaultState(getDefaultState().with(FACING.get(value), false));
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        BlockEntity entity = world.getBlockEntity(pos);
        if(entity instanceof BasePipeEntity)
            ((BasePipeEntity<?>) entity).checkConnections();
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CENTER, DIRECTION);
        for(Direction direction : Direction.values())
            builder.add(FACING.get(direction));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = SHAPES[0];
        if(state.get(CENTER))
            shape = VoxelShapes.union(shape, SHAPES[1]);
        if(state.get(FACING.get(Direction.NORTH)))
            shape = VoxelShapes.union(shape, SHAPES[2]);
        if(state.get(FACING.get(Direction.EAST)))
            shape = VoxelShapes.union(shape, SHAPES[3]);
        if(state.get(FACING.get(Direction.SOUTH)))
            shape = VoxelShapes.union(shape, SHAPES[4]);
        if(state.get(FACING.get(Direction.WEST)))
            shape = VoxelShapes.union(shape, SHAPES[5]);
        if(state.get(FACING.get(Direction.UP)))
            shape = VoxelShapes.union(shape, SHAPES[6]);
        if(state.get(FACING.get(Direction.DOWN)))
            shape = VoxelShapes.union(shape, SHAPES[7]);
        Direction middleDir = state.get(DIRECTION);
        if(middleDir == Direction.NORTH || middleDir == Direction.SOUTH)
            shape = VoxelShapes.union(shape, SHAPES[8]);
        else if(middleDir == Direction.EAST || middleDir == Direction.WEST)
            shape = VoxelShapes.union(shape, SHAPES[9]);
        else if(middleDir == Direction.UP || middleDir == Direction.DOWN)
            shape = VoxelShapes.union(shape, SHAPES[10]);
        return shape;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        BlockEntity entity = world.getBlockEntity(pos);
        if(entity instanceof BasePipeEntity)
            ((BasePipeEntity<?>) entity).checkConnections();
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }

    public static HashMap<Direction, BooleanProperty> getFACING() {
        return FACING;
    }

    public static void clearFacing(World world, BlockPos pos){
        for(Direction direction : Direction.values()){
            BlockState state = world.getBlockState(pos);
            world.setBlockState(pos, state.with(FACING.get(direction), false));
        }
    }


    static {
        for(Direction direction : Direction.values()){
            FACING.put(direction, BooleanProperty.of(direction.getName()));
        }

        SHAPES = new VoxelShape[] {
                Block.createCuboidShape(0, 0, 0, 0, 0, 0),
                Block.createCuboidShape(5, 5, 5, 11, 11, 11),
                Block.createCuboidShape(6, 6, 0, 10, 10, 5),
                Block.createCuboidShape(11, 6, 6, 16, 10, 10),
                Block.createCuboidShape(6, 6, 11, 10, 10, 16),
                Block.createCuboidShape(0, 6, 6, 5, 10, 10),
                Block.createCuboidShape(6, 11, 6, 10, 16, 10),
                Block.createCuboidShape(6, 0, 6, 10, 5, 10),
                Block.createCuboidShape(6, 6, 5, 10, 10, 11),
                Block.createCuboidShape(5, 6, 6, 11, 10, 10),
                Block.createCuboidShape(6, 5, 6, 10, 11, 10)
        };
    }
}
