def call(String outputFolderPath, String dest = "package", boolean isDebug = false) {
    echo "Cleaning up files from $outputFolderPath and placing a cleaned version in $dest"
    if (isUnix()) {
    sh """
        # 1. Clear and prep destination
        rm -rf "${dest}"
        mkdir -p "${dest}"
        
        # 2. Copy contents (/. ensures hidden files are included)
        cp -r "${outputFolderPath}/." "${dest}"
        
        # 3. Clean root files - use -f so it doesn't fail if a file is missing
        rm -f "${dest}"/*.cc
        rm -f "${dest}"/*.pb.h
        rm -f "${dest}"/auth.json
        rm -f "${dest}"/.ninja_deps
        rm -f "${dest}"/.ninja_log
        rm -f "${dest}"/build.ninja
        rm -rf "${dest}"/CMakeFiles
        rm -f "${dest}"/*.log
        rm -rf "${dest}"/vcpkg_installed
        rm -f "${dest}"/compile_commands.json
        rm -f "${dest}"/CMakeCache.txt
        
        # 4. Deep clean subdirectories
        # We escape the $ with \\$ so Groovy ignores it and Shell uses it
        (
            cd "${dest}" || exit 1
            find . -type d -name "CMakeFiles" -print0 | while IFS= read -r -d '' dir; do
                parent=\$(dirname "\$dir")
                rm -rf "\$dir"
                rm -f "\$parent"/*.cmake
                rm -f "\$parent"/*.json
            done
        )
    """
}
    else {
        pwsh """
            \$destPath = "${dest}"
            \$isDebug = ${isDebug ? '$true' : '$false'}

            if (Test-Path \$destPath) { Remove-Item -Recurse -Force \$destPath }
            New-Item -ItemType Directory -Force -Path \$destPath | Out-Null
            Copy-Item -Recurse -Force "${outputFolderPath}/*" "\$destPath/"

            \$targets = @("*.h", "*.cc", "*.cmake", "*.log", "auth.json", "build.ninja", "*.ilk", "compile_commands.json", "CMakeCache.txt", ".ninja_deps", ".ninja_log")
            if (-not \$isDebug) { \$targets += "*.pdb" }

            Get-ChildItem -Path \$destPath -Include \$targets -Recurse | Remove-Item -Force -ErrorAction SilentlyContinue
            Get-ChildItem -Path \$destPath -Directory -Filter "CMakeFiles" -Recurse | Remove-Item -Recurse -Force
            Get-ChildItem -Path \$destPath -Directory -Filter ".cmake" -Recurse | Remove-Item -Recurse -Force
            Get-ChildItem -Path \$destPath -Directory -Filter "vcpkg_installed" -Recurse | Remove-Item -Recurse -Force
	    Get-ChildItem -Path \$destPath\\tests -File -Filter "cmake_test_discovery*.json" | Remove-Item
        """
    }
}
