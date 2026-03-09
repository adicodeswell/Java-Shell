#!/bin/bash
set -e

echo "📦 Building mini-shell with Maven..."
mvn clean package -DskipTests

INSTALL_DIR="$HOME/.local/lib/mini-shell"
BIN_DIR="$HOME/.local/bin"

echo "📂 Creating directories..."
mkdir -p "$INSTALL_DIR"
mkdir -p "$BIN_DIR"

echo "🚚 Copying executable..."
cp target/mini-shell.jar "$INSTALL_DIR/"

echo "📝 Creating wrapper script..."
cat << 'EOF' > "$BIN_DIR/mini-shell"
#!/bin/bash
exec java --enable-preview -jar "$HOME/.local/lib/mini-shell/mini-shell.jar" "$@"
EOF

chmod +x "$BIN_DIR/mini-shell"

echo "✅ Installation complete!"
echo "You can now run 'mini-shell' from anywhere."
echo "If command is not found, ensure $BIN_DIR is in your PATH."
