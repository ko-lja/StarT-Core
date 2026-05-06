package com.startechnology.start_core.machine.wireless;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IManagedGridNode;
import appeng.core.AEConfig;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.SerializableManagedGridNode;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEChannelInputHatchMachine extends TieredPartMachine implements IGridConnectedMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEChannelInputHatchMachine.class, TieredPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    protected final DenseGridNodeHolder nodeHolder;

    @Getter
    @Setter
    @DescSynced
    protected boolean isOnline;

    @Getter
    @Setter
    private int reservedConnections;

    public MEChannelInputHatchMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTValues.LuV);
        this.nodeHolder = new DenseGridNodeHolder(this);
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.of(newFacing));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public int getChannelCapacity() {
        var factor = AEConfig.instance().getChannelMode().getCableCapacityFactor();
        return factor == 0 ? Integer.MAX_VALUE : 32* factor;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 122, 50);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new LabelWidget(6, 6, "start_core.gui.me_channel_input_hatch.title"));
        group.addWidget(new LabelWidget(6, 20, () -> isOnline ?
                "start_core.gui.me_channel_input_hatch.network.online" :
                "start_core.gui.me_channel_input_hatch.network.offline"));
        group.addWidget(new LabelWidget(6, 32, () -> Component.translatable(
                "start_core.gui.me_channel_input_hatch.reserved", reservedConnections, getChannelCapacity()).getString()));
        return group;
    }

    protected static class DenseGridNodeHolder extends MachineTrait {

        protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DenseGridNodeHolder.class);

        @Getter
        @Persisted
        @ReadOnlyManaged(onDirtyMethod = "onGridNodeDirty",
                         serializeMethod = "serializeGridNode",
                         deserializeMethod = "deserializeGridNode")
        protected final SerializableManagedGridNode mainNode;

        public DenseGridNodeHolder(MEChannelInputHatchMachine machine) {
            super(machine);
            this.mainNode = createManagedNode(machine);
        }

        protected SerializableManagedGridNode createManagedNode(MEChannelInputHatchMachine machine) {
            return (SerializableManagedGridNode) new SerializableManagedGridNode(
                    (IGridConnectedBlockEntity) machine, BlockEntityNodeListener.INSTANCE)
                    .setFlags(GridFlags.DENSE_CAPACITY)
                    .setVisualRepresentation(machine.getDefinition().getItem())
                    .setIdlePowerUsage(0)
                    .setInWorldNode(true)
                    .setExposedOnSides(machine.hasFrontFacing() ?
                            EnumSet.of(machine.getFrontFacing()) :
                            EnumSet.allOf(Direction.class))
                    .setTagName("me_channel_input");
        }

        protected void createMainNode() {
            this.mainNode.create(machine.getLevel(), machine.getPos());
        }

        @Override
        public void onMachineLoad() {
            super.onMachineLoad();
            if (machine.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new TickTask(0, this::createMainNode));
            }
        }

        @Override
        public void onMachineUnLoad() {
            super.onMachineUnLoad();
            mainNode.destroy();
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }

        @SuppressWarnings("unused")
        public boolean onGridNodeDirty(SerializableManagedGridNode node) {
            return node.isActive() && node.isOnline();
        }

        @SuppressWarnings("unused")
        public CompoundTag serializeGridNode(SerializableManagedGridNode node) {
            return node.serializeNBT();
        }

        @SuppressWarnings("unused")
        public SerializableManagedGridNode deserializeGridNode(CompoundTag tag) {
            this.mainNode.deserializeNBT(tag);
            return this.mainNode;
        }
    }
}
