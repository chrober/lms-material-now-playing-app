package com.chrober.lmsmaterial.nowplaying;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NowPlayingDisplayOptionsTest {
    @Test public void defaultsPreserveProgressAndQueueButHideTechnicalInformation() {
        String script = NowPlayingDisplayOptions.script(0, true, false, true);
        assertTrue(script.contains("var techWanted='false';"));
        assertTrue(script.contains("lms-material::techInfo"));
        assertFalse(script.contains(".np-tech{display:none"));
        assertFalse(script.contains(".np-tech{font-size:0"));
        assertFalse(script.contains(".np-controls #pos-slider{display:none"));
    }

    @Test public void progressIsIndependentOfPlaylistPosition() {
        String script = NowPlayingDisplayOptions.script(2, false, false, true);
        assertTrue(script.contains(".np-controls-wide .np-time-layout,"));
        assertTrue(script.contains(".np-controls .np-pos,"));
        assertTrue(script.contains(".np-controls .np-duration,"));
        assertTrue(script.contains(".np-controls #pos-slider{display:none!important;}"));
        assertFalse(script.contains(".np-tech{display:none"));
    }

    @Test public void hidingQueueAndTechnicalInformationRemovesEmptyMetadataRow() {
        String script = NowPlayingDisplayOptions.script(1, true, false, false);
        assertTrue(script.contains("var techWanted='false';"));
        assertTrue(script.contains(".np-tech{display:none!important;}"));
        assertFalse(script.contains(".np-controls #pos-slider{display:none"));
    }

    @Test public void hidingQueuePreservesTechnicalInformationIfSelected() {
        String script = NowPlayingDisplayOptions.script(0, true, true, false);
        assertTrue(script.contains("var techWanted='true';"));
        assertTrue(script.contains(".np-tech{font-size:0!important;}"));
        assertTrue(script.contains(".np-tech obj{font-size:.85rem!important;}"));
        assertFalse(script.contains(".np-tech{display:none!important;}"));
    }

    @Test public void changingColorOrMaterialTechnicalPreferenceTriggersSingleReload() {
        String script = NowPlayingDisplayOptions.script(2, true, false, true);
        assertTrue(script.contains("var expected='2';"));
        assertTrue(script.contains("localStorage.setItem(colorKey,expected);changed=true;"));
        assertTrue(script.contains("localStorage.setItem(techKey,techWanted);changed=true;"));
        assertTrue(script.contains("if(changed){location.reload();return 'reload';}"));
        assertTrue(script.contains("style.id='lyrion-display-options'"));
        assertTrue(script.contains("return 'ready';"));
    }
}
