package com.chrober.lmsmaterial.nowplaying;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public final class SettingsConnectionTest {
    private final List<LyrionPlayers.Player> players = Arrays.asList(
            new LyrionPlayers.Player("aa:bb", "Living Room"),
            new LyrionPlayers.Player("cc:dd", "Kitchen"));

    @Test public void acceptsSelectedPlayerOnlyForExactlyLoadedServer() {
        assertEquals("aa:bb", SettingsConnection.validPlayerId(
                "http://lyrion.local:9000", "http://lyrion.local:9000", players, 1));
        assertEquals("cc:dd", SettingsConnection.validPlayerId(
                "http://lyrion.local:9000", "http://lyrion.local:9000", players, 2));
    }

    @Test public void refusesPendingEditOrUnknownServer() {
        assertNull(SettingsConnection.validPlayerId(
                "ftp://not-valid", "http://lyrion.local:9000", players, 1));
        assertNull(SettingsConnection.validPlayerId(
                "http://new.local:9000", "http://lyrion.local:9000", players, 1));
        assertNull(SettingsConnection.validPlayerId(
                "http://lyrion.local:9000", "", players, 1));
    }

    @Test public void refusesPlaceholderOutOfRangeAndEmptyPlayerResponses() {
        assertNull(SettingsConnection.validPlayerId(
                "http://lyrion.local:9000", "http://lyrion.local:9000", players, 0));
        assertNull(SettingsConnection.validPlayerId(
                "http://lyrion.local:9000", "http://lyrion.local:9000", players, 3));
        assertNull(SettingsConnection.validPlayerId(
                "http://lyrion.local:9000", "http://lyrion.local:9000",
                Collections.emptyList(), 1));
        assertNull(SettingsConnection.validPlayerId(
                "http://lyrion.local:9000", "http://lyrion.local:9000", null, 1));
    }

    @Test public void refusesBlankPlayerId() {
        assertNull(SettingsConnection.validPlayerId(
                "http://lyrion.local:9000", "http://lyrion.local:9000",
                Collections.singletonList(new LyrionPlayers.Player("", "No ID")), 1));
    }
}
