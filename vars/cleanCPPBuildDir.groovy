def call(String outputFolderPath, String dest = "package", boolean isDebug = false) {
    echo "Cleaning up files from $outputFolderPath and placing a cleaned version in $dest"
    
    if (isUnix()) {
        sh """
            rm -rf "${dest}"
            mkdir -p "${dest}"
            cp -r "${outputFolderPath}/." "${dest}"
	    rm ${dest}/*.cc
            rm ${dest}/*.pb.h
	    rm ${dest}/auth.json
	    rm ${dest}/.ninja_deps
	    rm ${dest}/.ninja_log
	    rm ${dest}/build.ninja
	    rm -rf ${dest}/CMakeFiles
            rm ${dest}/*.log
            rm -rf ${dest}/vcpkg_installed
            rm ${dest}/compile_commands.json
 	    rm ${dest}/CMakeCache.txt
            (
		cd ${dest}
		find . -type d -name CMakeFiles -print0 |
		while IFS= read -r -d '' dir; do
			parent=\"\$(dirname \"$dir\")\"
			rm -rf $parent/CMakeFiles
			rm $parent/*.cmake
			rm $parent/*.json
		done
	    )
        """
    } else {
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
