#!/usr/bin/env python3
"""Recreate classic PNG launcher icons and WebP adaptive/TV resources.

Requires Pillow; images already checked into app/src/main/res so running the
script is optional for consumers who only need to compile the application.
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
ART = ROOT / "artwork"
icon = Image.open(ART / "lyrion-now-playing-icon-source.webp").convert("RGBA")
assert icon.getpixel((0, 0))[3] == 0, "Launcher artwork must have transparent outside corners"

sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
for density, size in sizes.items():
    target = RES / ("mipmap-" + density)
    target.mkdir(parents=True, exist_ok=True)
    icon.resize((size, size), Image.Resampling.LANCZOS).save(
        target / "ic_launcher.png", format="PNG", optimize=True
    )
    stale_webp = target / "ic_launcher.webp"
    if stale_webp.exists():
        stale_webp.unlink()

adaptive = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
adaptive.alpha_composite(icon.resize((288, 288), Image.Resampling.LANCZOS), (72, 72))
target = RES / "drawable-nodpi"
target.mkdir(parents=True, exist_ok=True)
adaptive.save(target / "ic_launcher_foreground.webp", format="WEBP", quality=92, method=6)

banner = Image.open(ART / "lyrion-now-playing-banner-source.webp").convert("RGB")
assert banner.size == (640, 360), "The TV banner source must be 640x360"
banner.save(target / "tv_banner.webp", format="WEBP", quality=90, method=6)
print("Updated PNG launcher icons and WebP adaptive/TV resources")
