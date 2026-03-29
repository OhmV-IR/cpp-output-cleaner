def call(String outputFolderPath, String dest = "package", boolean isDebug = false) {
    // Inject variables into the environment to avoid Groovy interpolation drama
    withEnv([
        "DEST_DIR=${dest}",
        "SRC_DIR=${outputFolderPath}",
        "IS_DEBUG=${isDebug}"
    ]) {
        echo "Cleaning up files from ${env.SRC_DIR} and placing a cleaned version in ${env.DEST_DIR}"

        if (isUnix()) {
            sh '''
                set -e
                # 1. Clear and prep destination
                rm -rf "$DEST_DIR"
                mkdir -p "$DEST_DIR"

                # 2. Copy contents
                cp -r "$SRC_DIR/." "$DEST_DIR"

                # 3. Root Level Clean (using wildcards to handle those .pb files)
                rm -f "$DEST_DIR"/*.cc
                rm -f "$DEST_DIR"/*.h
                rm -f "$DEST_DIR"/auth.json
		rm -f "$DEST_DIR"/*.cmake
                rm -f "$DEST_DIR"/.ninja_deps
                rm -f "$DEST_DIR"/.ninja_log
                rm -f "$DEST_DIR"/build.ninja
                rm -rf "$DEST_DIR"/CMakeFiles
                rm -f "$DEST_DIR"/*.log
                rm -rf "$DEST_DIR"/vcpkg_installed
                rm -f "$DEST_DIR"/compile_commands.json
                rm -f "$DEST_DIR"/CMakeCache.txt

                # 4. Deep clean subdirectories
                # Uses POSIX-compliant find to avoid "read -d" errors on Ubuntu/Dash
                cd "$DEST_DIR"
                find . -type d -name "CMakeFiles" -exec sh -c '
                    for dir do
                        parent=$(dirname "$dir")
                        rm -rf "$dir"
                        rm -f "$parent"/*.cmake
                        rm -f "$parent"/*.json
                    done
                ' sh {} +
            '''
        } else {
            powershell '''
                $destPath = $env:DEST_DIR
                $isDebug = [System.Convert]::ToBoolean($env:IS_DEBUG)

                if (Test-Path $destPath) { Remove-Item -Recurse -Force $destPath }
                New-Item -ItemType Directory -Force -Path $destPath | Out-Null
                
                # Copying with * to ensure hidden files are included
                Copy-Item -Recurse -Force "$($env:SRC_DIR)/*" "$destPath/"

                $targets = @("*.h", "*.cc", "*.cmake", "*.log", "auth.json", "build.ninja", "*.ilk", "compile_commands.json", "CMakeCache.txt", ".ninja_deps", ".ninja_log")
                if (-not $isDebug) { $targets += "*.pdb" }

                # SilentlyContinue prevents the script from stopping if a glob finds zero files
                Get-ChildItem -Path $destPath -Include $targets -Recurse | Remove-Item -Force -ErrorAction SilentlyContinue
                Get-ChildItem -Path $destPath -Directory -Filter "CMakeFiles" -Recurse | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
                Get-ChildItem -Path $destPath -Directory -Filter ".cmake" -Recurse | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
                Get-ChildItem -Path $destPath -Directory -Filter "vcpkg_installed" -Recurse | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
                
                if (Test-Path "$destPath\\tests") {
                    Get-ChildItem -Path "$destPath\\tests" -File -Filter "cmake_test_discovery*.json" | Remove-Item -Force -ErrorAction SilentlyContinue
                }
            '''
        }
    }
}
