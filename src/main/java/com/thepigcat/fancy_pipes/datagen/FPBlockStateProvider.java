package com.thepigcat.fancy_pipes.datagen;

import com.thepigcat.fancy_pipes.FancyPipes;
import com.thepigcat.fancy_pipes.api.blocks.PipeBlock;
import com.thepigcat.fancy_pipes.registries.FPBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class FPBlockStateProvider extends BlockStateProvider {
    public FPBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, FancyPipes.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (Block block : FPBlocks.BLOCKS.getRegistry().get()) {
            if (block instanceof PipeBlock) {
                if (block == FPBlocks.WOODEN_ITEM_PIPE.get()) {
                    extractingPipeBlock(block);
                } else {
                    pipeBlock(block);
                }
            }
        }
    }

    private void pipeBlock(Block block) {
        ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(block);
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        pipeConnection(builder, loc, Direction.DOWN, 0, 0);
        pipeConnection(builder, loc, Direction.UP, 180, 0);
        pipeConnection(builder, loc, Direction.NORTH, 90, 180);
        pipeConnection(builder, loc, Direction.EAST, 90, 270);
        pipeConnection(builder, loc, Direction.SOUTH, 90, 0);
        pipeConnection(builder, loc, Direction.WEST, 90, 90);
        builder.part().modelFile(pipeBaseModel(loc)).addModel().end();
    }

    private void pipeConnection(MultiPartBlockStateBuilder builder, ResourceLocation loc, Direction direction, int x, int y) {
        builder.part().modelFile(pipeConnectionModel(loc)).rotationX(x).rotationY(y).addModel()
                .condition(PipeBlock.CONNECTION[direction.get3DDataValue()], PipeBlock.PipeState.CONNECTED).end();
    }

    private void extractingPipeBlock(Block block) {
        ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(block);
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        extractingPipeConnection(builder, loc, Direction.DOWN, 0, 0);
        extractingPipeConnection(builder, loc, Direction.UP, 180, 0);
        extractingPipeConnection(builder, loc, Direction.NORTH, 90, 180);
        extractingPipeConnection(builder, loc, Direction.EAST, 90, 270);
        extractingPipeConnection(builder, loc, Direction.SOUTH, 90, 0);
        extractingPipeConnection(builder, loc, Direction.WEST, 90, 90);
        builder.part().modelFile(pipeBaseModel(loc)).addModel().end();
    }

    private void extractingPipeConnection(MultiPartBlockStateBuilder builder, ResourceLocation loc, Direction direction, int x, int y) {
        builder.part().modelFile(pipeConnectionModel(loc)).rotationX(x).rotationY(y).addModel()
                .condition(PipeBlock.CONNECTION[direction.get3DDataValue()], PipeBlock.PipeState.CONNECTED).end()
                .part().modelFile(pipeExtractingModel(loc)).rotationX(x).rotationY(y).addModel()
                .condition(PipeBlock.CONNECTION[direction.get3DDataValue()], PipeBlock.PipeState.EXTRACTING).end();
    }

    private ModelFile pipeBaseModel(ResourceLocation blockLoc) {
        return models().withExistingParent(blockLoc.getPath()+"_base", modLoc("block/pipe_base"))
                .texture("texture", ResourceLocation.fromNamespaceAndPath(blockLoc.getNamespace(), "block/"+blockLoc.getPath()));
    }

    private ModelFile pipeConnectionModel(ResourceLocation blockLoc) {
        return models().withExistingParent(blockLoc.getPath()+"_connection", modLoc("block/pipe_connection"))
                .texture("texture", ResourceLocation.fromNamespaceAndPath(blockLoc.getNamespace(), "block/"+blockLoc.getPath()));
    }

    private ModelFile pipeExtractingModel(ResourceLocation blockLoc) {
        return models().withExistingParent(blockLoc.getPath()+"_connection_extracting", modLoc("block/pipe_connection"))
                .texture("texture", ResourceLocation.fromNamespaceAndPath(blockLoc.getNamespace(), "block/"+blockLoc.getPath()+"_extracting"));
    }

}
