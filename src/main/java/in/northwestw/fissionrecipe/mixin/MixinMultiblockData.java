package in.northwestw.fissionrecipe.mixin;

import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MultiblockData.class, remap = false)
public abstract class MixinMultiblockData {
    @Shadow public abstract void markDirty();

    @Shadow
    public abstract Level getLevel();

    @Shadow public abstract VoxelCuboid getBounds();
}
