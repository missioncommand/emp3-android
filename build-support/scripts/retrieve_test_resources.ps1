# Execution: powershell -noexit -ExecutionPolicy ByPass -File .\retrieve_test_resources.ps1
# This script pulls xml files from the internet and places them in appropriate test resource folder.
# Takes the root path of the emp directory as a parameter
# This is done to keep our test resources up to date with the actual unit def tables the renderer will be using.
# args[0] = path of root emp dir

# Download 1.
$url_unitsb = "https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsb.xml"
$output_unitsb = "$PSScriptRoot\unitconstantsb.xml"
Invoke-WebRequest -Uri $url_unitsb -OutFile $output_unitsb

# Download 2.
$url_unitsc = "https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsc.xml"
$output_unitsc = "$PSScriptRoot\unitconstantsc.xml"
Invoke-WebRequest -Uri $url_unitsc -OutFile $output_unitsc

# Move block
$endPath = "$Args\sdk\sdk-view\src\test\resources"
mv .\unitconstantsb.xml $endPath
mv .\unitconstantsc.xml $endPath
echo "Pulling resources was successful."
