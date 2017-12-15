# Execution: powershell -noexit -ExecutionPolicy ByPass -File .\retrieve_test_resources.ps1
$url_unitsb = "https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsb.xml"
$output_unitsb = "$PSScriptRoot\unitconstantsb.xml"
Invoke-WebRequest -Uri $url_unitsb -OutFile $output_unitsb

$url_unitsc = "https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsc.xml"
$output_unitsc = "$PSScriptRoot\unitconstantsc.xml"
Invoke-WebRequest -Uri $url_unitsc -OutFile $output_unitsc

# Move block
mv .\unitconstantsb.xml ..\..\sdk\sdk-view\src\test\resources
mv .\unitconstantsb.xml ..\..\sdk\sdk-view\src\test\resources
