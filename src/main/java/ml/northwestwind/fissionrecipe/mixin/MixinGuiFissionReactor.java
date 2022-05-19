package ml.northwestwind.fissionrecipe.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiBigLight;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiHybridGauge;
import mekanism.client.gui.element.graph.GuiDoubleGraph;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.gui.GuiFissionReactor;
import mekanism.generators.client.gui.element.GuiFissionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(value = GuiFissionReactor.class, remap = false)
public abstract class MixinGuiFissionReactor extends MixinGuiMekanismTile<TileEntityFissionReactorCasing, MekanismTileContainer<TileEntityFissionReactorCasing>> {
    @Shadow private TranslationButton activateButton;

    @Shadow private TranslationButton scramButton;

    @Shadow private GuiDoubleGraph heatGraph;

    @Shadow protected abstract void updateButtons();

    // Unfortunately, redirect doesn't work
    @Inject(at = @At(value = "INVOKE", target = "Lmekanism/generators/client/gui/GuiFissionReactor;addRenderableWidget(Lmekanism/client/gui/element/GuiElement;)Lmekanism/client/gui/element/GuiElement;", ordinal = 0), method = "addGuiElements", cancellable = true)
    public void addGuiElements(CallbackInfo ci) {
        ci.cancel();
        addRenderableWidget(new GuiFissionReactorTab((GuiFissionReactor) (Object) this, tile, GuiFissionReactorTab.FissionReactorTab.STAT));
        addRenderableWidget(new GuiInnerScreen((GuiFissionReactor) (Object) this, 45, 17, 105, 56, () -> {
            FissionReactorMultiblockData multiblock = tile.getMultiblock();
            return List.of(
                    MekanismLang.STATUS.translate(multiblock.isActive() ? EnumColor.BRIGHT_GREEN : EnumColor.RED, BooleanStateDisplay.ActiveDisabled.of(multiblock.isActive())),
                    GeneratorsLang.GAS_BURN_RATE.translate(multiblock.lastBurnRate),
                    GeneratorsLang.FISSION_HEATING_RATE.translate(TextUtils.format(multiblock.lastBoilRate)),
                    MekanismLang.TEMPERATURE.translate(tile.getTempColor(), MekanismUtils.getTemperatureDisplay(multiblock.heatCapacitor.getTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true)),
                    GeneratorsLang.FISSION_DAMAGE.translate(tile.getDamageColor(), tile.getDamageString())
            );
        }).spacing(2).jeiCategories(FissionRecipe.RECIPE_TYPE));
        addRenderableWidget(new GuiHybridGauge(() -> tile.getMultiblock().gasCoolantTank, () -> tile.getMultiblock().getGasTanks(null),
                () -> tile.getMultiblock().fluidCoolantTank, () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, (GuiFissionReactor) (Object) this, 6, 13)
                .setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().fuelTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, (GuiFissionReactor) (Object) this, 25, 13)
                .setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().heatedCoolantTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, (GuiFissionReactor) (Object) this, 152, 13)
                .setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().wasteTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, (GuiFissionReactor) (Object) this, 171, 13)
                .setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
        addRenderableWidget(new GuiHeatTab((GuiFissionReactor) (Object) this, () -> {
            Component environment = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().lastEnvironmentLoss, UnitDisplayUtils.TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
        final TileEntityFissionReactorCasing tileCopy = tile;
        activateButton = addRenderableWidget(new TranslationButton((GuiFissionReactor) (Object) this, 6, 75, 81, 16, GeneratorsLang.FISSION_ACTIVATE,
                () -> MekanismGenerators.packetHandler().sendToServer(new PacketGeneratorsGuiInteract(PacketGeneratorsGuiInteract.GeneratorsGuiInteraction.FISSION_ACTIVE, tile, 1)), null,
                () -> EnumColor.DARK_GREEN) {
            @Override
            public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
                super.renderForeground(matrix, mouseX, mouseY);
                if (!active && tileCopy.getMultiblock().isForceDisabled()) {
                    active = true;
                    //Temporarily set active to true, so we can easily check if the mouse is over the button
                    if (isMouseOverCheckWindows(mouseX, mouseY)) {
                        matrix.pushPose();
                        //Offset to fix rendering position
                        matrix.translate(-getGuiLeft(), -getGuiTop(), 0);
                        displayTooltips(matrix, mouseX, mouseY, GeneratorsLang.FISSION_FORCE_DISABLED.translate());
                        matrix.popPose();
                    }
                    active = false;
                }
            }
        });
        scramButton = addRenderableWidget(new TranslationButton((GuiFissionReactor) (Object) this, 89, 75, 81, 16, GeneratorsLang.FISSION_SCRAM,
                () -> MekanismGenerators.packetHandler().sendToServer(new PacketGeneratorsGuiInteract(PacketGeneratorsGuiInteract.GeneratorsGuiInteraction.FISSION_ACTIVE, tile, 0)), null,
                () -> EnumColor.DARK_RED));
        addRenderableWidget(new GuiBigLight((GuiFissionReactor) (Object) this, 173, 76, tile.getMultiblock()::isActive));
        addRenderableWidget(new GuiDynamicHorizontalRateBar((GuiFissionReactor) (Object) this, new GuiBar.IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismUtils.getTemperatureDisplay(tileCopy.getMultiblock().heatCapacitor.getTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true);
            }

            @Override
            public double getLevel() {
                return Math.min(1, tileCopy.getMultiblock().heatCapacitor.getTemperature() / FissionReactorMultiblockData.MAX_DAMAGE_TEMPERATURE);
            }
        }, 5, 102, imageWidth - 12));
        heatGraph = addRenderableWidget(new GuiDoubleGraph((GuiFissionReactor) (Object) this, 5, 123, imageWidth - 10, 38,
                temp -> MekanismUtils.getTemperatureDisplay(temp, UnitDisplayUtils.TemperatureUnit.KELVIN, true)));
        heatGraph.setMinScale(1_600);
        updateButtons();
    }

    /*@Redirect(at = @At(value = "INVOKE", target = "Lmekanism/client/gui/element/GuiInnerScreen;jeiCategories([Lmekanism/client/jei/MekanismJEIRecipeType;)Lmekanism/client/gui/element/GuiInnerScreen;"), method = "addGuiElements")
    public GuiInnerScreen redirectJeiCategories(GuiInnerScreen instance, MekanismJEIRecipeType<?>[] recipeCategories) {
        LogManager.getLogger().info("Redirecting jeiCategories");
        return instance.jeiCategories();
    }*/
}
