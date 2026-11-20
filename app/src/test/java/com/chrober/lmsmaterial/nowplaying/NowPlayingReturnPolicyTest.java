package com.chrober.lmsmaterial.nowplaying;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static com.chrober.lmsmaterial.nowplaying.NowPlayingReturnPolicy.Action.*;

public final class NowPlayingReturnPolicyTest {
    private static final String SERVER = "http://lyrion.local:9000";
    private static final String URL = SERVER + "/material/np-only?player=aa:bb&theme=dark";
    private static final String OPTIONS = "script:progress-on;clock-off";

    @Test public void unchangedSettingsReuseTheLoadedWebView() {
        assertEquals(REUSE, NowPlayingReturnPolicy.decide(
                true, SERVER, SERVER, URL, URL, OPTIONS, OPTIONS));
    }

    @Test public void visualOptionsApplyWithoutAnotherServerProbeOrNewWebView() {
        assertEquals(APPLY_OPTIONS, NowPlayingReturnPolicy.decide(
                true, SERVER, SERVER, URL, URL, OPTIONS, "script:progress-off;clock-on"));
    }

    @Test public void themeOrPlayerChangesReloadWithinExistingServer() {
        assertEquals(RELOAD_PAGE, NowPlayingReturnPolicy.decide(
                true, SERVER, SERVER, URL, URL + "&theme=light", OPTIONS, OPTIONS));
        assertEquals(RELOAD_PAGE, NowPlayingReturnPolicy.decide(
                true, SERVER, SERVER, URL, SERVER + "/material/np-only?player=cc:dd",
                OPTIONS, OPTIONS));
    }

    @Test public void differentOriginAlwaysRecreatesTrustedWebView() {
        assertEquals(RECONNECT, NowPlayingReturnPolicy.decide(
                true, SERVER, "http://different.local:9000", URL, URL,
                OPTIONS, OPTIONS));
        assertEquals(RECONNECT, NowPlayingReturnPolicy.decide(
                true, SERVER, "https://lyrion.local", URL, URL, OPTIONS, OPTIONS));
    }

    @Test public void failedOrIncompleteWebViewIsNeverReused() {
        assertEquals(RECONNECT, NowPlayingReturnPolicy.decide(
                false, SERVER, SERVER, URL, URL, OPTIONS, OPTIONS));
        assertEquals(RECONNECT, NowPlayingReturnPolicy.decide(
                true, null, SERVER, URL, URL, OPTIONS, OPTIONS));
    }

    @Test public void missingUrlReloadsAndMissingOptionsAreReapplied() {
        assertEquals(RELOAD_PAGE, NowPlayingReturnPolicy.decide(
                true, SERVER, SERVER, null, URL, OPTIONS, OPTIONS));
        assertEquals(APPLY_OPTIONS, NowPlayingReturnPolicy.decide(
                true, SERVER, SERVER, URL, URL, null, OPTIONS));
    }
}
