package com.knightmere.tree_felling.events;

import com.knightmere.tree_felling.TreeFellingMod;
import net.minecraft.block.*;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.TimeUnit;


@Mod.EventBusSubscriber(modid = TreeFellingMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BreakLogEvent {


    /**
     * If player breaks a log while crouching with axe in hand,
     * it will create a chain reaction to start the felling process.
     */
    @SubscribeEvent
    public static void onLogBreak(BlockEvent.BreakEvent event){
        PlayerEntity player = event.getPlayer();

        ItemStack stack = player.getMainHandItem();

        if (stack.getItem() instanceof AxeItem && player.isCrouching()) {

            BlockState state = event.getState();
            BlockPos pos = event.getPos();

            if (state.is(BlockTags.LOGS)) {

                // Make logs fall
                makeLogsFallRecursive(state,player,pos.above(), false);
            }
        }
    }


    /**
     * A recursive function that will make tree fall like
     * sand or gravel.
     * @param state - the blockState that are going to fall
     * @param player - the player that triggered this
     * @param blockPos - the position of the next block to fall
     * @param isBranch - if it is not directly above the block player broke, it is true.
     */
    private static void makeLogsFallRecursive(BlockState state, PlayerEntity player, BlockPos blockPos, boolean isBranch){

            // If the block at this position is not equal to the block that triggered the event, just return early.
            if (player.level.getBlockState(blockPos).getBlock() != state.getBlock()) return;


            // If it is a branch, return early if there is a solid block below, or remove it if there is leaves
            if (isBranch){
                if (player.level.getBlockState(blockPos.below()).is(BlockTags.LEAVES)){
                    player.level.destroyBlock(blockPos.below(),true);
                    if (player.level.getBlockState(blockPos.below().below()).is(BlockTags.LEAVES))
                        player.level.destroyBlock(blockPos.below().below(),true);
                }

                if (!player.level.getBlockState(blockPos.below()).isAir()) {
                    return;
                }
            }


            FallingBlockEntity fallingLogEntity = new FallingBlockEntity(player.level,blockPos.getX()+0.5, blockPos.getY(), blockPos.getZ()+0.5, state);

            fallingLogEntity.time = 3;
            fallingLogEntity.dropItem = false; // If the falling log is "killed" or destroyed before it hits the ground
                                               // and turns into a solid block, it will drop nothing.

            // Replace solid block (without making it drop item) with a falling block.
            player.level.removeBlock(blockPos, false);
            player.level.addFreshEntity(fallingLogEntity);

            // Repeat recursively:

            // .. beside
            makeLogsFallRecursive(state, player, blockPos.north(), true);
            makeLogsFallRecursive(state, player, blockPos.west(),true);
            makeLogsFallRecursive(state, player, blockPos.south(),true);
            makeLogsFallRecursive(state, player, blockPos.east(),true);

            // ..diagonal
            makeLogsFallRecursive(state, player, blockPos.north().east(),true);
            makeLogsFallRecursive(state, player, blockPos.north().west(),true);
            makeLogsFallRecursive(state, player, blockPos.south().east(),true);
            makeLogsFallRecursive(state, player, blockPos.south().west(),true);

            // ..up beside
            makeLogsFallRecursive(state, player, blockPos.above().north(),true);
            makeLogsFallRecursive(state, player, blockPos.above().west(),true);
            makeLogsFallRecursive(state, player, blockPos.above().south(),true);
            makeLogsFallRecursive(state, player, blockPos.above().east(),true);

            // ..up diagonal
            makeLogsFallRecursive(state, player, blockPos.above().north().west(),true);
            makeLogsFallRecursive(state, player, blockPos.above().north().east(),true);
            makeLogsFallRecursive(state, player, blockPos.above().south().west(),true);
            makeLogsFallRecursive(state, player, blockPos.above().south().east(),true);

            // .. straight up
            makeLogsFallRecursive(state, player, blockPos.above(),false);
    }
}
