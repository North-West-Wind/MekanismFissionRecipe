package ml.northwestwind.fissionrecipe.mixin;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiMekanismTile.class, remap = false)
public abstract class MixinGuiMekanismTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends MixinGuiMekanism {
    @Shadow @Final protected TILE tile;
}
