package in.northwestw.fissionrecipe.mixin;

import in.northwestw.fissionrecipe.MekanismFission;
import in.northwestw.fissionrecipe.misc.RecipeHeatedCoolant;
import in.northwestw.fissionrecipe.recipe.BoilerRecipe;
import in.northwestw.fissionrecipe.recipe.HeatedCoolantRecipe;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registration.impl.DeferredChemical;
import mekanism.common.util.HeatUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

@Mixin(BoilerMultiblockData.class)
public abstract class MixinBoilerMultiblockData extends MixinMultiblockData {
    @Shadow public IChemicalTank superheatedCoolantTank;
    @Shadow public VariableCapacityFluidTank waterTank;
    @Shadow public IChemicalTank steamTank;
    @Unique
    private HeatedCoolantRecipe heatedCoolantRecipe;
    @Unique
    private BoilerRecipe boilerRecipe;

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
        if (heatedCoolantRecipe != null && !heatedCoolantRecipe.getInput().testType(superheatedCoolantTank.getType()))
            heatedCoolantRecipe = null;
        if (heatedCoolantRecipe == null) {
            heatedCoolantRecipe = serverHeatedCoolantRecipes(this.getLevel().getServer()).stream().filter(r -> r.getInput().testType(superheatedCoolantTank.getType())).findFirst().orElse(null);
            if (heatedCoolantRecipe == null) return instance.get(type);
        }
        return (ATTRIBUTE) new RecipeHeatedCoolant(heatedCoolantRecipe);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;round(D)J", ordinal = 0))
    public double firstToCool(double a) {
        if (heatedCoolantRecipe == null) return a;
        return heatedCoolantRecipe.getEfficiency() * superheatedCoolantTank.getStored();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/heat/VariableHeatCapacitor;getTemperature()D", ordinal = 0))
    public double getRecipeTemperature(VariableHeatCapacitor instance) {
        if (heatedCoolantRecipe == null) return instance.getTemperature();
        // cancel out the multiplied value and use ours
        return instance.getTemperature() * HeatUtils.HEATED_COOLANT_TEMP / heatedCoolantRecipe.getTemperature();
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/api/chemical/Chemical;getStack(J)Lmekanism/api/chemical/ChemicalStack;", ordinal = 0))
    public long scaleOutput(long size) {
        if (heatedCoolantRecipe == null) return size;
        return size * heatedCoolantRecipe.getOutputRaw().getAmount() / heatedCoolantRecipe.getInput().amount();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/api/chemical/ChemicalStack;getAmount()J", ordinal = 0))
    public long unscaleOutput(ChemicalStack instance) {
        if (heatedCoolantRecipe == null) return instance.getAmount();
        return instance.getAmount() * heatedCoolantRecipe.getInput().amount() / heatedCoolantRecipe.getOutputRaw().getAmount();
    }

    // END: modification of tick coolant
    // START: modification of tick boil

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/fluid/VariableCapacityFluidTank;isEmpty()Z", ordinal = 0))
    public boolean extraConditions(VariableCapacityFluidTank instance) {
        if (instance.isEmpty()) return false;
        if (boilerRecipe != null && !boilerRecipe.test(waterTank.getFluid()))
            boilerRecipe = null;
        if (boilerRecipe == null) {
            boilerRecipe = serverBoilerRecipes(this.getLevel().getServer()).stream().filter(r -> r.getInput().testType(waterTank.getFluid())).findFirst().orElse(null);
            if (boilerRecipe == null) return false;
        }

        return steamTank.isEmpty() || boilerRecipe.getOutput().is(steamTank.getType());
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/common/util/HeatUtils;getSteamEnergyEfficiency()D"))
    public double getBoilerRecipeEfficiency() {
        if (boilerRecipe == null) return HeatUtils.getSteamEnergyEfficiency();
        return boilerRecipe.getEfficiency();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/common/util/HeatUtils;getWaterThermalEnthalpy()D"))
    public double getBoilerRecipeThermalEnthalpy() {
        if (boilerRecipe == null) return HeatUtils.getWaterThermalEnthalpy();
        return boilerRecipe.getThermalEnthalpy();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/common/registration/impl/DeferredChemical;getStack(J)Lmekanism/api/chemical/ChemicalStack;"))
    public ChemicalStack getCustomOutput(DeferredChemical instance, long size) {
        if (boilerRecipe == null) return instance.getStack(size);
        return boilerRecipe.getOutput().copyWithAmount(size * boilerRecipe.getOutput().getAmount() / boilerRecipe.getInput().getRepresentations().get(0).getAmount());
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/api/chemical/IChemicalTank;growStack(JLmekanism/api/Action;)J", ordinal = 0))
    public long scaleGrow(long amount) {
        if (boilerRecipe == null) return amount;
        return amount * boilerRecipe.getOutput().getAmount() / boilerRecipe.getInput().getRepresentations().get(0).getAmount();
    }

    // END: modification of tick boil

    @Redirect(method = "getHeatAvailable", at = @At(value = "INVOKE", target = "Lmekanism/common/config/value/CachedDoubleValue;get()D"))
    public double getConductivity(CachedDoubleValue instance) {
        if (boilerRecipe == null) return instance.get();
        return boilerRecipe.getConductivity();
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
