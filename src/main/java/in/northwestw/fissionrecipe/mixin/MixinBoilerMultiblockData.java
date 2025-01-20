package in.northwestw.fissionrecipe.mixin;

import in.northwestw.fissionrecipe.MekanismFission;
import in.northwestw.fissionrecipe.recipe.BoilerRecipe;
import in.northwestw.fissionrecipe.recipe.HeatedCoolantRecipe;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

@Mixin(BoilerMultiblockData.class)
public abstract class MixinBoilerMultiblockData extends MixinMultiblockData {
    @Unique
    private HeatedCoolantRecipe heatedCoolantRecipe;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;input(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0))
    public IChemicalTank customSuperheatedCoolantTank(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return VariableCapacityChemicalTank.input(multiblock, capacity, validator, listener);
        List<HeatedCoolantRecipe> recipes = serverHeatedCoolantRecipes(server);
        return VariableCapacityChemicalTank.input(multiblock, capacity, gas -> recipes.stream().anyMatch(r -> r.getInput().testType(gas)), listener);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/fluid/VariableCapacityFluidTank;input(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/IntSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/common/capabilities/fluid/VariableCapacityFluidTank;", ordinal = 0))
    public VariableCapacityFluidTank customWaterTank(MultiblockData multiblock, IntSupplier capacity, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return VariableCapacityFluidTank.input(multiblock, capacity, validator, listener);
        List<BoilerRecipe> recipes = serverBoilerRecipes(server);
        return VariableCapacityFluidTank.input(multiblock, capacity, fluid -> recipes.stream().anyMatch(r -> r.test(fluid)), listener);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;output(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0))
    public IChemicalTank customSteamTank(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return VariableCapacityChemicalTank.output(multiblock, capacity, gas -> true, listener);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;output(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 1))
    public IChemicalTank customCooledCoolantTank(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return VariableCapacityChemicalTank.output(multiblock, capacity, gas -> true, listener);
    }

    // START: modification of tick coolant
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/api/chemical/ChemicalStack;get(Ljava/lang/Class;)Lmekanism/api/chemical/attribute/ChemicalAttribute;"))
    public <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE getHeatedCoolant(ChemicalStack instance, Class<ATTRIBUTE> type) {
        if (heatedCoolantRecipe == null) {
            
        }
        return null;
    }

    @Unique
    private static List<BoilerRecipe> serverBoilerRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.RecipeTypes.BOILER.get()).stream().map(RecipeHolder::value).toList();
    }

    @Unique
    private static List<HeatedCoolantRecipe> serverHeatedCoolantRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.RecipeTypes.HEATED_COOLANT.get()).stream().map(holder -> (HeatedCoolantRecipe) holder.value()).toList();
    }
}
