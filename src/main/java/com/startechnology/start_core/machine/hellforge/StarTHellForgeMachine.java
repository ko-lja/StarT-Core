package com.startechnology.start_core.machine.hellforge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.startechnology.start_core.machine.redstone.StarTRedstoneInterfacePartMachine;
import lombok.Getter;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.startechnology.start_core.machine.redstone.IStarTRedstoneIndicatorMachine;
import com.startechnology.start_core.machine.redstone.StarTRedstoneIndicatorRecord;
import com.startechnology.start_core.materials.StarTHellForgeHeatingLiquids;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

public class StarTHellForgeMachine extends WorkableElectricMultiblockMachine implements IStarTRedstoneIndicatorMachine {
    /*
     * persist/save data onto the world using NBT with the @Persisted field
     * annotation
     */
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(StarTHellForgeMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    protected int temperature;

    protected TickableSubscription tryTickSub;
    private boolean startHeatLoss;

    public StarTHellForgeMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.temperature = 0;
        this.startHeatLoss = false;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.startHeatLoss = true;
        this.temperatureChanged();

    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(
                Component.translatable("ui.start_core.hellforge_crucible", this.temperature)
        );
    }

    /**
     * Retrieves the appropriate heating fluid for the Hellforge based on the
     * required temperature.
     * The method selects the fluid with the highest temperature cap that is still
     * equal to or greater than the input temperature.
     *
     * @param temperature The required temperature of the Hellforge.
     * @return The Material representing the heating fluid, or null if no fluid can
     *         meet the required temperature.
     */
    public static Material getHellforgeHeatingLiquid(int temperature) {
        return Arrays.stream(Fluids.values())
                .dropWhile(entry -> entry.getTemperature() < temperature)
                .map(Fluids::getFluid)
                .findFirst().orElse(null);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (getLevel().isClientSide)
            return;

        tryTickSub = subscribeServerTick(tryTickSub, this::tryRemoveHeat);
    }

    @Override
    public void onUnload() {
        super.onUnload();

        if (getLevel().isClientSide)
            return;

        if (tryTickSub != null) {
            tryTickSub.unsubscribe();
            tryTickSub = null;
        }
    }

    protected void tryRemoveHeat() {
        if (getOffsetTimer() % 200 == 0 && this.startHeatLoss) {

            boolean machineActive = getRecipeLogic().isWorking();

            if (machineActive) {
                this.temperature = Math.max(this.temperature - 5, 0);
            } else {
                this.temperature = Math.max(this.temperature - 125, 0);
            }

            this.temperatureChanged();
        }
    }

    private void temperatureChanged() {
        for (var temperature : Fluids.values()) {
            var percentageOfTier = Math.min(this.temperature * temperature.getTempReciprocal(), 15);
            this.setIndicatorValue("variadic.start_core.indicator.hellforge." + temperature.getTemperature(),
                    (int) Math.floor(percentageOfTier));
        }

        Arrays.stream(Fluids.values()).forEach(
                entry -> {
                    final double percentageOfTier = Math.min(this.temperature * entry.getTempReciprocal(), 15);

                    this.setIndicatorValue("variadic.start_core.indicator.hellforge." + temperature,
                            (int) Math.floor(percentageOfTier));
                });
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        GTRecipe lastRecipe = getRecipeLogic().getLastRecipe();

        List<Content> content = lastRecipe.getInputContents(GTRecipeCapabilities.FLUID);

        if (content.isEmpty())
            return;

        if (content.get(0).getContent() instanceof FluidIngredient ingredient) {
            FluidStack ingredientFluid = ingredient.getStacks()[0];
            Material material = ChemicalHelper.getMaterial(ingredientFluid.getFluid());

            if (Fluids.contains(material)) {
                int maxHeat = Fluids.getHeatValue(material);

                if (this.temperature < maxHeat) {

                    int addTemperature = ingredientFluid.getFluid().getFluidType().getTemperature() / 1_000_000;

                    int amountToAdd = ingredientFluid.getAmount() / 1000;
                    this.temperature = Math.min(temperature + addTemperature * amountToAdd, maxHeat);
                    this.temperatureChanged();

                }
            }
        }
    }

    public int getCrucibleTemperature() {
        return this.temperature;
    }

    @Override
    public List<StarTRedstoneIndicatorRecord> getInitialIndicators() {
        return Arrays.stream(Fluids.values()).map(
                fluid -> {
                    var temperature = fluid.getTemperature();

                    return new StarTRedstoneIndicatorRecord(
                            "variadic.start_core.indicator.hellforge." + temperature,
                            Component.translatable("variadic.start_core.indicator.hellforge", Component.literal(temperature + "MK").withStyle(ChatFormatting.RED)),
                            Component.translatable("variadic.start_core.description.hellforge", temperature).withStyle(ChatFormatting.GRAY),
                            (int) Math.floor(Math.min(fluid.getTempReciprocal(), 15)),
                            temperature);
                }
        ).toList();
    }

    public enum Fluids {
        FlamewakeSolvent(StarTHellForgeHeatingLiquids.FlamewakeSolvent, 900),
        EmberheartNectar(StarTHellForgeHeatingLiquids.EmberheartNectar, 1800),
        IgniferousElixir(StarTHellForgeHeatingLiquids.IgniferousElixir, 2700),
        BlazingPhlogiston(StarTHellForgeHeatingLiquids.BlazingPhlogiston, 3600),
        CinderbrewSolvent(StarTHellForgeHeatingLiquids.CinderbrewSolvent, 1350),
        CorefireNectar(StarTHellForgeHeatingLiquids.CorefireNectar, 2250),
        InfernumElixir(StarTHellForgeHeatingLiquids.InfernumElixir, 3150),
        HellfireEssence(StarTHellForgeHeatingLiquids.HellfireEssence, 4050);

        @Getter
        private final Material fluid;
        @Getter
        private final int temperature;
        @Getter
        private final double tempReciprocal;
        private static final Map<Material, Fluids> BY_MATERIAL;

        Fluids(Material fluid, int temperature) {
            this.fluid = fluid;
            this.temperature = temperature;
            this.tempReciprocal = 1D / temperature * 15;
        }

        static {
            BY_MATERIAL = new HashMap<>();
            for (Fluids fluid : values()) {
                BY_MATERIAL.put(fluid.fluid, fluid);
            }
        }

        public static Fluids fromMaterial(Material material) {
            return BY_MATERIAL.get(material);
        }

        public static int getHeatValue(Material material) {
            Fluids fluid = fromMaterial(material);
            return fluid != null ? fluid.temperature : 0;
        }

        public static boolean contains(Material material) {
            return BY_MATERIAL.containsKey(material);
        }
    }
}
