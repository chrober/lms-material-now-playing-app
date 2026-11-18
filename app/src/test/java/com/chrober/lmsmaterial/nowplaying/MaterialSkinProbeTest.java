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
