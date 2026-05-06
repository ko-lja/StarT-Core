package com.startechnology.start_core.machine.wireless;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.startechnology.start_core.integration.ae2.wireless.IWirelessAEMachine;
import com.startechnology.start_core.integration.ae2.wireless.SavedWirelessLink;
import com.startechnology.start_core.integration.ae2.wireless.TowerPredicates;
import com.startechnology.start_core.integration.ae2.wireless.TowerTier;
import com.startechnology.start_core.integration.ae2.wireless.WirelessMachineRef;
import com.startechnology.start_core.integration.ae2.wireless.WirelessTowerDataStickHelper;
import com.startechnology.start_core.integration.ae2.wireless.WirelessTowerRegistry;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MERadioTowerMachine extends MultiblockControllerMachine implements IFancyUIMachine, IDataStickInteractable {

    private static final int MAX_UI_ROWS = TowerTier.highest().getMaxConnections();
    private static final String TAG_TOWER_ID = "TowerId";
    private static final String TAG_TIER = "TowerTier";
    private static final String TAG_LINKS = "TowerLinks";
    
    @Getter
    private UUID towerId = UUID.randomUUID();
    @Getter
    private TowerTier currentTier = TowerTier.STEEL;
    private final List<SavedWirelessLink> savedLinks = new ArrayList<>();
    private final Map<WirelessMachineRef, IGridConnection> runtimeConnections = new LinkedHashMap<>();
    private final Set<WirelessMachineRef> reservedMachineRefs = new LinkedHashSet<>();
    private final List<MEChannelInputHatchMachine> channelHatches = new ArrayList<>();

    private EnergyContainerList energyContainer = new EnergyContainerList(new ArrayList<>());
    @Nullable
    private TickableSubscription tickSubscription;

    public MERadioTowerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public boolean hasSavedLink(WirelessMachineRef ref) {
        return savedLinks.stream().anyMatch(link -> link.machineRef().equals(ref));
    }

    public void requestLink(IWirelessAEMachine machine, @Nullable Player player) {
        if (!(getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!isFormed()) {
            sendMessage(player, Component.translatable("start_core.machine.me_radio_tower.not_formed"));
            return;
        }

        var ref = machine.getMachineRef();
        if (hasSavedLink(ref)) {
            sendMessage(player, Component.translatable("start_core.machine.me_radio_tower.already_linked"));
            serverTickTower();
            return;
        }
        if (savedLinks.size() >= TowerTier.highest().getMaxConnections()) {
            sendMessage(player, Component.translatable("start_core.machine.me_radio_tower.memory_full"));
            return;
        }
        if (!currentTier.isWithinRange(level, getPos(), ref.dimension(), ref.pos())) {
            sendMessage(player, Component.translatable("start_core.machine.me_radio_tower.out_of_range"));
            return;
        }
        if (machine.hasPhysicalGridConnection()) {
            sendMessage(player, Component.translatable("start_core.machine.me_radio_tower.cable_connected"));
            return;
        }

        pruneBrokenLinks();
        refreshReservations();
        if (reservedMachineRefs.size() >= getUsableConnectionCapacity()) {
            sendMessage(player, Component.translatable("start_core.machine.me_radio_tower.no_free_channels"));
            return;
        }

        machine.setConfiguredTowerId(towerId);
        machine.self().onChanged();
        savedLinks.add(new SavedWirelessLink(ref, null));
        onChanged();
        serverTickTower();
        sendMessage(player, Component.translatable("start_core.machine.me_radio_tower.linked_machine",
                machine.getDisplayName(), WirelessTowerDataStickHelper.shortId(towerId)));
    }

    public void disconnectMachine(WirelessMachineRef ref, @Nullable Player player) {
        boolean removed = savedLinks.removeIf(link -> link.machineRef().equals(ref));
        destroyRuntimeConnection(ref);
        if (removed) {
            onChanged();
            serverTickTower();
            sendMessage(player, Component.translatable("start_core.machine.me_radio_tower.disconnected_machine", ref.dimensionalPos()));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        WirelessTowerRegistry.register(this);
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTickSubscription));
        }
    }

    @Override
    public void onUnload() {
        destroyRuntimeConnections();
        clearReservations();
        WirelessTowerRegistry.unregister(this);
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        super.onUnload();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var matchedTier = getMultiblockState().getMatchContext().get(TowerPredicates.MATCH_TIER_KEY);
        if (matchedTier instanceof TowerTier tier) {
            this.currentTier = tier;
        }
        rebuildStructureCaches();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTickSubscription));
        }
    }

    @Override
    public void onStructureInvalid() {
        destroyRuntimeConnections();
        clearReservations();
        channelHatches.clear();
        energyContainer = new EnergyContainerList(new ArrayList<>());
        super.onStructureInvalid();
    }

    private void rebuildStructureCaches() {
        channelHatches.clear();
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (var part : getParts()) {
            if (part instanceof MEChannelInputHatchMachine hatch) {
                channelHatches.add(hatch);
            }

            var io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE || io == IO.OUT) {
                continue;
            }

            for (var handlerList : part.getRecipeHandlers()) {
                if (!handlerList.isValid(io)) {
                    continue;
                }
                handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .forEach(energyContainers::add);
            }
        }
        channelHatches.sort(Comparator.comparing(hatch -> hatch.getPos().asLong()));
        energyContainer = new EnergyContainerList(energyContainers);
    }

    private void updateTickSubscription() {
        if (isFormed()) {
            tickSubscription = subscribeServerTick(tickSubscription, this::serverTickTower);
        } else if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    private void serverTickTower() {
        if (!(getLevel() instanceof ServerLevel) || !isFormed()) {
            destroyRuntimeConnections();
            clearReservations();
            return;
        }

        pruneBrokenLinks();
        syncRuntimeConnections(refreshReservations());
    }

    private void pruneBrokenLinks() {
        boolean changed = false;
        for (var iterator = savedLinks.iterator(); iterator.hasNext();) {
            var link = iterator.next();
            if (isLinkBroken(link.machineRef())) {
                destroyRuntimeConnection(link.machineRef());
                iterator.remove();
                changed = true;
            }
        }
        if (changed) {
            onChanged();
        }
    }

    private boolean isLinkBroken(WirelessMachineRef ref) {
        var targetLevel = getLoadedLevel(ref.dimension());
        if (targetLevel == null || !targetLevel.hasChunkAt(ref.pos())) {
            return false;
        }
        if (!(getMachine(targetLevel, ref.pos()) instanceof IWirelessAEMachine machine)) {
            return true;
        }
        return !Objects.equals(machine.getConfiguredTowerId(), towerId);
    }

    private List<SavedWirelessLink> refreshReservations() {
        clearReservations();
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            return List.of();
        }

        Map<BlockPos, Integer> counts = new HashMap<>();
        Map<BlockPos, MEChannelInputHatchMachine> hatchesByPos = new LinkedHashMap<>();
        for (var hatch : channelHatches) {
            hatchesByPos.put(hatch.getPos(), hatch);
        }

        boolean changed = false;
        int remainingTierCapacity = currentTier.getMaxConnections();
        List<SavedWirelessLink> updatedLinks = new ArrayList<>(savedLinks.size());
        List<SavedWirelessLink> reservedLinks = new ArrayList<>();

        for (var link : savedLinks) {
            var updatedLink = link;
            if (remainingTierCapacity > 0 && shouldReserveLink(level, link.machineRef())) {
                var assignedHatch = assignHatch(link, counts, hatchesByPos);
                if (assignedHatch != null) {
                    updatedLink = link.withHatch(assignedHatch);
                    reservedLinks.add(updatedLink);
                    reservedMachineRefs.add(link.machineRef());
                    remainingTierCapacity--;
                }
            }
            if (!updatedLink.equals(link)) {
                changed = true;
            }
            updatedLinks.add(updatedLink);
        }

        if (changed) {
            savedLinks.clear();
            savedLinks.addAll(updatedLinks);
            onChanged();
        }

        for (var hatch : channelHatches) {
            hatch.setReservedConnections(counts.getOrDefault(hatch.getPos(), 0));
        }
        return reservedLinks;
    }

    private boolean shouldReserveLink(ServerLevel towerLevel, WirelessMachineRef ref) {
        if (!currentTier.isWithinRange(towerLevel, getPos(), ref.dimension(), ref.pos())) {
            return false;
        }
        var machine = getLoadedMachine(ref);
        return machine == null || !machine.hasPhysicalGridConnection();
    }

    @Nullable
    private BlockPos assignHatch(SavedWirelessLink link, Map<BlockPos, Integer> counts,
                                 Map<BlockPos, MEChannelInputHatchMachine> hatchesByPos) {
        var preferred = link.hatchPos();
        if (preferred != null) {
            var hatch = hatchesByPos.get(preferred);
            if (hatch != null && counts.getOrDefault(preferred, 0) < hatch.getChannelCapacity()) {
                counts.put(preferred, counts.getOrDefault(preferred, 0) + 1);
                return preferred;
            }
        }
        for (var hatch : channelHatches) {
            var pos = hatch.getPos();
            int used = counts.getOrDefault(pos, 0);
            if (used < hatch.getChannelCapacity()) {
                counts.put(pos, used + 1);
                return pos;
            }
        }
        return null;
    }

    private void syncRuntimeConnections(List<SavedWirelessLink> reservedLinks) {
        List<WirelessMachineRef> activatable = new ArrayList<>();
        for (var link : reservedLinks) {
            var machine = getLoadedMachine(link.machineRef());
            if (machine != null && !machine.hasPhysicalGridConnection()) {
                activatable.add(link.machineRef());
            }
        }

        int maxActive = activatable.size();
        if (currentTier.getEuPerConnection() > 0) {
            maxActive = (int) Math.min(maxActive, energyContainer.getEnergyStored() / currentTier.getEuPerConnection());
        }

        Set<WirelessMachineRef> shouldBeActive = new LinkedHashSet<>();
        for (int i = 0; i < maxActive; i++) {
            shouldBeActive.add(activatable.get(i));
        }

        for (var ref : shouldBeActive) {
            ensureRuntimeConnection(ref);
        }

        for (var iterator = runtimeConnections.entrySet().iterator();
             iterator.hasNext();) {
            var entry = iterator.next();
            if (!shouldBeActive.contains(entry.getKey())) {
                destroyConnection(entry.getValue());
                iterator.remove();
            }
        }

        long energyCost = currentTier.getEuPerConnection() * shouldBeActive.size();
        if (energyCost > 0) {
            energyContainer.removeEnergy(energyCost);
        }
    }

    private void ensureRuntimeConnection(WirelessMachineRef ref) {
        if (runtimeConnections.containsKey(ref)) {
            return;
        }
        var machine = getLoadedMachine(ref);
        var hatch = getAssignedHatch(ref);
        if (machine == null || hatch == null || machine.getMainNode().getNode() == null || hatch.getMainNode().getNode() == null) {
            return;
        }
        try {
            runtimeConnections.put(ref, GridHelper.createConnection(hatch.getMainNode().getNode(), machine.getMainNode().getNode()));
        } catch (IllegalStateException ignored) {
        }
    }

    @Nullable
    private MEChannelInputHatchMachine getAssignedHatch(WirelessMachineRef ref) {
        for (var link : savedLinks) {
            if (link.machineRef().equals(ref) && link.hatchPos() != null) {
                for (var hatch : channelHatches) {
                    if (hatch.getPos().equals(link.hatchPos())) {
                        return hatch;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private IWirelessAEMachine getLoadedMachine(WirelessMachineRef ref) {
        var level = getLoadedLevel(ref.dimension());
        if (level == null || !level.hasChunkAt(ref.pos())) {
            return null;
        }
        if (getMachine(level, ref.pos()) instanceof IWirelessAEMachine machine) {
            return machine;
        }
        return null;
    }

    @Nullable
    private ServerLevel getLoadedLevel(ResourceLocation dimension) {
        if (!(getLevel() instanceof ServerLevel towerLevel)) {
            return null;
        }
        var levelKey = ResourceKey.create(Registries.DIMENSION, dimension);
        return towerLevel.getServer().getLevel(levelKey);
    }

    private void clearReservations() {
        reservedMachineRefs.clear();
        for (var hatch : channelHatches) {
            hatch.setReservedConnections(0);
        }
    }

    private void destroyRuntimeConnections() {
        for (var connection : runtimeConnections.values()) {
            destroyConnection(connection);
        }
        runtimeConnections.clear();
    }

    private void destroyRuntimeConnection(WirelessMachineRef ref) {
        var connection = runtimeConnections.remove(ref);
        destroyConnection(connection);
    }

    private void destroyConnection(@Nullable IGridConnection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.destroy();
        } catch (RuntimeException ignored) {
        }
    }

    public int getActiveConnectionCount() {
        return runtimeConnections.size();
    }

    public int getUsableConnectionCapacity() {
        int hatchCapacity = 0;
        for (var hatch : channelHatches) {
            hatchCapacity += hatch.getChannelCapacity();
        }
        return Math.min(currentTier.getMaxConnections(), hatchCapacity);
    }

    private String getStatusLine(int row) {
        if (row < 0 || row >= savedLinks.size()) {
            return "";
        }
        var link = savedLinks.get(row);
        var state = translate(runtimeConnections.containsKey(link.machineRef()) ?
                "start_core.gui.me_radio_tower.status.online" :
                reservedMachineRefs.contains(link.machineRef()) ?
                "start_core.gui.me_radio_tower.status.standby" :
                "start_core.gui.me_radio_tower.status.saved");
        var machine = getLoadedMachine(link.machineRef());
        var name = machine != null ? machine.getDisplayName() :
                translate("start_core.gui.me_radio_tower.unloaded_machine");
        return translate("start_core.gui.me_radio_tower.status_row", state, name);
    }

    private void locateRow(int row, Player player) {
        if (row < 0 || row >= savedLinks.size()) {
            return;
        }
        var ref = savedLinks.get(row).machineRef();
        player.sendSystemMessage(Component.translatable("start_core.machine.me_radio_tower.machine_location", ref.dimensionalPos()));
    }

    private void disconnectRow(int row, Player player) {
        if (row < 0 || row >= savedLinks.size()) {
            return;
        }
        disconnectMachine(savedLinks.get(row).machineRef(), player);
    }

    private void sendMessage(@Nullable Player player, Component message) {
        if (player != null) {
            player.sendSystemMessage(message);
        }
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190 + 66, 156);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);

        group.addWidget(new LabelWidget(6, 6, () -> translate("start_core.gui.me_radio_tower.tower_id",
                WirelessTowerDataStickHelper.shortId(towerId))));
        group.addWidget(new LabelWidget(6, 18, () -> translate("start_core.gui.me_radio_tower.tier_range",
                currentTier.getDisplayName(), currentTier.getRangeText())));
        group.addWidget(new LabelWidget(6, 30, () -> translate("start_core.gui.me_radio_tower.connections",
                reservedMachineRefs.size(), getUsableConnectionCapacity(), runtimeConnections.size(),
                savedLinks.size())));
        group.addWidget(new LabelWidget(6, 42, () -> translate("start_core.gui.me_radio_tower.eu_per_tick",
                runtimeConnections.size() * currentTier.getEuPerConnection())));

        var scroll = new DraggableScrollableWidgetGroup(4, 56, 182 + 66, 96).setBackground(GuiTextures.DISPLAY);
        for (int i = 0; i < MAX_UI_ROWS; i++) {
            int row = i;
            int y = 4 + i * 18;
            scroll.addWidget(new LabelWidget(4, y + 3, () -> getStatusLine(row)));
            scroll.addWidget(new ButtonWidget(128 + 66, y, 22, 14,
                    buttonTexture("start_core.gui.me_radio_tower.button.locate"), cd -> {
                if (!cd.isRemote) {
                    locateRow(row, group.getGui().entityPlayer);
                }
            }));
            scroll.addWidget(new ButtonWidget(154 + 66, y, 22, 14,
                    buttonTexture("start_core.gui.me_radio_tower.button.disconnect"), cd -> {
                if (!cd.isRemote) {
                    disconnectRow(row, group.getGui().entityPlayer);
                }
            }));
        }
        group.addWidget(scroll);
        return group;
    }

    private boolean hasStatusRow(int row) {
        return row >= 0 && row < savedLinks.size();
    }

    /*private static class RowButtonWidget extends ButtonWidget {

        private final BooleanSupplier visible;

        private RowButtonWidget(int xPosition, int yPosition, int width, int height, GuiTextureGroup buttonTexture,
                                BooleanSupplier visible, Consumer<ClickData> onPressed) {
            super(xPosition, yPosition, width, height, buttonTexture, onPressed);
            this.visible = visible;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (visible.getAsBoolean()) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return visible.getAsBoolean() && super.mouseClicked(mouseX, mouseY, button);
        }
    }*/

    private static GuiTextureGroup buttonTexture(String text) {
        return new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture(text));
    }

    private static String translate(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(210, 242, this, entityPlayer).widget(new FancyMachineUIWidget(this, 210, 242));
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            WirelessTowerDataStickHelper.writeTower(dataStick, this);
            player.sendSystemMessage(Component.translatable("start_core.machine.me_radio_tower.copied_to_data_stick",
                    WirelessTowerDataStickHelper.shortId(towerId)));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putUUID(TAG_TOWER_ID, towerId);
        tag.putString(TAG_TIER, currentTier.name());
        var links = new ListTag();
        for (SavedWirelessLink link : savedLinks) {
            links.add(link.save());
        }
        tag.put(TAG_LINKS, links);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        towerId = tag.hasUUID(TAG_TOWER_ID) ? tag.getUUID(TAG_TOWER_ID) : UUID.randomUUID();
        if (tag.contains(TAG_TIER)) {
            try {
                currentTier = TowerTier.valueOf(tag.getString(TAG_TIER));
            } catch (IllegalArgumentException ignored) {
                currentTier = TowerTier.STEEL;
            }
        }
        savedLinks.clear();
        if (tag.contains(TAG_LINKS, Tag.TAG_LIST)) {
            var links = tag.getList(TAG_LINKS, Tag.TAG_COMPOUND);
            for (int i = 0; i < links.size(); i++) {
                savedLinks.add(SavedWirelessLink.load(links.getCompound(i)));
            }
        }
    }
}
