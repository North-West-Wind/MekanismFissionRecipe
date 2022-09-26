package ml.northwestwind.fissionrecipe.mixin;

import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = MultiblockData.class, remap = false)
public abstract class MixinMultiblockData {
    @Shadow public abstract void markDirty();

    @Shadow protected abstract World getWorld();

    @Shadow
    @Final
    protected List<IGasTank> gasTanks;

    @Shadow public abstract VoxelCuboid getBounds();

    @Shadow public abstract boolean isFormed();
}
