$conn = New-Object System.Data.SqlClient.SqlConnection("Server=.;Database=master;Integrated Security=True")
try {
    $conn.Open()
    $cmd = $conn.CreateCommand()
    $cmd.CommandText = "RESTORE HEADERONLY FROM DISK='d:\quanly\QuanLyDaiLy.bak'"
    $reader = $cmd.ExecuteReader()
    if ($reader.Read()) {
        for ($i = 0; $i -lt $reader.FieldCount; $i++) {
            Write-Output "$($reader.GetName($i)): $($reader.GetValue($i))"
        }
    }
    $reader.Close()
} catch {
    Write-Error $_.Exception.Message
} finally {
    $conn.Close()
}
