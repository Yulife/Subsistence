package com.subsistence.common.recipe.core;

import com.google.gson.Gson;
import com.subsistence.common.recipe.SubsistenceRecipes;
import com.subsistence.common.recipe.wrapper.CompostRecipe;
import com.subsistence.common.util.StackHelper;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author lclc98
 */
public class CompostParser {


    public static class ParsedRecipe {

        public boolean crash_on_fail = true;
        public Recipe[] recipes;
    }

    public static class Recipe {

        public String[] inputItem;
        public String inputLiquid;
        public Output output;
    }

    public static class Output {

        public String outputLiquid;
        public String outputItem;

        public int time;
        public int timeTorch = -1;
        public int timeLava = -1;
        public int timeFire = -1;
    }

    public static void parseFile(File file) {
        try {
            FMLLog.info("[Subsistence] Parsing " + file.getName());
            ParsedRecipe recipe = new Gson().fromJson(new FileReader(file), ParsedRecipe.class);
            verifyParse(file.getName(), recipe);
        } catch (IOException ex) {
            FMLLog.warning("[Subsistence] Failed to parse " + file.getName());
        }
    }

    private static void verifyParse(String name, ParsedRecipe recipe) {
        for (Recipe recipe1 : recipe.recipes) {
            ArrayList<ItemStack> inputItem = new ArrayList<ItemStack>();
            FluidStack inputLiquid = null;
            if (recipe1.inputLiquid != null)
                inputLiquid = RecipeParser.getLiquid(recipe1.inputLiquid);

            ItemStack outputItem = null;
            FluidStack outputLiquid = null;

            if (recipe1.inputItem.length > 0) {
                for (String inputList : recipe1.inputItem) {
                    ItemStack[] tempInput = StackHelper.convert(RecipeParser.getItem(inputList));
                    for (ItemStack stack : tempInput)
                        inputItem.add(stack);
                }
            }

            if (recipe1.output.outputItem != null && !recipe1.output.outputItem.isEmpty()) {
                outputItem = StackHelper.convert(RecipeParser.getItem(recipe1.output.outputItem))[0];
            }
            if (recipe1.output.outputLiquid != null && !recipe1.output.outputLiquid.isEmpty()) {
                outputLiquid = RecipeParser.getLiquid(recipe1.output.outputLiquid);
            }

            if (recipe.crash_on_fail) {
                if (inputItem.size() <= 0 && inputLiquid == null)
                    throw new NullPointerException("Inputs is null!");
                if (outputItem == null && outputLiquid == null) {
                    throw new NullPointerException("Outputs can't be null!");
                }
            }


            SubsistenceRecipes.COMPOST.register(new CompostRecipe(inputItem.toArray(new ItemStack[inputItem.size()]), inputLiquid, outputItem, outputLiquid, recipe1.output.time, recipe1.output.timeTorch, recipe1.output.timeLava, recipe1.output.timeFire));
        }

        int length = recipe.recipes.length;
        FMLLog.info("[Subsistence] Parsed " + name + ". Loaded " + length + (length > 1 ? " recipes" : " recipe"));
    }
}
