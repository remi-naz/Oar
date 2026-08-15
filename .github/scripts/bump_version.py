"""Bump versionCode/versionName for the 'production' flavor in app/build.gradle.kts."""
import os
import re
import sys

BUILD_FILE = "app/build.gradle.kts"


def main() -> None:
    if len(sys.argv) != 2 or sys.argv[1] not in ("major", "minor", "patch"):
        print("Usage: bump_version.py <major|minor|patch>", file=sys.stderr)
        sys.exit(1)
    bump_type = sys.argv[1]

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

    major, minor, patch = (int(part) for part in current_name.split("."))
    if bump_type == "major":
        major, minor, patch = major + 1, 0, 0
    elif bump_type == "minor":
        minor, patch = minor + 1, 0
    else:
        patch += 1

    new_name = f"{major}.{minor}.{patch}"
    new_code = current_code + 1

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
