package com.chrober.lmsmaterial.nowplaying;

import org.junit.Test;
import static org.junit.Assert.*;

public class LyrionPlayersTest {
    @Test public void parsesPlayerIdsAndNamesAndIgnoresInvalidEntries() throws Exception {
        String reply = "{\"result\":{\"players_loop\":["
                + "{\"playerid\":\"aa:bb\",\"name\":\"Kitchen\"},"
                + "{\"playerid\":\"cc:dd\"},"
                + "{\"name\":\"Missing id\"},null,42]}}";
        java.util.List<LyrionPlayers.Player> players = LyrionPlayers.parsePlayers(reply);
        assertEquals(2, players.size());
        assertEquals("aa:bb", players.get(0).id);
        assertEquals("Kitchen", players.get(0).toString());
        assertEquals("cc:dd", players.get(1).toString());
    }

    @Test public void emptyPlayerListIsValid() throws Exception {
        assertTrue(LyrionPlayers.parsePlayers("{\"result\":{\"players_loop\":[]}}").isEmpty());
    }

    @Test public void rejectsJsonRpcErrors() throws Exception {
        try {
            LyrionPlayers.parsePlayers("{\"error\":{\"code\":-1,\"message\":\"Denied\"}}");
            fail("Expected JSON-RPC error");
        } catch (java.io.IOException expected) {
            assertTrue(expected.getMessage().contains("Denied"));
        }
    }

    @Test public void rejectsUnexpectedAndMalformedResponses() {
        for (String response : new String[]{"{}", "not json", "{\"result\":null}"}) {
            try {
                LyrionPlayers.parsePlayers(response);
                fail("Unexpectedly accepted: " + response);
            } catch (Exception expected) {
                // A response without a JSON-RPC result is not an empty player list.
            }
        }
    }
}
