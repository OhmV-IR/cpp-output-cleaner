def call(String outputFolderPath, String dest = "package", boolean isDebug = false) {
    echo "Cleaning up files from $outputFolderPath and placing a cleaned version in $dest"
    
    if (isUnix()) {
        sh """
            rm -rf "${dest}"
            mkdir -p "${dest}"
            cp -r "${outputFolderPath}/." "${dest}"
            find "${dest}" -name "*.h" -o -name "*.cc" -o -name "*.cmake" -o -name "*.log" -o -name "build.ninja" -o -name "compile_commands.json" -o -name "CMakeCache.txt" -delete
            find "${dest}" -type d -name "CMakeFiles" -exec rm -rf {} +
            find "${dest}" -type d -name ".cmake" -exec rm -rf {} +
            find "${dest}" -type d -name "vcpkg_installed" -exec rm -rf {} +
        """
    } else {
        pwsh """
            \$destPath = "${dest}"
            \$isDebug = ${isDebug ? '$true' : '$false'}

            if (Test-Path \$destPath) { Remove-Item -Recurse -Force \$destPath }
            New-Item -ItemType Directory -Force -Path \$destPath | Out-Null
            Copy-Item -Recurse -Force "${outputFolderPath}/*" "\$destPath/"

            \$targets = @("*.h", "*.cc", "*.cmake", "*.log", "auth.json", "build.ninja", "*.ilk", "compile_commands.json", "CMakeCache.txt")
            if (-not \$isDebug) { \$targets += "*.pdb" }

            Get-ChildItem -Path \$destPath -Include \$targets -Recurse | Remove-Item -Force -ErrorAction SilentlyContinue
            Get-ChildItem -Path \$destPath -Directory -Filter "CMakeFiles" -Recurse | Remove-Item -Recurse -Force
            Get-ChildItem -Path \$destPath -Directory -Filter ".cmake" -Recurse | Remove-Item -Recurse -Force
            Get-ChildItem -Path \$destPath -Directory -Filter "vcpkg_installed" -Recurse | Remove-Item -Recurse -Force
        """
    }
}
