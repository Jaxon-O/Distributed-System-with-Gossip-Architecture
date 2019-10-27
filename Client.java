import java.util.Scanner;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;


public class Client{

	Scanner sc = new Scanner(System.in);

	public static void main(String[] args){
		try {
	    	// Get registry
	    	Registry registry = LocateRegistry.getRegistry();
	    	// Lookup the remote object "Hello" from registry
	    	// and create a stub for it
	    	FrontEndServerInterface stub = (FrontEndServerInterface) registry.lookup("FES");
	    	Client client = new Client();
			client.start(stub);
	    	}
	    catch (java.rmi.ConnectException e){
	    	System.err.println("Server not currently running");
	    }
	    catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public void start(FrontEndServerInterface stub){
		System.out.println();
		System.out.println("Welcome to our Movie Rating program. Type 'help' if you are unsure how to use the program.");
		Boolean running = true;
		while (running == true){
			System.out.println("\nEnter your next command:\n");
			String msg = sc.nextLine().toLowerCase();
			String[] splitMsg = msg.split("\\s+");
			if (msg.equals("quit") || msg.equals("exit") || msg.equals("close")){
				System.out.println("Thank you for using our program. Goodbye.");
				running = false;
			}

			else if (msg.equals("help") || msg.equals("?")){
				System.out.println("\n> To find the reviews for a movie, type 'get movie', where 'movie' is replaced with a movie of your choosing.");
				System.out.println("> To find all ratings by a user, type 'getUser userId', where 'userId' is an integer corresponding to a users ID.");
				System.out.println("> By adding a movie onto the end of this command, you can get a rating from a user for a specific movie, if it exists.");
				System.out.println("> To add a review to a movie, type 'set movieName rating userId', where 'rating' is a decimal from 1-5.");
				System.out.println("> To update a movie you have already rated, simply do so again with the same userId and your rating will be updated automatically.");
				System.out.println("> Note: The higher the rating, the more positive the review.");
				System.out.println("> To exit the program, type 'exit'.");
			}

			else if (splitMsg[0].equals("get")){
				String movie = "";
				for (int i = 1; i < splitMsg.length; i++){
					movie += splitMsg[i]+" ";
				}
				movie = movie.replaceAll("[!,:-]","");
				movie = movie.replace("'","");
			    movie = movie.replace(".","");
				if (movie == ""){
					System.out.println("\n> Please follow the 'get' command with a movie name");
				}
				else{
					getMovieRatings(movie, stub);
				}
			}
			else if(splitMsg[0].equals("getuser")){
				if(splitMsg.length == 1){
					System.err.println("\n> Please follow the 'getUser' command with a userId followed by an optional movie.");
				}
				else{
					try{
						int userId = Integer.parseInt(splitMsg[1]);
						if (splitMsg.length == 2){
							getUserRatings(userId, stub);
						}
						else{
							String movie = "";
							for (int i = 2; i < splitMsg.length; i++){
								movie = movie + splitMsg[i];
							}
							getUserRating(userId, movie, stub);
						}
					}
					catch (NumberFormatException e){
						System.err.println("\n> The user ID should consist of only an integer.");
					}
				}
			}
			else if (splitMsg[0].equals("set")){
				String movie = "";
				int rating = -1;
				try{
					rating = Integer.parseInt(splitMsg[splitMsg.length-1]);
					for (int i = 1; i < splitMsg.length-2; i++){
						movie += splitMsg[i] + " ";
					}
					movie = movie.replaceAll("[!,:-]","");
					movie = movie.replace("'","");
			    	movie = movie.replace(".","");
			    	float newRating = Float.parseFloat(splitMsg[splitMsg.length-2]);
			    	int userId = Integer.parseInt(splitMsg[splitMsg.length-1]);
					addNewRating(userId, newRating, movie, stub);
				}
				catch (NumberFormatException e){
					System.out.println("Incorrect syntax. Should be in the form 'set <movie> <rating> <userId>'.");
				}
			}
			else {
				System.out.println("\n> Unknown command. Type 'help' if you are stuck");
				System.out.println("\n> Note that spaces will affect your input");
			}
		}
	}

	public void getMovieRatings(String movie, FrontEndServerInterface stub){
		try{
			//Formatting for the way that the data is stored in the server
			//(If a movie starts with "The", it will go to the end after a comma in quotation)
			if (movie.substring(0,2).equals("a ")){
				movie = movie.replace(" ","");
					movie = movie.substring(1) + movie.substring(0,1);
				}
			else if (movie.length() > 3){
				if (movie.substring(0,4).equals("the ")){
					movie = movie.replace(" ","");
					movie = movie.substring(3) + movie.substring(0,3);
				}
			}
			movie = movie.replace(" ","");
			float response = stub.getRating(movie);
			if (Float.isNaN(response)){
				ArrayList<String> similar = stub.didYouMean(movie);
				if (similar.isEmpty() || movie.length() < 3){
					System.out.println("\n> No such movie found");
				}
				else{
					System.out.println("\n> No such movie found. Did you mean one of the following?\n");
					for (int i = 0; i < similar.size(); i++){
						System.out.println("> "+similar.get(i));
					}
				}
			}
			else if (response == -1){
				System.out.println("\n> Failed to connect to a server");
			}
			else{
				System.out.println("\n> Movie rating = "+response);
			}
		}
		catch(NullPointerException e){
			System.err.println("\n> No such movie found. Make sure the spelling is correct");
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void getUserRatings(int userId, FrontEndServerInterface stub){
		try{
			ArrayList<String> response = stub.getUserRatings(userId);
			if(response == null){
				System.out.println("\n> Failed to connect to server");
			}
			else if (response.isEmpty()){
				System.out.println("\n> No such user was found");
			}
			else{
				System.out.println("\n> Here are all the found ratings for this user:\n");
				for (int i = 0; i < response.size(); i++){
					System.out.println("> "+response.get(i));
				}
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void getUserRating(int userId, String movie, FrontEndServerInterface stub){
		try{
			float response = stub.getUserRating(userId, movie);
			if(response == -1){
				System.out.println("\n> Failed to connect to server");
			}
			else if (Float.isNaN(response)){
				System.out.println("\n> This user has not rated this film.");
			}
			else{
				System.out.println("\n> User "+userId+" rated this movie "+response);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void addNewRating(int userId, float rating, String movie, FrontEndServerInterface stub){
		try{
			if (movie.substring(0,2).equals("a ")){
				movie = movie.replace(" ","");
					movie = movie.substring(1) + movie.substring(0,1);
				}
			else if (movie.length() > 3){
				if (movie.substring(0,4).equals("the ")){
					movie = movie.replace(" ","");
					movie = movie.substring(3) + movie.substring(0,3);
				}
			}
			movie = movie.replaceAll("\\(.*?\\)", "").toLowerCase().replace(" ","");
			movie = movie.replaceAll("[!,:-]","");
			movie = movie.replace("*","");
			String response = stub.addNewRating(movie, rating, userId);
			if (response.equals("-1")){
				System.out.println("\n> Failed to connect to server");
			}
			else{
				System.out.println(response);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}