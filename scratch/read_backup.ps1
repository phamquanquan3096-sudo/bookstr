$conn = New-Object System.Data.SqlClient.SqlConnection("Server=.;Database=master;Integrated Security=True")
try {
    $conn.Open()
    $cmd = $conn.CreateCommand()
    $cmd.CommandText = "RESTORE HEADERONLY FROM DISK='d:\quanly\QuanLyDaiLy.bak'"
    $reader = $cmd.ExecuteReader()
    while ($reader.Read()) {
        Write-Output "BackupName: $($reader['BackupName']), Position: $($reader['Position']), BackupType: $($reader['BackupType'])"
    }
    $reader.Close()
} catch {
    Write-Error $_.Exception.Message
} finally {
    $conn.Close()
}
