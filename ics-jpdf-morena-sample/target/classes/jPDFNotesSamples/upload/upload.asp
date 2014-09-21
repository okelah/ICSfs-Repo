<!-- #include file="uploadlib.asp" -->
<%

' Create the FileUploader
Dim Uploader, File
Set Uploader = New FileUploader

' This starts the upload process
Uploader.Upload()

Response.Write "<b>Thank you for your upload " & Uploader.Form("fullname") & "</b><br>"

' Check if any files were uploaded
If Uploader.Files.Count = 0 Then
	Response.Write "File(s) not uploaded."
Else
	' Loop through the uploaded files
	For Each File In Uploader.Files.Items
		' Save the file
		File.SaveToDisk "c:\temp\"
		
		' Output the file details to the browser
		Response.Write "File Uploaded: " & File.FileName & "<br>"
		Response.Write "Size: " & File.FileSize & " bytes<br>"
		Response.Write "Type: " & File.ContentType & "<br><br>"
	Next
End If


%>
