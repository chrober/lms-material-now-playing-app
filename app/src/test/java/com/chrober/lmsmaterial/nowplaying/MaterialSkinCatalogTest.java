package com.chrober.lmsmaterial.nowplaying;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class MaterialSkinCatalogTest {
    @Test public void parsesBuiltInAndCustomThemeEntries() throws Exception {
        JSONObject result = new JSONObject("{\"themes\":["
                + "{\"key\":\"user:dark/Midnight\",\"label\":\"My-Midnight\"},"
                + "{\"key\":\"../unsafe\",\"label\":\"Unsafe\"},null],"
                + "\"colors\":["
                + "{\"key\":\"mycolor\",\"label\":\"Accent\",\"color\":\"#22aabb\"},"
                + "{\"key\":\"bad&value\",\"color\":\"#eabbcc\"},"
                + "{\"key\":\"empty\",\"color\":\"garbage\"}]}");
        MaterialSkinCatalog.Result parsed = MaterialSkinCatalog.parseCatalog(result);
        assertEquals(1, parsed.themes.size());
        assertEquals("user:dark/Midnight", parsed.themes.get(0).key);
        assertEquals("My Midnight", parsed.themes.get(0).label);
        assertEquals(1, parsed.colors.size());
        assertEquals("mycolor", parsed.colors.get(0).key);
    }

    @Test public void handlesAbsentAndIncorrectTypes() throws Exception {
        JSONObject result = new JSONObject("{\"themes\":\"not a list\",\"colors\":42}");
        MaterialSkinCatalog.Result parsed = MaterialSkinCatalog.parseCatalog(result);
        assertTrue(parsed.themes.isEmpty());
        assertTrue(parsed.colors.isEmpty());
    }

    @Test public void parsesOptionalPreferencesOnlyForValidObject() throws Exception {
        JSONObject result = new JSONObject().put("_p2", "{\"theme\":\"dark\"}");
        assertEquals("dark", MaterialSkinCatalog.parseDefaults(result).getString("theme"));
        assertNull(MaterialSkinCatalog.parseDefaults(new JSONObject().put("_p2", "")));
        assertNull(MaterialSkinCatalog.parseDefaults(new JSONObject()));
    }
}
