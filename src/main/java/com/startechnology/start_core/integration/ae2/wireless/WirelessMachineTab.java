package com.startechnology.start_core.integration.ae2.wireless;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessMachineTab implements IFancyUIProvider {

    private final IWirelessAEMachine machine;

    public WirelessMachineTab(IWirelessAEMachine machine) {
        this.machine = machine;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var group = new WidgetGroup(0, 0, 170, 96);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);

        group.addWidget(new LabelWidget(6, 6, "start_core.gui.wireless_machine_tab.configured_tower"));
        group.addWidget(new LabelWidget(104, 6, this::getTowerLabel));
        group.addWidget(new LabelWidget(6, 20, () -> machine.findTower() != null ?
                "start_core.gui.wireless_machine_tab.tower_status.loaded" :
                "start_core.gui.wireless_machine_tab.tower_status.not_loaded"));
        group.addWidget(new LabelWidget(6, 34, this::getLinkState));
        group.addWidget(new LabelWidget(6, 48, () -> machine.hasPhysicalGridConnection() ?
                "start_core.gui.wireless_machine_tab.cable_link.detected" :
                "start_core.gui.wireless_machine_tab.cable_link.none"));

        group.addWidget(new ButtonWidget(6, 68, 48, 16,
                buttonTexture("start_core.gui.wireless_machine_tab.button.connect"), cd -> {
            if (!cd.isRemote) {
                var tower = machine.findTower();
                if (tower == null) {
                    if (!(machine.self().getLevel() instanceof ServerLevel level)) {
                        return;
                    }
                    tower = WirelessTowerRegistry.findNearestTower(level, machine.getMachineRef());
                    if (tower == null) {
                        group.getGui().entityPlayer.sendSystemMessage(
                                Component.translatable("start_core.machine.me_radio_tower.no_nearby_tower"));
                        return;
                    }
                }
                tower.requestLink(machine, group.getGui().entityPlayer);
            }
        }));
        group.addWidget(new ButtonWidget(114, 68, 48, 16,
                buttonTexture("start_core.gui.wireless_machine_tab.button.drop"), cd -> {
            if (!cd.isRemote) {
                var tower = machine.findTower();
                if (tower == null) {
                    group.getGui().entityPlayer.sendSystemMessage(
                            Component.translatable("start_core.machine.me_radio_tower.not_loaded"));
                    return;
                }
                tower.disconnectMachine(machine.getMachineRef(), group.getGui().entityPlayer);
            }
        }));
        group.addWidget(new ButtonWidget(114, 68, 48, 16,
                buttonTexture("start_core.gui.wireless_machine_tab.button.clear"), cd -> {
            if (!cd.isRemote) {
                var tower = machine.findTower();
                if (tower != null) {
                    tower.disconnectMachine(machine.getMachineRef(), null);
                }
                machine.setConfiguredTowerId(null);
                machine.self().onChanged();
                group.getGui().entityPlayer.sendSystemMessage(
                        Component.translatable("start_core.machine.me_radio_tower.assignment_cleared"));
            }
        }));

        return group;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(Items.ENDER_EYE);
    }

    @Override
    public Component getTitle() {
        return Component.translatable("start_core.gui.wireless_machine_tab.title");
    }

    @Override
    public List<Component> getTabTooltips() {
        return List.of(Component.translatable("start_core.gui.wireless_machine_tab.tooltip"));
    }

    private String getTowerLabel() {
        if (machine.getConfiguredTowerId() == null) {
            return "start_core.gui.wireless_machine_tab.none";
        }
        return WirelessTowerDataStickHelper.shortId(machine.getConfiguredTowerId());
    }

    private String getLinkState() {
        if (machine.getConfiguredTowerId() == null) {
            return "start_core.gui.wireless_machine_tab.wireless_link.not_configured";
        }
        if (machine.findTower() == null) {
            return "start_core.gui.wireless_machine_tab.wireless_link.waiting";
        }
        if (machine.isLinkedToTower()) {
            return machine.isOnline() ?
                    "start_core.gui.wireless_machine_tab.wireless_link.online" :
                    "start_core.gui.wireless_machine_tab.wireless_link.standby";
        }
        return "start_core.gui.wireless_machine_tab.wireless_link.disconnected";
    }

    private static GuiTextureGroup buttonTexture(String text) {
        return new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture(text));
    }
}
