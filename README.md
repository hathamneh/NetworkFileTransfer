# Network File Transfer
Small system for for managing files on server using Java.
Multiuser, multithreaded, command line application.

Credit goes to me **Haitham Athamneh** as the main developer. And for **Ahmad Qasrawi and Yazan Al-Haj**.

[Made for educational purposes]

# Features

* Multiuser
You can signup and create as many users as you want, each user has first and last name, username, password and access privilage (All: can access every thing, Restrected: can access only his files).

* Multithreaded
The application can serve multiple connections at the same time.

* Easy to understand
The application is written using modular design, every component is an independent module

# Client Side Commands
* `download $filename [dest]`
Download file from server.
* `upload $filename [private]`
Upload file to server.
* `edit $filename`
Edit a file (it should be text file).
* `ls [-l]`
List files in current directory.
* `pwd`
Show current directory.
* `rm $filename|$folder`
Delete file, or recursivly delete folder.
* `cd ..|$folder`
Change current directory.
* `mkdir $name`
Create new directory.
* `signup $fname $lname $uname $pass all|restr`
Register new user.
* `login $uname $pass`
Login user.
* `logout`
Logout user.
* `passwd`
Update user password.
* `help [$cmd]`
Get help for one or all commands.
* `exit`
Terminate connection and exit.

# Server Side Commands
* `users all`
Show all registered users.
* `users active`
Show all currently connected users.
* `user data $name`
Show all user data.
* `user delete $name`
Delete a user by user name.
* `file info $path`
Show all file meta data.

# One last word
I've published this work to help others understand how you can use java api for sending and receiving files through the network.

# Contact me
If you need any further help in this project, don't be shy, send me an email to himoath@gmail.com
