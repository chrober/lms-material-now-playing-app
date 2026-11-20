#!/usr/bin/env python3
"""Check classic Android launcher icons before packaging the APK.

Fire OS 6 / Android API 25 uses the legacy launcher resources. This check
ensures only PNG variants are committed and that each has the intended size.
It does not claim that PNG alone fixes Amazon Fire TV launcher caching bugs.
"""
from pathlib import Path
from struct import unpack

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"

for density, expected_size in SIZES.items():
    folder = RES / f"mipmap-{density}"
    png = folder / "ic_launcher.png"
    old_webp = folder / "ic_launcher.webp"
    assert png.is_file(), f"Missing PNG icon: {png}"
    assert not old_webp.exists(), f"Obsolete WebP icon still present: {old_webp}"
    with png.open("rb") as icon:
        header = icon.read(24)
    assert len(header) == 24 and header[:8] == PNG_SIGNATURE, f"Invalid PNG: {png}"
    assert header[12:16] == b"IHDR", f"No PNG IHDR: {png}"
    width, height = unpack(">II", header[16:24])
    assert (width, height) == (expected_size, expected_size), (
        f"Wrong icon size for {density}: {width} x {height}"
    )
    print(f"OK {png.relative_to(ROOT)}: {width} x {height}")

print("All five PNG launcher icons are valid.")
