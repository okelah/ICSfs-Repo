<?php

//Choose true or false here, to generate a success or an error message.
if ($_FILES['PDFFile']['error']  != 0)
{
	echo "ERROR: " . $_FILES['PDFFile']['error'];
	print_r (error_get_last());
}
else
{
	// Set the path to the file
	$uploaddir = '/home/users/web/b2033/sl.qoppacom/public_html/dare2b/';
	$uploadfile = $uploaddir . basename($_FILES['PDFFile']['name']);
	echo $uploadfile . "\n";
	
	// get current permissions
	$currentperms = fileperms($uploadfile);
	if ($currentperms == 0)
	{
		$currentperms = 0755;
	}
	
	if (move_uploaded_file($_FILES['PDFFile']['tmp_name'], $uploadfile))
	{
		chmod ($uploadfile, $currentperms);
    	echo "File is valid, and was successfully uploaded.\n";
	}
	else 
	{
    	echo "Error moving uploade file.\n";
	}
}