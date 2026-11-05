"""Bump versionCode/versionName for the 'production' flavor in app/build.gradle.kts.

Usage: bump_version.py <major|minor|patch|build> [prerelease-prefix]

- major/minor/patch bump the semver core; 'build' keeps it (code-only bump).
- Any existing prerelease suffix on the current versionName is stripped first.
- With a prefix (e.g. 'beta') the new versionName becomes <core>-<prefix>.<N>, where N
  is one past the highest existing git tag v<core>-<prefix>.<n>.
- Without a prefix the result is a clean <core> (this is how a beta is finalized).
- versionCode always increases by one.
"""
import os
import re
import subprocess
import sys

BUILD_FILE = "app/build.gradle.kts"
BUMP_TYPES = ("major", "minor", "patch", "build")
VERSION_RE = re.compile(r"^(\d+)\.(\d+)\.(\d+)(?:-[0-9A-Za-z.]+)?$")


def next_prerelease_number(core: str, prefix: str) -> int:
    tags = subprocess.run(
        ["git", "tag", "-l", f"v{core}-{prefix}.*"],
        capture_output=True, text=True, check=True,
    ).stdout.split()
    numbers = []
    for tag in tags:
        m = re.fullmatch(rf"v{re.escape(core)}-{re.escape(prefix)}\.(\d+)", tag)
        if m:
            numbers.append(int(m.group(1)))
    return max(numbers, default=0) + 1


def tag_exists(tag: str) -> bool:
    return bool(subprocess.run(
        ["git", "tag", "-l", tag], capture_output=True, text=True, check=True
    ).stdout.strip())


def main() -> None:
    if len(sys.argv) not in (2, 3) or sys.argv[1] not in BUMP_TYPES:
        print("Usage: bump_version.py <major|minor|patch|build> [prerelease-prefix]", file=sys.stderr)
        sys.exit(1)
    bump_type = sys.argv[1]
    prefix = sys.argv[2] if len(sys.argv) == 3 else ""
    if prefix and not re.fullmatch(r"[A-Za-z]+", prefix):
        print("Prerelease prefix must contain letters only (e.g. 'beta')", file=sys.stderr)
        sys.exit(1)

    with open(BUILD_FILE) as f:
        content = f.read()

    match = re.search(r'create\("production"\)\s*\{([^}]*)\}', content, re.S)
    if not match:
        print(f"Could not find production flavor block in {BUILD_FILE}", file=sys.stderr)
        sys.exit(1)
    block = match.group(1)

    name_match = re.search(r'versionName\s*=\s*"([^"]+)"', block)
    code_match = re.search(r'versionCode\s*=\s*(\d+)', block)
    if not name_match or not code_match:
        print("Could not find versionName/versionCode in production flavor block", file=sys.stderr)
        sys.exit(1)

    current_name = name_match.group(1)
    current_code = int(code_match.group(1))

    version_match = VERSION_RE.match(current_name)
    if not version_match:
        print(f"Unrecognized versionName '{current_name}'", file=sys.stderr)
        sys.exit(1)
    major, minor, patch = (int(part) for part in version_match.groups())

    if bump_type == "major":
        major, minor, patch = major + 1, 0, 0
    elif bump_type == "minor":
        minor, patch = minor + 1, 0
    elif bump_type == "patch":
        patch += 1

    core = f"{major}.{minor}.{patch}"
    new_name = f"{core}-{prefix}.{next_prerelease_number(core, prefix)}" if prefix else core
    new_code = current_code + 1

    if tag_exists(f"v{new_name}"):
        print(
            f"Tag v{new_name} already exists. Pick a different bump type "
            f"(e.g. 'patch'/'minor' for a stable release) or add a prerelease prefix.",
            file=sys.stderr,
        )
        sys.exit(1)

    new_block = block.replace(f'versionName = "{current_name}"', f'versionName = "{new_name}"', 1)
    new_block = new_block.replace(f'versionCode = {current_code}', f'versionCode = {new_code}', 1)

    new_content = content[: match.start(1)] + new_block + content[match.end(1) :]
    with open(BUILD_FILE, "w") as f:
        f.write(new_content)

    print(f"Bumped version: {current_name} (code {current_code}) -> {new_name} (code {new_code})")

    github_output = os.environ.get("GITHUB_OUTPUT")
    if github_output:
        with open(github_output, "a") as f:
            f.write(f"new_version={new_name}\n")
            f.write(f"new_code={new_code}\n")


if __name__ == "__main__":
    main()
