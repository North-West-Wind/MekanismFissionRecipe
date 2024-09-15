package in.northwestw.fissionrecipe.mixin;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiMekanism.class, remap = false)
public abstract class MixinGuiMekanism extends MixinAbstractContainerScreen {
    @Shadow protected abstract <T extends GuiElement> T addRenderableWidget(T element);
}
