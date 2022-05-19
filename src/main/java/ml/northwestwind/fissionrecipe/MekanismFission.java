package ml.northwestwind.fissionrecipe;

import ml.northwestwind.fissionrecipe.events.RegistryEvents;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.FluidCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.serializer.FissionRecipeSerializer;
import ml.northwestwind.fissionrecipe.recipe.serializer.FluidCoolantRecipeSerializer;
import ml.northwestwind.fissionrecipe.recipe.serializer.GasCoolantRecipeSerializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod(MekanismFission.MOD_ID)
public class MekanismFission
{
    public static final String MOD_ID = "fissionrecipe";

    public MekanismFission() {
        RegistryEvents.registerGases();
        RegistryEvents.registerItems();
    }

    @Mod.EventBusSubscriber(modid = MekanismFission.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvent {
        @SubscribeEvent
        public static void registerRecipe(final net.minecraftforge.event.RegistryEvent.Register<RecipeSerializer<?>> event) {
            Recipes.FISSION.register(event.getRegistry());
            Recipes.FLUID_COOLANT.register(event.getRegistry());
            Recipes.GAS_COOLANT.register(event.getRegistry());
        }

        public static class Recipes<S extends RecipeSerializer<? extends Recipe<?>>> {
            public static final Recipes<FissionRecipeSerializer> FISSION = new Recipes<>(new FissionRecipeSerializer(), FissionRecipe.RECIPE_TYPE.uid());
            public static final Recipes<FluidCoolantRecipeSerializer> FLUID_COOLANT = new Recipes<>(new FluidCoolantRecipeSerializer(), FluidCoolantRecipe.RECIPE_TYPE_ID);
            public static final Recipes<GasCoolantRecipeSerializer> GAS_COOLANT = new Recipes<>(new GasCoolantRecipeSerializer(), GasCoolantRecipe.RECIPE_TYPE_ID);

            private static <T extends Recipe<?>> RecipeType<T> customType(ResourceLocation rl) {
                return Registry.register(Registry.RECIPE_TYPE, rl, new RecipeType<T>() {
                    public String toString() {
                        return rl.toString();
                    }
                });
            }

            final ResourceLocation rl;
            RecipeType<? extends Recipe<?>> type = null;
            S serializer;

            private Recipes(S serializer, ResourceLocation rl) {
                this.serializer = serializer;
                this.rl = rl;
            }

            public S getSerializer() {
                return serializer;
            }

            @SuppressWarnings("unchecked")
            public <T extends RecipeType<?>> T getType() {
                return (T) type;
            }

            public void register(IForgeRegistry<RecipeSerializer<?>> registry) {
                if (type == null) type = customType(rl);

                registry.register(serializer.setRegistryName(rl));
            }
        }
    }

}
