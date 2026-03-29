def call(String outputFolderPath, String dest, boolean isDebug){
	echo "Cleaning up files from $outputFolderPath and placing a cleaned version in $dest"
	echo isDebug ? "Leaving debug files in place" : "removing debug files"
}
