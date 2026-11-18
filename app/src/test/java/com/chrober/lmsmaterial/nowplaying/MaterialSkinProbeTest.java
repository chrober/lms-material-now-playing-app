package com.chrober.lmsmaterial.nowplaying;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaterialSkinProbeTest {
    @Test public void acceptsActualMaterialSkinNowPlayingRedirect() {
        String html = "<script>window.location.href = '/material/?player=Kitchen"
                + "&layout=desktop&npAutoClose=false&css=msk--np-only&single=true';</script>";
        assertEquals(MaterialSkinProbe.Status.AVAILABLE,
                MaterialSkinProbe.classify(200, html).status);
    }

    @Test public void treatsMissingHandlerAsUnavailable() {
        assertEquals(MaterialSkinProbe.Status.MISSING,
                MaterialSkinProbe.classify(404, "<html>Not Found</html>").status);
        assertEquals(MaterialSkinProbe.Status.MISSING,
                MaterialSkinProbe.classify(410, "").status);
    }

    @Test public void rejectsFallbackHtmlAndLoginPage() {
        assertEquals(MaterialSkinProbe.Status.UNEXPECTED_PAGE,
                MaterialSkinProbe.classify(200, "<html>Welcome to Lyrion</html>").status);
        assertEquals(MaterialSkinProbe.Status.UNEXPECTED_PAGE,
                MaterialSkinProbe.classify(200, "<html>Login required</html>").status);
    }

    @Test public void retriesPossibleColdStartFailures() {
        MaterialSkinProbe.Result missing =
                new MaterialSkinProbe.Result(MaterialSkinProbe.Status.MISSING, 404);
        MaterialSkinProbe.Result unexpected =
                new MaterialSkinProbe.Result(MaterialSkinProbe.Status.UNEXPECTED_PAGE, 200);
        MaterialSkinProbe.Result timeout =
                new MaterialSkinProbe.Result(MaterialSkinProbe.Status.TIMEOUT, 0);
        MaterialSkinProbe.Result unavailable =
                new MaterialSkinProbe.Result(MaterialSkinProbe.Status.HTTP_ERROR, 503);

        assertEquals(true, MaterialSkinProbe.shouldRetry(missing, 1));
        assertEquals(true, MaterialSkinProbe.shouldRetry(missing, 2));
        assertEquals(false, MaterialSkinProbe.shouldRetry(missing, 3));
        assertEquals(true, MaterialSkinProbe.shouldRetry(unexpected, 1));
        assertEquals(true, MaterialSkinProbe.shouldRetry(timeout, 1));
        assertEquals(false, MaterialSkinProbe.shouldRetry(timeout, 2));
        assertEquals(true, MaterialSkinProbe.shouldRetry(unavailable, 1));
    }

    @Test public void doesNotRetrySuccessOrAuthorizationFailures() {
        assertEquals(false, MaterialSkinProbe.shouldRetry(
                new MaterialSkinProbe.Result(MaterialSkinProbe.Status.AVAILABLE, 200), 1));
        assertEquals(false, MaterialSkinProbe.shouldRetry(
                new MaterialSkinProbe.Result(MaterialSkinProbe.Status.UNAUTHORIZED, 401), 1));
        assertEquals(false, MaterialSkinProbe.shouldRetry(
                new MaterialSkinProbe.Result(MaterialSkinProbe.Status.HTTP_ERROR, 302), 1));
    }

    @Test public void separatesAuthorizationFromOtherHttpErrors() {
        assertEquals(MaterialSkinProbe.Status.UNAUTHORIZED,
                MaterialSkinProbe.classify(401, "").status);
        assertEquals(MaterialSkinProbe.Status.UNAUTHORIZED,
                MaterialSkinProbe.classify(403, "").status);
        assertEquals(MaterialSkinProbe.Status.HTTP_ERROR,
                MaterialSkinProbe.classify(500, "").status);
        assertEquals(MaterialSkinProbe.Status.HTTP_ERROR,
                MaterialSkinProbe.classify(302, "").status);
    }
}
