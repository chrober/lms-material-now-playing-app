package com.chrober.lmsmaterial.nowplaying;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

/** Compatibility checks against Material Skin's theme IDs and color catalog. */
public class MaterialThemeTest {
    @Test public void originalThemeSelectionHasNoExtraDefaultEntry() {
        List<MaterialTheme.Option> themes = MaterialTheme.builtInThemes();
        assertEquals(5, themes.size());
        assertEquals("auto", themes.get(0).key);
        assertEquals("light", themes.get(1).key);
        assertEquals("dark", themes.get(2).key);
        assertEquals("black", themes.get(3).key);
        assertEquals("dark-lyrion", themes.get(4).key);
    }

    @Test public void automaticThemeUsesSystemDarkMode() {
        assertEquals("light", MaterialTheme.effectiveTheme("auto", false));
        assertEquals("dark", MaterialTheme.effectiveTheme("auto", true));
        assertEquals("black", MaterialTheme.effectiveTheme("black", false));
    }

    @Test public void userThemesRemainSelectableWithoutAddingDuplicates() {
        List<MaterialTheme.Option> result = MaterialTheme.mergedThemes(Arrays.asList(
                new MaterialTheme.Option("user:dark/Midnight", "Midnight"),
                new MaterialTheme.Option("dark", "Repeated dark")));
        assertEquals(6, result.size());
        assertEquals("user:dark/Midnight", result.get(5).key);
    }

    @Test public void rejectsUnsafeThemeAndColorIdentifiers() {
        assertTrue(MaterialTheme.safeKey("user:dark/Midnight"));
        assertTrue(MaterialTheme.safeKey("dark-lyrion"));
        assertFalse(MaterialTheme.safeKey("../foo"));
        assertFalse(MaterialTheme.safeKey("x&foo=bar"));
        assertFalse(MaterialTheme.safeKey("x?q"));
    }

    @Test public void themeUrlUsesSelectedThemeWithoutToolbarColorSuffix() {
        assertEquals("dark-lyrion", MaterialTheme.urlTheme("dark-lyrion"));
        assertEquals("auto", MaterialTheme.urlTheme("auto"));
        assertEquals("dark", MaterialTheme.urlTheme("../unsafe"));
    }

    @Test public void nativePaletteMatchesOriginalThemeColors() {
        assertEquals(0xfffafafa, MaterialTheme.palette("light", "blue", 0, false).background);
        assertEquals(0xff212121, MaterialTheme.palette("dark", "blue", 0, true).background);
        assertEquals(0xff000000, MaterialTheme.palette("black", "blue", 0, true).background);
        assertEquals(0xff081f22, MaterialTheme.palette("dark-lyrion", "lyrion", 0, true).background);
        assertEquals(0xffe3fdc4, MaterialTheme.palette("dark-lyrion", "lyrion", 0, true).accent);
    }

    @Test public void builtInColorKeysMatchMaterialSkinCatalog() {
        assertEquals(19, MaterialTheme.builtInColors().size());
        assertEquals("red", MaterialTheme.builtInColors().get(0).key);
        assertEquals("lyrion", MaterialTheme.builtInColors().get(18).key);
        assertEquals(0xffd84141, MaterialTheme.palette("dark", "red", 0, true).accent);
        assertEquals(0xffc62828, MaterialTheme.palette("light", "red", 0, false).accent);
    }
}
