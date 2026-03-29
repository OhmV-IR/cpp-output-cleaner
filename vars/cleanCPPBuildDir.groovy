def call(String outputFolderPath, String dest = "package", boolean isDebug = false){
  	echo "Cleaning up files from $outputFolderPath and placing a cleaned version in $dest"
    if(isDebug == true){
	    echo "Leaving debug files in place"
    } else {
      echo "removing debug files"
    }
	if(isUnix()){
		sh """
			rm -rf ${dest}
			mkdir -p ${dest}
          		cp -r ${outputFolderPath} ${dest}
          		rm -f ${dest}/*.h
          		rm -f ${dest}/*.cc
          		rm -rf ${dest}/.cmake
          		rm -f ${dest}/*.cmake
          		rm -rf ${dest}/CMakeFiles
              rm -f ${dest}/*.log
              rm -rf ${dest}/vcpkg_installed
              rm -f ${dest}/build.ninja
              rm -f ${dest}/compile_commands.json
              rm -f ${dest}/CMakeCache.txt
              rm -rf ${dest}/CMakeFiles
              (
                cd ${dest}
                find . -type d -name CMakeFiles -print0 |
                while IFS= read -r -d '' dir; do
                  parent="$(dirname "\$dir")"
                  echo "Processing: \$parent"
                  rm -rf \$parent/CMakeFiles
                  rm -f \$parent/*.cmake
                  rm -f \$parent/*.json
                done
              )
	  """
  } else {
    pwsh """
            New-Item -ItemType Directory -Force -Path "${dest}" | Out-Null
    
            Copy-Item -Recurse -Force "${outputFolderPath}/*" "${dest}/"
    
            Remove-Item -Force -ErrorAction SilentlyContinue `
            ${dest}\\*.h,
            ${dest}\\*.cc,
            ${dest}\\*.cmake,
            ${dest}\\*.log,
            ${dest}\\auth.json,
            ${dest}\\build.ninja,
            ${dest}\\*.ilk,
            ${dest}\\compile_commands.json,
            ${dest}\\CMakeCache.txt


            ${isDebug == false ? "Remove-Item -Force -ErrorAction SilentlyContinue ${dest}\\*.pdb," : ""}
    
            Remove-Item -Recurse -Force -ErrorAction SilentlyContinue `
            ${dest}\\.cmake,
            ${dest}\\CMakeFiles,
            ${dest}\\vcpkg_installed
    
            Get-ChildItem -Recurse -Directory -Filter "CMakeFiles" -Path "${dest}" | ForEach-Object {
              \$parent = \$_.Parent.FullName
              Write-Host "Processing: $parent"
              Remove-Item -Recurse -Force -ErrorAction SilentlyContinue \$_.FullName
              Remove-Item -Force -ErrorAction SilentlyContinue `
              (Join-Path \$parent "*.cmake"),
              (Join-Path \$parent "*.ilk"),
              (Join-Path \$parent "*.json")
              ${isDebug == false ? "Remove-Item -Force -ErrorAction SilentlyContinue (Join-Path \$parent "*.pdb") : "" }
            }
    """
  }
}
