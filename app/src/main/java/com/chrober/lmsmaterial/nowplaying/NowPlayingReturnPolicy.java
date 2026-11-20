package com.chrober.lmsmaterial.nowplaying;

/** Decide how much work is needed when closing native Settings. */
final class NowPlayingReturnPolicy {
    enum Action { REUSE, APPLY_OPTIONS, RELOAD_PAGE, RECONNECT }

    private NowPlayingReturnPolicy() { }

    static Action decide(boolean webViewReady, String previousServer, String nextServer,
            String previousUrl, String nextUrl, String previouslyAppliedOptions,
            String nextOptions) {
        // A new origin requires a fresh WebView with a new origin-restricted client.
        // An incomplete/failed first load must always use the guarded cold path.
        if (!webViewReady || previousServer == null || !previousServer.equals(nextServer))
            return Action.RECONNECT;
        if (previousUrl == null || !previousUrl.equals(nextUrl))
            return Action.RELOAD_PAGE;
        if (previouslyAppliedOptions == null
                || !previouslyAppliedOptions.equals(nextOptions))
            return Action.APPLY_OPTIONS;
        return Action.REUSE;
    }
}
