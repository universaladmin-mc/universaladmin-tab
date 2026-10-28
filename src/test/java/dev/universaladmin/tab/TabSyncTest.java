package dev.universaladmin.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TabSyncTest {

    private static final TabSync.Settings SETTINGS = new TabSync.Settings("default", " &7[V]", " &c[Staff]", true);

    private final UUID admin = UUID.randomUUID();
    private final FakeDisplay display = new FakeDisplay();
    private final FakeStatus status = new FakeStatus();
    private final TabSync sync = new TabSync(display);

    @Test
    void aNickGetsTheNickGroupAndTheNickAsName() {
        display.configuredName = "&b%player%";
        status.nicks.put(admin, "Notch");

        sync.refresh(List.of(admin), status, SETTINGS);

        assertEquals("default", display.group.get(admin));
        assertEquals("&bNotch", display.name.get(admin));
        assertEquals("Notch", display.profileName.get(admin));

        status.nicks.remove(admin);
        sync.refresh(List.of(admin), status, SETTINGS);

        assertNull(display.group.get(admin));
        assertNull(display.name.get(admin));
        assertEquals("Admin", display.profileName.get(admin));
    }

    @Test
    void someoneWhoJoinedNickedIsListedUnderTheirOwnNameAfterwards() {
        display.knownName = "Notch";
        display.configuredName = "&b%player%";

        sync.refresh(List.of(admin), status, SETTINGS);

        assertEquals("&bAdmin", display.name.get(admin));
        assertNull(display.group.get(admin));
    }

    @Test
    void aNameWithoutThePlayerPlaceholderIsLeftAlone() {
        display.configuredName = "%essentials_nickname%";
        status.nicks.put(admin, "Notch");

        sync.refresh(List.of(admin), status, SETTINGS);

        assertEquals(List.of("group"), display.calls);
        assertEquals("default", display.group.get(admin));
    }

    @Test
    void markersFollowVanishAndStaffModeAndOnlyChangesAreSent() {
        status.vanished.add(admin);

        sync.refresh(List.of(admin), status, SETTINGS);
        assertEquals("&f [Owner] &7[V]", display.suffix.get(admin));
        assertEquals(" &7[V]", display.tagSuffix.get(admin));

        status.staffMode.add(admin);
        sync.refresh(List.of(admin), status, SETTINGS);
        assertEquals("&f [Owner] &7[V] &c[Staff]", display.suffix.get(admin));

        display.calls.clear();
        sync.refresh(List.of(admin), status, SETTINGS);
        assertEquals(List.of(), display.calls, "nothing changed, nothing sent");

        status.vanished.clear();
        status.staffMode.clear();
        sync.refresh(List.of(admin), status, SETTINGS);
        assertNull(display.suffix.get(admin));
        assertNull(display.tagSuffix.get(admin));
    }

    @Test
    void aPlayerTabHasNotLoadedYetIsLeftForTheNextRound() {
        display.loaded = false;
        status.nicks.put(admin, "Notch");

        sync.refresh(List.of(admin), status, SETTINGS);

        assertEquals(List.of(), display.calls);
    }

    @Test
    void stoppingGivesBackOnlyWhatWasSet() {
        status.nicks.put(admin, "Notch");
        sync.refresh(List.of(admin), status, SETTINGS);
        display.calls.clear();

        sync.giveBack();

        assertEquals(List.of("group", "name"), display.calls);
        assertNull(display.group.get(admin));
    }

    @Test
    void afterATabReloadEverythingIsSetAgain() {
        status.nicks.put(admin, "Notch");
        sync.refresh(List.of(admin), status, SETTINGS);
        display.calls.clear();

        sync.forgetAll();
        sync.refresh(List.of(admin), status, SETTINGS);

        assertEquals(List.of("group", "name"), display.calls);
    }

    private static final class FakeDisplay implements TabDisplay {

        boolean loaded = true;
        String knownName = "Admin";
        String configuredName = "%player%";
        final Map<UUID, String> profileName = new HashMap<>();
        final Map<UUID, String> group = new HashMap<>();
        final Map<UUID, String> name = new HashMap<>();
        final Map<UUID, String> suffix = new HashMap<>();
        final Map<UUID, String> tagSuffix = new HashMap<>();
        final List<String> calls = new ArrayList<>();

        @Override
        public boolean loaded(UUID playerId) {
            return loaded;
        }

        @Override
        public String knownName(UUID playerId) {
            return knownName;
        }

        @Override
        public void profileName(UUID playerId, String value) {
            profileName.put(playerId, value);
        }

        @Override
        public void group(UUID playerId, String value) {
            calls.add("group");
            group.put(playerId, value);
        }

        @Override
        public String configuredName(UUID playerId) {
            return configuredName;
        }

        @Override
        public void name(UUID playerId, String value) {
            calls.add("name");
            name.put(playerId, value);
        }

        @Override
        public String configuredSuffix(UUID playerId) {
            return "&f [Owner]";
        }

        @Override
        public void suffix(UUID playerId, String value) {
            calls.add("suffix");
            suffix.put(playerId, value);
        }

        @Override
        public String configuredTagSuffix(UUID playerId) {
            return "";
        }

        @Override
        public void tagSuffix(UUID playerId, String value) {
            calls.add("tagSuffix");
            tagSuffix.put(playerId, value);
        }
    }

    private static final class FakeStatus implements TabSync.Status {

        final Map<UUID, String> nicks = new HashMap<>();
        final Set<UUID> vanished = new HashSet<>();
        final Set<UUID> staffMode = new HashSet<>();

        @Override
        public String shownName(UUID playerId) {
            return nicks.getOrDefault(playerId, "Admin");
        }

        @Override
        public Optional<String> nick(UUID playerId) {
            return Optional.ofNullable(nicks.get(playerId));
        }

        @Override
        public boolean vanished(UUID playerId) {
            return vanished.contains(playerId);
        }

        @Override
        public boolean staffMode(UUID playerId) {
            return staffMode.contains(playerId);
        }
    }
}
