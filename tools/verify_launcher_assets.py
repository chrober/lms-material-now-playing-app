#!/usr/bin/env python3
"""Check classic Android launcher icons before packaging the APK.

Fire OS 6 / Android API 25 uses the legacy launcher resources. This check
ensures only PNG variants are committed and that each has the intended size.
It does not claim that PNG alone fixes Amazon Fire TV launcher caching bugs.
"""
from pathlib import Path
from struct import unpack
from sys import argv
from zipfile import ZipFile

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

if len(argv) > 1:
    assert len(argv) == 2, "Usage: verify_launcher_assets.py [APK_FILE]"
    apk = Path(argv[1])
    assert apk.is_file(), f"APK missing: {apk}"
    with ZipFile(apk) as package:
        names = package.namelist()
        for density in SIZES:
            prefix = f"res/mipmap-{density}"
            expected = [
                n for n in names if n.startswith(prefix)
                and n.endswith("/ic_launcher.png")
            ]
            assert expected, f"APK missing PNG launcher icon: {density}"
            invalid = [
                n for n in names if n.startswith(prefix)
                and n.endswith("/ic_launcher.webp")
            ]
            assert not invalid, f"APK has obsolete WebP launcher icon: {invalid}"
            print(f"APK OK: {expected[0]}")
    print(f"APK launcher icons verified: {apk}")
