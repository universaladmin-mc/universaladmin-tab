package dev.universaladmin.tab;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brings TAB in line with UniversalAdmin, player by player, on every
 * {@link #refresh}: a nicked player gets the nick group (so their rank's
 * prefix and sorting do not give them away); everyone is listed under the
 * name their profile has now - TAB only knows the one they joined with,
 * which is the nick for a player who joined nicked; vanished players and
 * players in staff mode get a marker after their name. Only what changed
 * is sent to TAB, and only values this class set are ever given back -
 * TAB's own configuration and other plugins' API values stay untouched.
 */
final class TabSync {

    /**
     * @param nickGroup         the TAB group of nicked players, empty to keep their own
     * @param vanishMarker      appended for vanished players, empty for none
     * @param staffModeMarker   appended for players in staff mode, empty for none
     * @param markersOnNameTags the markers also go after the name tag, not only in the tab list
     */
    record Settings(String nickGroup, String vanishMarker, String staffModeMarker, boolean markersOnNameTags) {
    }

    /** What UniversalAdmin says about a player right now. */
    interface Status {

        /** The name everyone sees: the nick, or the player's own name. */
        String shownName(UUID playerId);

        Optional<String> nick(UUID playerId);

        boolean vanished(UUID playerId);

        boolean staffMode(UUID playerId);
    }

    private record Applied(String group, String name, String suffix, String tagSuffix) {

        static final Applied NONE = new Applied(null, null, null, null);
    }

    private static final String PLAYER_PLACEHOLDER = "%player%";

    private final TabDisplay display;
    private final Map<UUID, Applied> applied = new ConcurrentHashMap<>();

    TabSync(TabDisplay display) {
        this.display = display;
    }

    void refresh(Collection<UUID> online, Status status, Settings settings) {
        for (UUID playerId : online) {
            if (display.loaded(playerId)) {
                refresh(playerId, status, settings);
            }
        }
    }

    private void refresh(UUID playerId, Status status, Settings settings) {
        Applied before = applied.getOrDefault(playerId, Applied.NONE);
        Optional<String> nick = status.nick(playerId);

        String group = nick.isPresent() && !settings.nickGroup().isBlank() ? settings.nickGroup() : null;
        if (!Objects.equals(group, before.group())) {
            display.group(playerId, group);
        }
        String shown = status.shownName(playerId);
        display.profileName(playerId, shown);
        String name = shown.equals(display.knownName(playerId)) ? null : nameFor(playerId, shown);
        if (!Objects.equals(name, before.name())) {
            display.name(playerId, name);
        }

        String marker = (status.vanished(playerId) ? settings.vanishMarker() : "")
                + (status.staffMode(playerId) ? settings.staffModeMarker() : "");
        String suffix = withMarker(display.configuredSuffix(playerId), marker);
        if (!Objects.equals(suffix, before.suffix())) {
            display.suffix(playerId, suffix);
        }
        String tagSuffix = settings.markersOnNameTags() ? withMarker(display.configuredTagSuffix(playerId), marker) : null;
        if (!Objects.equals(tagSuffix, before.tagSuffix())) {
            display.tagSuffix(playerId, tagSuffix);
        }

        Applied now = new Applied(group, name, suffix, tagSuffix);
        if (now.equals(Applied.NONE)) {
            applied.remove(playerId);
        } else {
            applied.put(playerId, now);
        }
    }

    /**
     * The configured tab-list name with {@code shown} for {@code %player%},
     * which TAB fills with the name it knows. A configured name without it
     * (a placeholder of another plugin, say) follows the profile already
     * and is left alone.
     */
    private String nameFor(UUID playerId, String shown) {
        String configured = display.configuredName(playerId);
        if (configured == null || configured.isBlank()) {
            return shown;
        }
        return configured.contains(PLAYER_PLACEHOLDER) ? configured.replace(PLAYER_PLACEHOLDER, shown) : null;
    }

    private static String withMarker(String configured, String marker) {
        if (marker.isEmpty() || configured == null) {
            return null;
        }
        return configured + marker;
    }

    /** The player left - TAB forgets its API values with them. */
    void forget(UUID playerId) {
        applied.remove(playerId);
    }

    /** TAB reloaded and dropped every value set through its API - the next refresh sets them again. */
    void forgetAll() {
        applied.clear();
    }

    /** Gives every value this class set back to TAB's configuration - when the extension stops. */
    void giveBack() {
        applied.forEach((playerId, values) -> {
            if (!display.loaded(playerId)) {
                return;
            }
            if (values.group() != null) {
                display.group(playerId, null);
            }
            if (values.name() != null) {
                display.name(playerId, null);
            }
            if (values.suffix() != null) {
                display.suffix(playerId, null);
            }
            if (values.tagSuffix() != null) {
                display.tagSuffix(playerId, null);
            }
        });
        applied.clear();
    }
}
