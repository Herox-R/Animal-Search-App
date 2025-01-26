# Animal Search App
This application uses a TCP socket to enable communication between the ClientApplication and ServerApplication. The ClientApplication sends requests (such as animal search, insert, or delete) to the ServerApplication, which processes these requests and returns responses.

# Simple Project Framework:
Java SE (Core framework)
Apache Ant (Build tool)
JDBC (For SQL Server communication)
SQL Server (Database)

# How it works 
The app is a client-server application that allows users to search, add, or delete animal records.
-ClientApplication: This is the user-facing app where you can search for animals by name or species, and also add or delete animals if you're an admin. It connects to the server to send and receive data.
-ServerApplication: This is the backend that manages the data. It stores animal information in a database and responds to requests from the client, like searching for an animal or updating records.

# Author 
-Sylvester N
