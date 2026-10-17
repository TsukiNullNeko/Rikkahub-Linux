#!/usr/bin/env bash
# Build a .deb from the Gradle installDist output (launcher script + all jars).
#
# Usage: tools/make_deb.sh [version]
set -euo pipefail

VERSION="${1:-1.0.1}"
REPO="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO"

DIST="desktop/build/install/rikkahub"
if [ ! -d "$DIST" ]; then
    echo "missing $DIST - run :desktop:installDist first" >&2
    exit 1
fi

S=build/deb-stage
rm -rf "$S" "build/rikkahub_${VERSION}_amd64.deb"
mkdir -p "$S/DEBIAN" "$S/usr/bin" "$S/opt/rikkahub" \
         "$S/usr/share/applications" "$S/usr/share/icons/hicolor/256x256/apps"

cp -r "$DIST"/* "$S/opt/rikkahub/"

ICON="$(find app/src/main/res -name ic_launcher.png | head -1)"
cp "$ICON" "$S/usr/share/icons/hicolor/256x256/apps/rikkahub.png"
cp "$ICON" "$S/opt/rikkahub/rikkahub.png"

cat > "$S/usr/bin/rikkahub" <<'EOS'
#!/bin/sh
exec /opt/rikkahub/bin/rikkahub "$@"
EOS
chmod 755 "$S/usr/bin/rikkahub"

cat > "$S/usr/share/applications/rikkahub.desktop" <<'EOS'
[Desktop Entry]
Type=Application
Name=RikkaHub
Comment=RikkaHub - native Linux LLM chat client
Exec=/usr/bin/rikkahub
Icon=rikkahub
Terminal=false
Categories=Utility;Network;Chat;
EOS

SIZE="$(du -sk "$S/opt" | cut -f1)"
cat > "$S/DEBIAN/control" <<EOS
Package: rikkahub
Version: ${VERSION}
Section: utils
Priority: optional
Architecture: amd64
Maintainer: yuexusi <xusi444@qq.com>
Installed-Size: ${SIZE}
Depends: libc6 (>= 2.35), openjdk-21-jre-headless | default-jre-headless | java21-runtime-headless, libx11-6, libxext6, libxi6, libxrender1, libxtst6, libfreetype6, libfontconfig1
Homepage: https://github.com/yuexusi/Rikkahub-Linux
Description: RikkaHub - native Linux LLM chat client
 A native Linux port of RikkaHub, an Android LLM chat client,
 built with Compose Multiplatform and Material 3 Expressive.
EOS

dpkg-deb --build --root-owner-group "$S" "build/rikkahub_${VERSION}_amd64.deb"
echo "built: build/rikkahub_${VERSION}_amd64.deb"
