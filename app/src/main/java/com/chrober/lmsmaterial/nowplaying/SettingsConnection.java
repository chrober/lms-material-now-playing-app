package com.chrober.lmsmaterial.nowplaying;

import java.util.List;

/** Keeps the last working server/player pair intact until a valid selection exists. */
final class SettingsConnection {
    private SettingsConnection() { }

    /** Returns the chosen player's ID only when it belongs to the loaded server. */
    static String validPlayerId(String editedServer, String loadedServer,
            List<LyrionPlayers.Player> players, int selection) {
        String normalized = ServerAddress.normalize(editedServer);
        if (normalized == null || !normalized.equals(loadedServer)
                || players == null || selection < 1 || selection > players.size())
            return null;
        LyrionPlayers.Player player = players.get(selection - 1);
        return player == null || player.id == null || player.id.isEmpty()
                ? null : player.id;
    }
}
