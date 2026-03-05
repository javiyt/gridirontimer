#!/usr/bin/env bash
set -euo pipefail

BUILD_FILE="app/build.gradle.kts"

usage() {
  cat <<'EOF'
Usage:
  scripts/create_release.sh [X.Y.Z]
  scripts/create_release.sh [--patch | --minor | --major]

Behavior:
  - If version is provided, uses it.
  - If version is omitted, reads versionName from app/build.gradle.kts and increments:
      --patch (default): X.Y.Z -> X.Y.(Z+1)
      --minor:           X.Y.Z -> X.(Y+1).0
      --major:           X.Y.Z -> (X+1).0.0
  - Creates branch: release_X.Y.Z
  - Updates versionName and versionCode (+1) in app/build.gradle.kts
  - Creates commit: "Release X.Y.Z"
  - Pushes branch and creates PR with same title via gh CLI
EOF
}

require_cmd() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Error: required command not found: $cmd" >&2
    exit 1
  fi
}

validate_semver() {
  local version="$1"
  [[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]
}

get_current_version() {
  sed -nE 's/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' "$BUILD_FILE" | head -n1
}

get_current_version_code() {
  sed -nE 's/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*([0-9]+).*/\1/p' "$BUILD_FILE" | head -n1
}

increment_patch() {
  local current="$1"
  local major minor patch
  IFS='.' read -r major minor patch <<<"$current"
  echo "${major}.${minor}.$((patch + 1))"
}

increment_minor() {
  local current="$1"
  local major minor patch
  IFS='.' read -r major minor patch <<<"$current"
  echo "${major}.$((minor + 1)).0"
}

increment_major() {
  local current="$1"
  local major minor patch
  IFS='.' read -r major minor patch <<<"$current"
  echo "$((major + 1)).0.0"
}

default_base_branch() {
  if git symbolic-ref refs/remotes/origin/HEAD >/dev/null 2>&1; then
    git symbolic-ref refs/remotes/origin/HEAD | sed 's@^refs/remotes/origin/@@'
  else
    echo "main"
  fi
}

update_build_file() {
  local new_version="$1"
  local new_code="$2"
  local tmp_file
  tmp_file="$(mktemp)"

  awk -v new_version="$new_version" -v new_code="$new_code" '
    BEGIN { vn=0; vc=0 }
    {
      line=$0
      if (vn == 0 && line ~ /versionName[[:space:]]*=/) {
        sub(/"[^"]+"/, "\"" new_version "\"", line)
        vn=1
      }
      if (vc == 0 && line ~ /versionCode[[:space:]]*=/) {
        sub(/=[[:space:]]*[0-9]+/, "= " new_code, line)
        vc=1
      }
      print line
    }
    END {
      if (vn == 0 || vc == 0) {
        exit 2
      }
    }
  ' "$BUILD_FILE" >"$tmp_file"

  mv "$tmp_file" "$BUILD_FILE"
}

main() {
  require_cmd git
  require_cmd gh
  require_cmd awk
  require_cmd sed
  require_cmd mktemp

  if [[ ! -f "$BUILD_FILE" ]]; then
    echo "Error: build file not found: $BUILD_FILE" >&2
    exit 1
  fi

  local bump_type="patch"
  local provided_version=""
  local flag_count=0

  while [[ $# -gt 0 ]]; do
    case "$1" in
      -h|--help)
        usage
        exit 0
        ;;
      --patch)
        bump_type="patch"
        flag_count=$((flag_count + 1))
        shift
        ;;
      --minor)
        bump_type="minor"
        flag_count=$((flag_count + 1))
        shift
        ;;
      --major)
        bump_type="major"
        flag_count=$((flag_count + 1))
        shift
        ;;
      *)
        if [[ -n "$provided_version" ]]; then
          echo "Error: only one version argument is allowed." >&2
          usage
          exit 1
        fi
        provided_version="$1"
        shift
        ;;
    esac
  done

  if [[ "$flag_count" -gt 1 ]]; then
    echo "Error: use only one bump flag among --patch, --minor, --major." >&2
    exit 1
  fi

  if [[ -n "$provided_version" && "$flag_count" -gt 0 ]]; then
    echo "Error: do not combine explicit version with bump flags." >&2
    exit 1
  fi

  local current_version current_code target_version new_code
  current_version="$(get_current_version)"
  current_code="$(get_current_version_code)"

  if [[ -z "$current_version" || -z "$current_code" ]]; then
    echo "Error: could not parse versionName/versionCode from $BUILD_FILE" >&2
    exit 1
  fi

  if ! validate_semver "$current_version"; then
    echo "Error: current versionName is not semver (X.Y.Z): $current_version" >&2
    exit 1
  fi

  if [[ -n "$provided_version" ]]; then
    target_version="${provided_version#v}"
    if ! validate_semver "$target_version"; then
      echo "Error: provided version must be semver X.Y.Z, got: $provided_version" >&2
      exit 1
    fi
  else
    case "$bump_type" in
      patch) target_version="$(increment_patch "$current_version")" ;;
      minor) target_version="$(increment_minor "$current_version")" ;;
      major) target_version="$(increment_major "$current_version")" ;;
      *)
        echo "Error: unsupported bump type: $bump_type" >&2
        exit 1
        ;;
    esac
  fi

  new_code=$((current_code + 1))

  local branch_name commit_title base_branch
  branch_name="release_${target_version}"
  commit_title="Release ${target_version}"
  base_branch="$(default_base_branch)"

  if git rev-parse --verify "$branch_name" >/dev/null 2>&1; then
    echo "Error: local branch already exists: $branch_name" >&2
    exit 1
  fi

  if git ls-remote --exit-code --heads origin "$branch_name" >/dev/null 2>&1; then
    echo "Error: remote branch already exists: origin/$branch_name" >&2
    exit 1
  fi

  git checkout -b "$branch_name"
  update_build_file "$target_version" "$new_code"

  git add "$BUILD_FILE"
  git commit -m "$commit_title"
  git push -u origin "$branch_name"
  gh pr create --base "$base_branch" --head "$branch_name" --title "$commit_title" --body "$commit_title"

  echo "Done:"
  echo "  Branch: $branch_name"
  echo "  Version: $target_version"
  echo "  versionCode: $new_code"
  echo "  PR title: $commit_title"
}

main "$@"
