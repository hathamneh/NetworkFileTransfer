# Network File Transfer
For educational purposes only

# Instructor Description
Your application should consist of a file server that serves clients according to the
following specifications:

* A new client should sign up with his full name, user name and password. The
server application should ask the user running the server side to specify the
access rights of the client to one of the following: All (client can access all files,
both public and private) and Restricted (client can access public files only).
* A registered client should sign in with his user name and password. The server
should check whether the user name and password match. If they do not match
it should send an error message to the client. If they match it should ask the
client to choose an operation from the following: download file, upload file,
modify file, display all files in a directory, change password, display file info,
and log off.
* According to the selected operation, one of the following should be performed:
    1. Download file: the client should provide the name and path of the file to be
downloaded. If the file is not found, it should send an error message to the
client. If the client access rights do not allow the user to download the file, it
should send an error message to the client. Otherwise, the file should be sent
to the client.
    2. Upload file: the client should provide the name of the file and the path where
the file should be uploaded. The client should also specify whether the file is
private or public. Then the client should send the file.
    3. Modify file: the client should provide the name and path of the file to be
modified. If the file is not found, it should send an error message to the
client. If the client access rights do not allow the user to modify the file, it
should send an error message to the client. Otherwise, the file should be
modified by the client. Note that if a file is being modified by a client, no
other client should be allowed to download or modify the file until the
modification is done.
    4. Display all files in a directory: the client should provide the path of the
directory it wants to display its files. The server should send back a list of
the files names and all their info.
    5. Change password: the client should enter the new password and confirm it.
If the password is entered twice the same, it should change the password.
Otherwise, it should send an error message to the client.
    6. Display file info: the client should provide the name and path of the file
whose info is requested.
    7. Log off: the client connection should be terminated.
* The server application should keep records of all registered clients info and the
files it hosts. The user running the server application should be able to display
info of a specific registered client or file or all registered clients and files info.
    1. Registered client info include: full name, user name, date of signing
up, last date he/she signed in, access rights (All, public)
    2. File info include: file name, private/public, upload date, name of user
who uploaded the file, last modification date, name of user who did
the last modification, number of times the file is downloaded.

* The user running the server application should be able to obtain all clients
currently logged in.