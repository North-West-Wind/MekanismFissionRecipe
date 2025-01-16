package in.northwestw.fissionrecipe.jei;

import mekanism.api.providers.IItemProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record FissionRecipeViewType(
        ResourceLocation id, @Nullable ResourceLocation icon, @Nullable IItemProvider item, IHasTranslationKey name, Class<FissionJEIRecipe> recipeClass,
        int xOffset, int yOffset, int width, int height, List<IItemProvider> workstations
) implements IRecipeViewerRecipeType<FissionJEIRecipe> {
    public static final FissionRecipeViewType FISSION = new FissionRecipeViewType(
            MekanismGenerators.rl("fission"),
            MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "radioactive.png"),
            null,
            GeneratorsLang.FISSION_REACTOR,
            FissionJEIRecipe.class,
            -6, -13, 182, 60,
            List.of(GeneratorsBlocks.FISSION_REACTOR_CASING, GeneratorsBlocks.FISSION_REACTOR_PORT, GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, GeneratorsBlocks.CONTROL_ROD_ASSEMBLY)
    );

    @Override
    public Component getTextComponent() {
        return TextComponentUtil.build(name);
    }

    @Override
    public boolean requiresHolder() {
        return false;
    }

    @Override
    public ItemStack iconStack() {
        return item == null ? ItemStack.EMPTY : item.getItemStack();
    }
}
