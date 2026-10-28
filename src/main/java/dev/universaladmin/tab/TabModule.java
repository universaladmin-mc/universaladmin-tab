package dev.universaladmin.tab;

import dev.universaladmin.core.UniversalAdmin;
import dev.universaladmin.module.GuiIcon;
import dev.universaladmin.module.Module;
import dev.universaladmin.module.ModuleContext;
import dev.universaladmin.module.ModuleDescriptor;
import dev.universaladmin.module.ModuleId;
import dev.universaladmin.settings.SettingDefinition;
import dev.universaladmin.settings.SettingKey;
import dev.universaladmin.settings.SettingTypes;
import dev.universaladmin.settings.SettingsService;
import dev.universaladmin.status.Nicks;
import dev.universaladmin.status.PlayerStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.EventHandler;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Everything UniversalAdmin TAB adds while the module is enabled: its
 * settings, and a check twice a second that brings TAB in line with
 * nicks, vanish and staff mode ({@link TabSync}). Vanished players are
 * hidden by TAB itself - UniversalAdmin sets the {@code vanished} metadata
 * TAB looks for.
 */
final class TabModule implements Module {

    static final ModuleId ID = ModuleId.of("tab", "tab");
    static final SettingKey<String> NICK_GROUP = SettingKey.of("tab", "tab.nick-group");
    static final SettingKey<String> VANISH_MARKER = SettingKey.of("tab", "tab.vanish-marker");
    static final SettingKey<String> STAFF_MODE_MARKER = SettingKey.of("tab", "tab.staff-mode-marker");
    static final SettingKey<Boolean> MARKERS_ON_NAME_TAGS = SettingKey.of("tab", "tab.markers-on-name-tags");

    /** Ten ticks: a change shows up within half a second, at the cost of a few map lookups per player. */
    private static final long REFRESH_TICKS = 10L;

    private static final ModuleDescriptor DESCRIPTOR = ModuleDescriptor.builder(ID, "TAB")
            .description("TAB: the tab list and name tags follow nicks, vanish and staff mode.")
            .icon(new GuiIcon("oak_sign", "TAB"))
            .build();

    private final UniversalAdmin platform;

    TabModule(UniversalAdmin platform) {
        this.platform = platform;
    }

    @Override
    public ModuleDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public void onEnable(ModuleContext context) {
        SettingsService settings = platform.settings();
        context.registerSetting(SettingDefinition.builder(NICK_GROUP, SettingTypes.STRING, "default")
                .description("The TAB group nicked players are shown in (its prefix, suffix and sorting), so a nick does not "
                        + "give away the rank. Empty: they keep their own.")
                .build());
        context.registerSetting(SettingDefinition.builder(VANISH_MARKER, SettingTypes.STRING, " &7[V]")
                .description("Added after the name of vanished players - only staff who can see them notice it. Empty: none.")
                .build());
        context.registerSetting(SettingDefinition.builder(STAFF_MODE_MARKER, SettingTypes.STRING, " &c[Staff]")
                .description("Added after the name of players in staff mode. Empty: none.")
                .build());
        context.registerSetting(SettingDefinition.builder(MARKERS_ON_NAME_TAGS, SettingTypes.BOOLEAN, true)
                .description("The markers also go after the name tag above the head, not only in the tab list.")
                .build());

        TabSync sync = new TabSync(new NeznamyTabDisplay());
        TabSync.Status status = new TabSync.Status() {
            @Override
            public String shownName(UUID playerId) {
                Player player = platform.plugin().getServer().getPlayer(playerId);
                return player == null ? "" : player.getName();
            }

            @Override
            public Optional<String> nick(UUID playerId) {
                return platform.services().get(Nicks.class).flatMap(nicks -> nicks.nick(playerId)).map(Nicks.Nick::name);
            }

            @Override
            public boolean vanished(UUID playerId) {
                return platform.services().get(PlayerStatus.class).map(players -> players.vanished(playerId)).orElse(false);
            }

            @Override
            public boolean staffMode(UUID playerId) {
                return platform.services().get(PlayerStatus.class).map(players -> players.staffMode(playerId)).orElse(false);
            }
        };
        Runnable refresh = () -> {
            List<UUID> online = platform.plugin().getServer().getOnlinePlayers().stream().map(Player::getUniqueId).toList();
            sync.refresh(online, status, new TabSync.Settings(settings.get(NICK_GROUP).trim(), settings.get(VANISH_MARKER),
                    settings.get(STAFF_MODE_MARKER), settings.get(MARKERS_ON_NAME_TAGS)));
        };
        context.resources().task(platform.plugin().getServer().getScheduler().runTaskTimer(
                platform.plugin(), refresh, REFRESH_TICKS, REFRESH_TICKS));

        // A /tab reload drops everything set through TAB's API.
        EventBus events = TabAPI.getInstance().getEventBus();
        if (events != null) {
            EventHandler<TabLoadEvent> reloaded = event -> sync.forgetAll();
            events.register(TabLoadEvent.class, reloaded);
            context.resources().closeable(() -> events.unregister(reloaded));
        }
        context.registerListener(new Listener() {
            @org.bukkit.event.EventHandler
            public void onQuit(PlayerQuitEvent event) {
                sync.forget(event.getPlayer().getUniqueId());
            }
        });
        context.resources().closeable(sync::giveBack);
    }
}
