param(
    [Parameter(Mandatory)]
    [string]$JdbcDriver
)

$ErrorActionPreference = "Stop"
$driver = (Resolve-Path -LiteralPath $JdbcDriver).Path
$output = Join-Path $PSScriptRoot "target/classes"
$sources = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot "src/main/java") -Recurse -Filter "*.java" |
    ForEach-Object FullName)
New-Item -ItemType Directory -Path $output -Force | Out-Null

& javac -encoding UTF-8 -cp $driver -d $output @sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& java -cp "$output;$driver" termproject.Main
exit $LASTEXITCODE
