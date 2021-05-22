package com.alc.moreminecarts.containers;

import com.alc.moreminecarts.MMReferences;
import com.alc.moreminecarts.proxy.MoreMinecartsPacketHandler;
import com.alc.moreminecarts.tile_entities.MinecartLoaderTile;
import com.alc.moreminecarts.tile_entities.MinecartUnloaderTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MinecartUnLoaderContainer extends Container {

    private final IInventory inventory;
    private final IIntArray data;
    protected final World level;

    public MinecartUnLoaderContainer(int n, World world, PlayerInventory player_inventory, PlayerEntity player_entity) {
        super(MMReferences.minecart_loader_c, n);

        this.inventory = new Inventory(1);
        this.data = new IntArray(2);
        this.level = player_inventory.player.level;

        CommonInitialization(player_inventory);
    }

    // For use with tile entity loaders.
    public MinecartUnLoaderContainer(int n, World world, BlockPos pos, PlayerInventory player_inventory, PlayerEntity player_entity) {
        super(MMReferences.minecart_loader_c, n);

        TileEntity te = world.getBlockEntity(pos);

        if (te instanceof MinecartLoaderTile) {
            MinecartLoaderTile tile = (MinecartLoaderTile) te;
            this.inventory = tile;
            this.data = tile.dataAccess;
        } else if (te instanceof MinecartUnloaderTile) {
            MinecartUnloaderTile tile = (MinecartUnloaderTile) te;
            this.inventory = tile;
            this.data = tile.dataAccess;
        } else {
            // should error out?
            this.inventory = new Inventory(1);
            this.data = new IntArray(2);
        }

        this.level = player_inventory.player.level;

        CommonInitialization(player_inventory);
    }

    public void CommonInitialization(PlayerInventory player_inventory) {

        checkContainerSize(inventory, 9);
        checkContainerDataCount(data, 2);

        // content slots
        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 36));
        }

        // player inventory slots, taken from the AbstractFurnaceContainer code.
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(player_inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(player_inventory, k, 8 + k * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return this.inventory.stillValid(player);
    }

    // Taken from the chest container. No clue what this does really.
    @Override
    public ItemStack quickMoveStack(PlayerEntity p_82846_1_, int p_82846_2_) {
        int containerRows = 1;
        ItemStack lvt_3_1_ = ItemStack.EMPTY;
        Slot lvt_4_1_ = (Slot)this.slots.get(p_82846_2_);
        if (lvt_4_1_ != null && lvt_4_1_.hasItem()) {
            ItemStack lvt_5_1_ = lvt_4_1_.getItem();
            lvt_3_1_ = lvt_5_1_.copy();
            if (p_82846_2_ < containerRows * 9) {
                if (!this.moveItemStackTo(lvt_5_1_, containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(lvt_5_1_, 0, containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (lvt_5_1_.isEmpty()) {
                lvt_4_1_.set(ItemStack.EMPTY);
            } else {
                lvt_4_1_.setChanged();
            }
        }

        return lvt_3_1_;
    }

    @OnlyIn(Dist.CLIENT)
    public int getSize() {
        return 9;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean getLockedMinecartsOnly()
    {
        return this.data.get(0) % 2 == 1;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean getLeaveOneInStack()
    {
        return (this.data.get(0) & 2) == 2;
    }

    @OnlyIn(Dist.CLIENT)
    public MinecartLoaderTile.ComparatorOutputType getComparatorOutputType()
    {
        return MinecartLoaderTile.ComparatorOutputType.fromInt(this.data.get(0) >> 2);
    }

    @OnlyIn(Dist.CLIENT)
    public MoreMinecartsPacketHandler.MinecartLoaderPacket getCurrentPacket() {
        return new MoreMinecartsPacketHandler.MinecartLoaderPacket(
                false,
                getLockedMinecartsOnly(),
                getLeaveOneInStack(),
                getComparatorOutputType()
        );
    }

    @OnlyIn(Dist.CLIENT)
    public boolean getIsUnloader() {
        return this.data.get(1) > 0;
    }

    public void setOptions(boolean locked_minecarts_only, boolean leave_one_in_stack, MinecartLoaderTile.ComparatorOutputType comparator_output) {
       int options = (locked_minecarts_only?1:0) + ((leave_one_in_stack?1:0) << 1) + (comparator_output.toInt() << 2);
       this.setData(0, options);
       this.broadcastChanges();
    }
}