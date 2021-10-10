# Mekanism Fission Recipe
A really unique addon for [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism) [Generators](https://www.curseforge.com/minecraft/mc-mods/mekanism-generators).

## What?
With this mod installed, you can add actual recipes for Mekanism's Fission Reactor. Below is an example of converting hydrogen to oxygen with the fission reactor that generates no heat:
![hydrogen_to_oxygen](https://github.com/North-West-Wind/MekanismFissionRecipe/blob/main/resources/hydrogen.png?raw=true)

## How?
This mod uses a dirty hack called [Mixins](https://github.com/SpongePowered/Mixin). It changes Mekanism's code at runtime. By creating a new recipe type and forcing the Fission Reactor to accept it, you can now add custom fission recipes to Mekanism.

You just need to create the recipe like a datapack. Here's how the recipe JSON file should look like:
```json
{
	"type": "mekanism:fission",
	"input": {
		"gas": "mekanism:fissile_fuel",
		"amount": 1
	},
	"output": {
		"gas": "mekanism:nuclear_waste",
		"amount": 1
	},
	"heat": 1
}
```
- Type: Always use "mekanism:fission" if you are adding a fission recipe.
- Input
  - Gas: The input gas. If you're making a mod you can add your own gas.
  - Amount: Input amount.
- Output: Similar to input.
- Heat: The amount of heat generated. 1 is the amount Fissile Fuel normally creates.
  - You can also use equations in this field with x as subject. For example, "x*x" will make burning 0.1mB of fuel generate 0.01 heat.

## Why?
This is originally implemented in [Sky Farm](https://www.curseforge.com/minecraft/modpacks/sky-farm-1-16), but I feel like this feature has its own potential, so I took it out, and made it a standalone mod.

## Help?
If you find my projects great, you can support me by joining my Patreon!
[![Patreon](https://drive.google.com/uc?export=download&id=1AH5YdXRoE6G3RQKqWY03TsYMy1H_E5lU)](https://www.patreon.com/nww)

## License
This mod is running under GNU GPLv3.
