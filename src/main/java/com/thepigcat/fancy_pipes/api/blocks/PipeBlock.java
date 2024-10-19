package com.thepigcat.fancy_pipes.api.blocks;

import com.thepigcat.fancy_pipes.api.blockentities.PipeBlockEntity;
import com.thepigcat.fancy_pipes.content.blockentities.ItemPipeBE;
import com.thepigcat.fancy_pipes.registries.FPBlockEntities;
import com.thepigcat.fancy_pipes.registries.FPItems;
import com.thepigcat.fancy_pipes.util.BlockUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class PipeBlock extends BaseEntityBlock {
    public static final EnumProperty<PipeState>[] CONNECTION = new EnumProperty[6];

    public final int border;
    public final VoxelShape shapeCenter;
    public final VoxelShape shapeD;
    public final VoxelShape shapeU;
    public final VoxelShape shapeN;
    public final VoxelShape shapeS;
    public final VoxelShape shapeW;
    public final VoxelShape shapeE;
    public final VoxelShape[] shapes;

    static {
        for (Direction dir : Direction.values()) {
            CONNECTION[dir.get3DDataValue()] = EnumProperty.create(dir.getSerializedName(), PipeState.class);
        }
    }

    public PipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(CONNECTION[0], PipeState.NONE)
                .setValue(CONNECTION[1], PipeState.NONE)
                .setValue(CONNECTION[2], PipeState.NONE)
                .setValue(CONNECTION[3], PipeState.NONE)
                .setValue(CONNECTION[4], PipeState.NONE)
                .setValue(CONNECTION[5], PipeState.NONE)
        );
        int width = 10;
        border = (16 - width) / 2;
        int B0 = border;
        int B1 = 16 - border;
        shapeCenter = box(B0, B0, B0, B1, B1, B1);
        shapeD = box(B0, 0, B0, B1, B0, B1);
        shapeU = box(B0, B1, B0, B1, 16, B1);
        shapeN = box(B0, B0, 0, B1, B1, B0);
        shapeS = box(B0, B0, B1, B1, B1, 16);
        shapeW = box(0, B0, B0, B0, B1, B1);
        shapeE = box(B1, B0, B0, 16, B1, B1);
        shapes = new VoxelShape[64];
    }


    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        int index = 0;

        for (Direction direction : Direction.values()) {
            if (blockState.getValue(CONNECTION[direction.ordinal()]) != PipeState.NONE) {
                index |= 1 << direction.ordinal();
            }
        }

        return getShape(index);
    }

    public VoxelShape getShape(int i) {
        if (shapes[i] == null) {
            shapes[i] = shapeCenter;

            if (((i >> 0) & 1) != 0) {
                shapes[i] = Shapes.or(shapes[i], shapeD);
            }

            if (((i >> 1) & 1) != 0) {
                shapes[i] = Shapes.or(shapes[i], shapeU);
            }

            if (((i >> 2) & 1) != 0) {
                shapes[i] = Shapes.or(shapes[i], shapeN);
            }

            if (((i >> 3) & 1) != 0) {
                shapes[i] = Shapes.or(shapes[i], shapeS);
            }

            if (((i >> 4) & 1) != 0) {
                shapes[i] = Shapes.or(shapes[i], shapeW);
            }

            if (((i >> 5) & 1) != 0) {
                shapes[i] = Shapes.or(shapes[i], shapeE);
            }
        }

        return shapes[i];
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return FPBlockEntities.COBBLESTONE_PIPE.get().create(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, FPBlockEntities.COBBLESTONE_PIPE.get(), (beLevel, bePos, beState, be) -> be.tick());
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONNECTION[0], CONNECTION[1], CONNECTION[2], CONNECTION[3], CONNECTION[4], CONNECTION[5]);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState blockState, Direction facingDirection, BlockState facingBlockState, LevelAccessor level, BlockPos blockPos, BlockPos facingBlockPos) {
        int connectionIndex = facingDirection.ordinal();
        BlockEntity blockEntity = level.getBlockEntity(facingBlockPos);
        PipeBlockEntity<?> pipeBE = BlockUtils.getBe(PipeBlockEntity.class, level, blockPos);
        if (facingBlockState.is(this) || (blockEntity != null && canConnectTo(blockEntity))) {
            pipeBE.getDirections().add(facingDirection);
            return blockState.setValue(CONNECTION[connectionIndex], PipeState.CONNECTED);
        } else if (facingBlockState.isEmpty()) {
            pipeBE.getDirections().remove(facingDirection);
            return blockState.setValue(CONNECTION[connectionIndex], PipeState.NONE);
        }

        return blockState;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = defaultBlockState();

        for (Direction direction : Direction.values()) {
            int connectionIndex = direction.ordinal();
            BlockPos facingBlockPos = blockPos.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(facingBlockPos);

            if (blockEntity != null && canConnectTo(blockEntity)) {
                blockState = blockState.setValue(CONNECTION[connectionIndex], PipeState.CONNECTED);
            }
        }

        return blockState;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        PipeBlockEntity<?> be = BlockUtils.getBe(PipeBlockEntity.class, level, pos);
        be.setDirections(connectionsToDirections(state));
    }

    private static Set<Direction> connectionsToDirections(BlockState state) {
        Set<Direction> directions = new ObjectArraySet<>();
        for (Direction direction : Direction.values()) {
            if (state.getValue(CONNECTION[direction.ordinal()]) != PipeState.NONE) {
                directions.add(direction);
            }
        }
        return directions;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemPipeBE be = BlockUtils.getBe(ItemPipeBE.class, level, pos);
        if (stack.is(FPItems.WRENCH.get())) {
            be.extracting = hitResult.getDirection();
        } else {
            Direction direction = be.to;
            if (direction != null) {
                player.sendSystemMessage(Component.literal("Pos: " + pos.relative(direction)));
            } else {
                player.sendSystemMessage(Component.literal(":skull:"));
            }
            player.sendSystemMessage(Component.literal("dirs: " + be.getDirections()));
        }
        return ItemInteractionResult.SUCCESS;
    }

    public abstract boolean canConnectTo(BlockEntity connectTo);

    public enum PipeState implements StringRepresentable {
        EXTRACTING("extracting"),
        CONNECTED("connected"),
        NONE("none");

        private final String name;

        PipeState(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}