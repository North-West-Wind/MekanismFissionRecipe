package ml.northwestwind.fissionrecipe;

import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.FluidCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.serializer.FissionRecipeSerializer;
import ml.northwestwind.fissionrecipe.recipe.serializer.FluidCoolantRecipeSerializer;
import ml.northwestwind.fissionrecipe.recipe.serializer.GasCoolantRecipeSerializer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod(MekanismFission.MOD_ID)
public class MekanismFission
{
    public static final String MOD_ID = "fissionrecipe";

    @Mod.EventBusSubscriber(modid = MekanismFission.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvent {
        @SubscribeEvent
        public static void registerRecipe(final net.minecraftforge.event.RegistryEvent.Register<IRecipeSerializer<?>> event) {
            Recipes.FISSION.register(event.getRegistry());
        }

        public static class Recipes<S extends IRecipeSerializer<? extends IRecipe<?>>> {
            public static final Recipes<FissionRecipeSerializer> FISSION = new Recipes<>(new FissionRecipeSerializer(), FissionRecipe.RECIPE_TYPE_ID);
            public static final Recipes<FluidCoolantRecipeSerializer> FLUID_COOLANT = new Recipes<>(new FluidCoolantRecipeSerializer(), FluidCoolantRecipe.RECIPE_TYPE_ID);
            public static final Recipes<GasCoolantRecipeSerializer> GAS_COOLANT = new Recipes<>(new GasCoolantRecipeSerializer(), GasCoolantRecipe.RECIPE_TYPE_ID);

            private static <T extends IRecipe<?>> IRecipeType<T> customType(ResourceLocation rl) {
                return Registry.register(Registry.RECIPE_TYPE, rl, new IRecipeType<T>() {
                    public String toString() {
                        return rl.toString();
                    }
                });
            }

            final ResourceLocation rl;
            IRecipeType<? extends IRecipe<?>> type = null;
            S serializer;

            private Recipes(S serializer, ResourceLocation rl) {
                this.serializer = serializer;
                this.rl = rl;
            }

            public S getSerializer() {
                return serializer;
            }

            @SuppressWarnings("unchecked")
            public <T extends IRecipeType<?>> T getType() {
                return (T) type;
            }

            public void register(IForgeRegistry<IRecipeSerializer<?>> registry) {
                if (type == null) type = customType(rl);

                registry.register(serializer.setRegistryName(rl));
            }
        }
    }

}
