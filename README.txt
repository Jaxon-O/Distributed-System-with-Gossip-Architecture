To run, start the ReplicaManager and FrontEndServer batch files. This takes ~15 seconds. Wait until the FrontEndServer prints 'server ready' then a client program may be started.

The rmiregistry is started with the ReplicaManager batch file. Only one may be running at any one time for the program to work.

The FrontEndServer CLI will report on connections to clients and RMs, as well as gossip. The command setrm can be used to set a specific RM to a specific status

The client program will run automatically and takes various commands, all of which can be viewed by typing 'help'.
The update function is included in the set function, and will do so if the user has already rated that movie.
The get movie command will return a movies overall average rating. If the movie isnt found, the client will send another query to suggest similar movies.
The getuser command followed by only an id will print all ratings by that user. If followed by a movie, will print the user rating for a specific movie if it exists.