package in.northwestw.fissionrecipe.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {
    @Shadow protected int imageWidth;
}
