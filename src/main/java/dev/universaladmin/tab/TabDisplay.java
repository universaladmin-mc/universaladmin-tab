package dev.universaladmin.tab;

import java.util.UUID;

/**
 * The part of TAB this extension changes, per player - what {@link TabSync}
 * talks to. Every setter takes {@code null} to give the value back to
 * TAB's own configuration.
 */
interface TabDisplay {

    /** Whether TAB has finished loading the player (it loads them asynchronously after they join). */
    boolean loaded(UUID playerId);

    /**
     * The name TAB knows the player by - the one they joined with, also
     * when that was a nick. Its {@code %player%} placeholder shows it.
     */
    String knownName(UUID playerId);

    /**
     * Tells TAB the name the player's profile has now, for everything TAB
     * binds to names (name-tag teams, scores) - nothing if it knows already.
     */
    void profileName(UUID playerId, String name);

    /** A TAB group for the player instead of their permission group - prefix, suffix and sorting follow it. */
    void group(UUID playerId, String group);

    /** The tab-list name as configured (placeholders unreplaced); {@code null} if the feature is off. */
    String configuredName(UUID playerId);

    void name(UUID playerId, String name);

    /** The tab-list suffix as configured; {@code null} if the feature is off. */
    String configuredSuffix(UUID playerId);

    void suffix(UUID playerId, String suffix);

    /** The name-tag suffix as configured; {@code null} if TAB's name tags are off. */
    String configuredTagSuffix(UUID playerId);

    void tagSuffix(UUID playerId, String suffix);
}
