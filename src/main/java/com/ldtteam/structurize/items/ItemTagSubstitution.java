package com.ldtteam.structurize.items;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.ISpecialBlockPickItem;
import com.ldtteam.structurize.api.util.Utils;
import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blockentities.ModBlockEntities;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.client.TagSubstitutionRenderer;
import com.ldtteam.structurize.network.messages.AbsorbBlockMessage;
import com.ldtteam.structurize.tag.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemTagSubstitution extends BlockItem implements ISpecialBlockPickItem
{
    public ItemTagSubstitution(@NotNull final Properties properties)
    {
        super(ModBlocks.blockTagSubstitution.get(), properties);
    }

    @Override
    public void initializeClient(@NotNull final Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return TagSubstitutionRenderer.getInstance();
            }
        });
    }

    @NotNull
    @Override
    public InteractionResult onBlockPick(@NotNull Player player,
                                         @NotNull ItemStack stack,
                                         @Nullable BlockPos pos,
                                         final boolean ctrlKey)
    {
        if (pos == null)
        {
            if (!player.level().isClientSide())
            {
                clearAbsorbedBlock(stack);
            }
            return InteractionResult.SUCCESS;
        }

        final BlockState blockstate = player.level().getBlockState(pos);
        if (blockstate.is(BlockTags.WITHER_IMMUNE))
        {
            // this way lies madness, and/or Sparta...
            if (!player.level().isClientSide())
            {
                clearAbsorbedBlock(stack);
            }
            return InteractionResult.SUCCESS;
        }

        if (player.level().isClientSide())
        {
            ItemStack pick = getPickedBlock(player, pos, blockstate);

            // sadly we can't use the default message since we want to pass an extra ItemStack...
            //   (and getCloneItemStack is client-side-only, somewhat strangely)
            Network.getNetwork().sendToServer(new AbsorbBlockMessage(pos, pick));
        }
        return InteractionResult.FAIL;
    }

    @NotNull
    private ItemStack getPickedBlock(@NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState blockstate)
    {
        return blockstate.getCloneItemStack(Minecraft.getInstance().hitResult, player.level(), pos, player);
    }

    private void clearAbsorbedBlock(@NotNull ItemStack stack)
    {
        setBlockEntityData(stack, ModBlockEntities.TAG_SUBSTITUTION.get(), new CompoundTag());
    }

    public void onAbsorbBlock(@NotNull final ServerPlayer player,
                              @NotNull final ItemStack stack,
                              @NotNull final BlockPos pos,
                              @NotNull final ItemStack absorbItem)
    {
        final BlockState blockstate = player.level().getBlockState(pos);
        final BlockEntity blockentity = player.level().getBlockEntity(pos);

        final BlockEntityTagSubstitution.ReplacementBlock replacement;
        if (blockentity instanceof BlockEntityTagSubstitution blockception)
        {
            replacement = blockception.getReplacement();
        }
        else if (!isAllowed(blockentity))
        {
            Utils.playErrorSound(player);
            return;
        }
        else
        {
            replacement = new BlockEntityTagSubstitution.ReplacementBlock(blockstate, blockentity, absorbItem);
        }

        setBlockEntityData(stack, ModBlockEntities.TAG_SUBSTITUTION.get(), replacement.write(new CompoundTag()));
    }

    private boolean isAllowed(@Nullable final BlockEntity blockentity)
    {
        if (blockentity == null) return true;

        final ITag<BlockEntityType<?>> tag = ForgeRegistries.BLOCK_ENTITY_TYPES.tags()
                .getTag(ModTags.SUBSTITUTION_ABSORB_WHITELIST);
        return tag.contains(blockentity.getType());
    }

    /**
     * Gets the absorbed replacement block from the stack.
     * @param stack the stack
     * @return the replacement block data (without loading blockentity)
     */
    @NotNull
    public BlockEntityTagSubstitution.ReplacementBlock getAbsorbedBlock(@NotNull ItemStack stack)
    {
        final CompoundTag tag = Objects.requireNonNullElse(getBlockEntityData(stack), new CompoundTag());
        return new BlockEntityTagSubstitution.ReplacementBlock(tag);
    }

    @Override
    public Component getHighlightTip(@NotNull final ItemStack stack, @NotNull final Component displayName)
    {
        final BlockEntityTagSubstitution.ReplacementBlock absorbed = getAbsorbedBlock(stack);
        if (!absorbed.isEmpty())
        {
            return Component.empty()
                    .append(super.getHighlightTip(stack, displayName))
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(absorbed.getItemStack().getHoverName());
        }

        return super.getHighlightTip(stack, displayName);
    }

    @NotNull
    @Override
    public Optional<TooltipComponent> getTooltipImage(@NotNull final ItemStack stack)
    {
        final BlockEntityTagSubstitution.ReplacementBlock absorbed = getAbsorbedBlock(stack);
        final ItemStack absorbedItem = absorbed.getItemStack();

        if (!absorbedItem.isEmpty())
        {
            return Optional.of(new ItemStackTooltip(absorbedItem));
        }

        return super.getTooltipImage(stack);
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(@NotNull final BlockPlaceContext context)
    {
        return super.getPlacementState(context);
    }
}
