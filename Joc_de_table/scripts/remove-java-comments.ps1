# Removes Java comments from files under a directory in-place.
param(
    [string]$Path = "src/main/java"
)
function Remove-CommentsFromFile {
    param([string]$file)
    $text = Get-Content -Raw -LiteralPath $file
    $out = New-Object System.Text.StringBuilder
    $len = $text.Length
    $i = 0
    $inString = $false
    $inChar = $false
    $inLineComment = $false
    $inBlockComment = $false
    while ($i -lt $len) {
        $c = $text[$i]
        $next = if ($i+1 -lt $len) { $text[$i+1] } else { "" }
        if ($inLineComment) {
            if ($c -eq "`n") { $inLineComment = $false; $out.Append($c) | Out-Null }
            $i++
            continue
        }
        if ($inBlockComment) {
            if ($c -eq '*' -and $next -eq '/') { $inBlockComment = $false; $i += 2; continue }
            $i++
            continue
        }
        if (-not $inString -and -not $inChar -and $c -eq '/' -and $next -eq '/') { $inLineComment = $true; $i += 2; continue }
        if (-not $inString -and -not $inChar -and $c -eq '/' -and $next -eq '*') { $inBlockComment = $true; $i += 2; continue }
        if (-not $inChar -and $c -eq '"') { $out.Append($c) | Out-Null; $inString = -not $inString; $i++; continue }
        if (-not $inString -and $c -eq "'" ) { $out.Append($c) | Out-Null; $inChar = -not $inChar; $i++; continue }
        if ($c -eq '\\' -and ($inString -or $inChar)) { $out.Append($c) | Out-Null; if ($i+1 -lt $len) { $out.Append($text[$i+1]) | Out-Null; $i += 2; continue } }
        $out.Append($c) | Out-Null
        $i++
    }
    Set-Content -LiteralPath $file -Value $out.ToString()
}
Get-ChildItem -Path $Path -Recurse -Filter *.java | ForEach-Object {
    Write-Host "Processing: " $_.FullName
    Remove-CommentsFromFile -file $_.FullName
}
Write-Host "Done."

