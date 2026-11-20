package com.chrober.lmsmaterial.nowplaying;

/**
 * Per-display customization of Material Skin's existing Now Playing UI.
 *
 * Material Skin's /np-only endpoint redirects to the ordinary application and
 * injects np-only.css. The technical-info preference belongs to Material's
 * origin-local settings; the other two toggles require narrowly scoped CSS.
 *
 * No changes are made to the Lyrion server or any other Material Skin client.
 */
final class NowPlayingDisplayOptions {
    private NowPlayingDisplayOptions() { }

    static String script(int colorUsage, boolean showProgress,
            boolean showTechnicalInfo, boolean showPlaylistPosition) {
        StringBuilder css = new StringBuilder();
        if (!showProgress) {
            // Hide both elapsed/remaining times and the progress slider in
            // expanded wide and regular layouts. Never hide the entire
            // controls container: it also contains track/queue information.
            css.append(".np-controls-wide .np-time-layout,")
               .append(".np-controls .np-time-layout,")
               .append(".np-controls .np-pos,")
               .append(".np-controls .np-duration,")
               .append(".np-controls #pos-slider{display:none!important;}");
        }
        if (!showPlaylistPosition) {
            if (showTechnicalInfo) {
                // Material renders the technical <obj> and the queue position
                // as separate children of the SAME .np-tech row: the latter
                // is a bare text node. CSS cannot target a bare text node, so
                // suppress inherited text, then restore text within <obj>.
                css.append(".np-tech{font-size:0!important;}")
                   .append(".np-tech obj{font-size:.85rem!important;}");
            } else {
                css.append(".np-tech{display:none!important;}");
            }
        }

        return "(function(){try{" +
                "if(!document.getElementById('app'))return 'missing';" +
                "var colorKey='lms-material::colorUsage';" +
                "var expected='" + colorUsage + "';" +
                "var techKey='lms-material::techInfo';" +
                "var techWanted='" + showTechnicalInfo + "';" +
                "var changed=false;" +
                "if(localStorage.getItem(colorKey)!==expected){" +
                "localStorage.setItem(colorKey,expected);changed=true;}" +
                "if(localStorage.getItem(techKey)!==techWanted){" +
                "localStorage.setItem(techKey,techWanted);changed=true;}" +
                "if(changed){location.reload();return 'reload';}" +
                "var style=document.getElementById('lyrion-display-options');" +
                "if(!style){style=document.createElement('style');" +
                "style.id='lyrion-display-options';document.head.appendChild(style);}" +
                "style.textContent='" + css + "';" +
                "return 'ready';}catch(e){return 'missing';}})()";
    }
}
