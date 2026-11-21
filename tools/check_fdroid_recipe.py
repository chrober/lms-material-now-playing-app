#!/usr/bin/env python3
"""Fail-closed checks for the *disabled* upstream F-Droid packaging draft.

No publication, GitLab API access, secrets or network access is performed.
Source SHA and version are checked against git history in a complete clone.
"""
from __future__ import annotations

import argparse
import pathlib
import re
import subprocess
import sys

import yaml

APP_ID = "com.chrober.lmsmaterial.nowplaying"
EXPECTED_SOURCE = "https://github.com/chrober/lyrion-display"


def git(*args: str) -> str:
    return subprocess.check_output(["git", *args], text=True).strip()


def gradle_value(script: str, key: str) -> str:
    match = re.search(r"^\s*" + re.escape(key) + r"\s+['\"]?([^'\s\"]+)", script, re.M)
    if not match:
        raise ValueError(f"Missing {key} in app/build.gradle")
    return match.group(1)


def check(path: pathlib.Path) -> None:
    obj = yaml.safe_load(path.read_text(encoding="utf8"))
    assert isinstance(obj, dict), "Root must be a YAML mapping"
    assert obj.get("License") == "MIT", "Unexpected license"
    assert obj.get("SourceCode") == EXPECTED_SOURCE, "Unexpected upstream source"
    assert obj.get("Repo") == EXPECTED_SOURCE + ".git", "Unexpected clone URL"
    assert obj.get("RepoType") == "git", "Repository must be git"
    assert obj.get("Categories") == ["Multimedia"]
    assert obj.get("AutoUpdateMode") == "None", "Auto-update must stay disabled"
    assert obj.get("UpdateCheckMode") == "Static", "Update checks must stay manual"
    builds = obj.get("Builds")
    assert isinstance(builds, list) and len(builds) == 1, "Expected one disabled test build"
    build = builds[0]
    assert build.get("disable"), "Draft source build MUST remain disabled"
    assert build.get("gradle") == ["yes"], "Unexpected Gradle build variant"
    assert build.get("subdir") == "app", "Unexpected module"
    sha = build.get("commit")
    assert isinstance(sha, str) and re.fullmatch("[0-9a-f]{40}", sha), "Pin exact SHA"
    script = git("show", sha + ":app/build.gradle")
    assert gradle_value(script, "applicationId") == APP_ID, "Application ID mismatch"
    version = gradle_value(script, "versionName")
    code = int(gradle_value(script, "versionCode"))
    assert version == str(build["versionName"]), "Version name differs at source SHA"
    assert code == int(build["versionCode"]), "Version code differs at source SHA"
    assert version == str(obj["CurrentVersion"])
    assert code == int(obj["CurrentVersionCode"])
    # The reproducibility recipe must pin the *exact published release tag*,
    # never an unreleased QA snapshot or an unrelated branch revision.
    tagged_sha = git("rev-list", "-n", "1", "refs/tags/v" + version)
    assert sha == tagged_sha, "Recipe source does not match the published release tag"
    pinned_wrapper = git("show", sha + ":gradle/wrapper/gradle-wrapper.properties")
    assert "gradle-9.9.1-bin.zip" in pinned_wrapper, (
        "Pinned source commit must contain the Gradle version selection")
    assert script.find("if (System.getenv('ANDROID_KEYSTORE_PATH'))") >= 0, (
        "Source must permit unsigned release APK builds")
    wrapper = pathlib.Path("gradle/wrapper/gradle-wrapper.properties").read_text()
    assert "gradle-9.9.1-bin.zip" in wrapper, "Gradle version is not pinned"
    print(f"PASS: disabled recipe {APP_ID} {version} ({code}), source {sha}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--metadata", type=pathlib.Path,
                        default=pathlib.Path("fdroid/metadata") / (APP_ID + ".yml"))
    args = parser.parse_args()
    try:
        check(args.metadata)
    except (AssertionError, ValueError, subprocess.CalledProcessError, FileNotFoundError) as exc:
        print(f"F-Droid recipe validation failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
