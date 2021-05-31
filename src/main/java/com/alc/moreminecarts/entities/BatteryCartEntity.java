package com.alc.moreminecarts.entities;

import com.alc.moreminecarts.MMItemReferences;
import com.alc.moreminecarts.MMReferences;
import com.alc.moreminecarts.blocks.PistonDisplayBlock;
import com.alc.moreminecarts.containers.BatteryCartContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;


public class BatteryCartEntity extends AbstractMinecartEntity implements INamedContainerProvider {

    public static String ENERGY_PROPERTY = "energy";

    public BatteryCartEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public BatteryCartEntity(EntityType<?> type, World worldIn, double x, double y, double z) {
        super(type, worldIn, x, y, z);
    }

    LazyOptional<IEnergyStorage> energy_handler = LazyOptional.of(() -> new EnergyStorage(40000));

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energy_handler.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public Type getMinecartType() {
        return Type.CHEST;
    }

    @Override
    public void destroy(DamageSource source) {
        super.destroy(source);
        if (!source.isExplosion() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.spawnAtLocation(MMItemReferences.chunk_loader); // todo change to proper item
        }
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return MMReferences.piston_display_block.defaultBlockState().setValue(PistonDisplayBlock.VARIANT, 5);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public boolean stillValid(PlayerEntity player) {
        return player.distanceToSqr((double)this.position().x + 0.5D, (double)this.position().y + 0.5D, (double)this.position().z + 0.5D) <= 64.0D;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(ENERGY_PROPERTY, energy_handler.orElse(null).getEnergyStored());
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        energy_handler.orElse(null).receiveEnergy( compound.getInt(ENERGY_PROPERTY), false );
    }

    public int getEnergyAmount() {
        return energy_handler.orElse(null).getEnergyStored();
    }

    public int getComparatorSignal() {
        return (int)Math.floor((float)energy_handler.resolve().get().getEnergyStored() / energy_handler.resolve().get().getMaxEnergyStored() * 15.0);
    }

    // Container stuff

    @Nullable
    public Container createMenu(int i, PlayerInventory inv, PlayerEntity player) {
        return new BatteryCartContainer(i, level, this, inv, player);
    }

    public ActionResultType interact(PlayerEntity p_184230_1_, Hand p_184230_2_) {
        ActionResultType ret = super.interact(p_184230_1_, p_184230_2_);
        if (ret.consumesAction()) return ret;
        p_184230_1_.openMenu(this);
        return ActionResultType.SUCCESS;
    }

}