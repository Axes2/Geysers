package com.axes2.geysers.block;

import com.axes2.geysers.block.entity.GeyserVentBlockEntity;
import com.axes2.geysers.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The vent block. Hosts the {@link GeyserVentBlockEntity} and drives its server tick.
 * The client ticker (for particle emission) is added in M2.
 */
public class GeyserVentBlock extends Block implements EntityBlock {
    public GeyserVentBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeyserVentBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (type != ModBlockEntities.GEYSER_VENT.get()) {
            return null;
        }
        if (level.isClientSide) {
            return null; // client ticker (particles) added in M2
        }
        return (lvl, pos, blockState, blockEntity) ->
                ((GeyserVentBlockEntity) blockEntity).serverTick(lvl, pos, blockState);
    }
}
