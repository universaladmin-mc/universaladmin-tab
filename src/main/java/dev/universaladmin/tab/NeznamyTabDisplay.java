package dev.universaladmin.tab;

import java.util.UUID;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;

/**
 * {@link TabDisplay} through TAB's API. The tab-list and name-tag managers
 * are {@code null} while the feature is off in TAB's config - their values
 * are then left alone.
 */
final class NeznamyTabDisplay implements TabDisplay {

    @Override
    public boolean loaded(UUID playerId) {
        return player(playerId) != null;
    }

    @Override
    public String knownName(UUID playerId) {
        TabPlayer player = player(playerId);
        return player == null ? null : player.getName();
    }

    @Override
    public void profileName(UUID playerId, String name) {
        TabPlayer player = player(playerId);
        if (player != null && !player.getExpectedProfileName().equals(name)) {
            player.setExpectedProfileName(name);
        }
    }

    @Override
    public void group(UUID playerId, String group) {
        TabPlayer player = player(playerId);
        if (player != null) {
            player.setTemporaryGroup(group);
        }
    }

    @Override
    public String configuredName(UUID playerId) {
        TabListFormatManager tabList = TabAPI.getInstance().getTabListFormatManager();
        TabPlayer player = player(playerId);
        return tabList == null || player == null ? null : tabList.getOriginalRawName(player);
    }

    @Override
    public void name(UUID playerId, String name) {
        TabListFormatManager tabList = TabAPI.getInstance().getTabListFormatManager();
        TabPlayer player = player(playerId);
        if (tabList != null && player != null) {
            tabList.setName(player, name);
        }
    }

    @Override
    public String configuredSuffix(UUID playerId) {
        TabListFormatManager tabList = TabAPI.getInstance().getTabListFormatManager();
        TabPlayer player = player(playerId);
        return tabList == null || player == null ? null : tabList.getOriginalRawSuffix(player);
    }

    @Override
    public void suffix(UUID playerId, String suffix) {
        TabListFormatManager tabList = TabAPI.getInstance().getTabListFormatManager();
        TabPlayer player = player(playerId);
        if (tabList != null && player != null) {
            tabList.setSuffix(player, suffix);
        }
    }

    @Override
    public String configuredTagSuffix(UUID playerId) {
        NameTagManager tags = TabAPI.getInstance().getNameTagManager();
        TabPlayer player = player(playerId);
        return tags == null || player == null ? null : tags.getOriginalRawSuffix(player);
    }

    @Override
    public void tagSuffix(UUID playerId, String suffix) {
        NameTagManager tags = TabAPI.getInstance().getNameTagManager();
        TabPlayer player = player(playerId);
        if (tags != null && player != null) {
            tags.setSuffix(player, suffix);
        }
    }

    private static TabPlayer player(UUID playerId) {
        TabPlayer player = TabAPI.getInstance().getPlayer(playerId);
        return player != null && player.isLoaded() ? player : null;
    }
}
