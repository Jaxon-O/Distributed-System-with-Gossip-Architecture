import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.*;
import java.util.List;
import java.lang.Math;
import java.time.Instant;
	
public class ReplicaManager implements ReplicaManagerInterface {

	private ArrayList<Rating> data = new ArrayList<Rating>();
	private ArrayList<Rating> log = new ArrayList<Rating>();
	private String status = "active";
	
    public ReplicaManager() {
    	loadFiles();
    }

    public static void main(String args[]) {
		try {
		    Registry registry = LocateRegistry.getRegistry();
		    System.err.println("Server ready");
			} 
		catch (Exception e){
			e.printStackTrace();
		}
    }

    public void setStatus(String stat){
    	if(stat.equals("active")){
    		status = "active";
    	}
    	else if(stat.equals("offline")){
    		status = "offline";
    	}
    	else if(stat.equals("overloaded")){
    		status = "overloaded";
    	}
    }

    public String getStatus(){
    	return status;
    }

    public ArrayList<Rating> getLog(){
    	return log;
    } 

    public void emptyLog(){
    	log.clear();
    }

    private void loadFiles(){
		//Loading and formatting for the raitings file which includes every rating and userId for each movie
		try{
			BufferedReader br = new BufferedReader(new FileReader("databases/ratings.csv"));
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				if (count != 0){
					String[] temp = line.split(",");
					int userId = Integer.parseInt(temp[0]);
					int movieId = Integer.parseInt(temp[1]);
					float rating = Float.parseFloat(temp[2]);
					Rating newRating = new Rating(userId, movieId, rating, "");
					data.add(newRating);
				}
				else{}
				count += 1;
			}
			br.close();
			br = new BufferedReader(new FileReader("databases/movies.csv"));
			line = "";
			count = 0;
			while ((line = br.readLine()) != null) {
				if (count != 0){
					String[] temp = line.split(",");
					int movieId = Integer.parseInt(temp[0]);
					String movie = temp[1];
					for (int i = 0; i < temp.length-3; i++){
			        	movie = movie + "," + temp[i+2];
			        }
			        for (Rating check: data){
			        	if (check.getMovieId() == movieId){
			        		check.setMovie(movie);
			        	}
			        }
			    }
			    else{}
			    count+=1;
			}
		}
		catch(FileNotFoundException e){}
		catch(IOException e){}
    }


    //Returns average rating for a given movie
    public float getRating(String movie){
    	float average = 0;
    	int count = 0;
    	for (int i = 0; i < data.size(); i++){
    		Rating rating = data.get(i);
    		if (rating.getStrippedMovie().equals(movie)){
    			average += rating.getRating();
    			count += 1;
    		}
    	}
    	average = average/count;
    	return(average);
    }

    public ArrayList<String> getUserRatings(int userId){
    	ArrayList<String> results = new ArrayList<String>();
    	for (int i = 0; i < data.size(); i++){
    		Rating rating = data.get(i);
    		if (rating.getUserId() == userId){
    			String msg = rating.getMovie() + ", " + rating.getRating();
    			results.add(msg);
    		}
    	}
    	return results;
    }

    public float getUserRating(int userId, String movie){
    	float result = Float.NaN;
    	for (int i = 0; i < data.size(); i++){
    		Rating rating = data.get(i);
    		if (rating.getUserId() == userId && rating.getStrippedMovie().equals(movie)){
    			result = rating.getRating();
    			break;
    		}
    	}
    	return result;
    }

    public String addNewRating(String movie, float rating, int userId){
    	if (rating < 0 || rating > 5){
    		return("Rating must be between 0 and 5.");
    	}
    	String msg = "Error";
    	Boolean found = false;
    	if (update(userId, movie, rating) == true){
    		found = true;
    		msg = "User has already rated this movie. The rating was updated";
    	}
    	else{
	    	for (int i = 0; i < data.size(); i++){
	    		Rating currentRating = data.get(i);
	    		if (currentRating.getStrippedMovie().equals(movie)){
	    			int movieId = currentRating.getMovieId();
	    			Instant instant = Instant.now();
	    			long timestamp = instant.toEpochMilli();
	    			Rating newRating = new Rating(userId, currentRating.getMovieId(), rating, currentRating.getMovie());
	    			newRating.setTimestamp(timestamp);
	    			data.add(newRating);
	    			log.add(newRating);
	    			System.out.println("ADDED: Movie = "+currentRating.getMovie()+"   Rating = "+rating+"   User = "+userId);
	    			msg = "Successfuly added new rating";
	    			found = true;
	    			break;
	    		}
	    	}
	    }
    	if (found == false){
    		msg = "No such movie found. If you would like to add a new movie, please contact an Administrator";
    	}
		return (msg);
    }

    public Boolean update(int userId, String movie, float rating){
    	Boolean response = false;
    	for (int i = 0; i < data.size(); i++){
    		Rating currentRating = data.get(i);
    		if (currentRating.getUserId() == userId && currentRating.getStrippedMovie().equals(movie)){
    			currentRating.setRating(rating);
    			Instant instant = Instant.now();
    			long timestamp = instant.toEpochMilli();
    			currentRating.setTimestamp(timestamp);
    			log.add(currentRating);
    			response = true;
    			System.out.println("UPDATED: Movie = "+currentRating.getMovie()+"   Rating = "+rating+"   User = "+userId);
    			break;
    		}
    	}
    	return(response);
    }

    public ArrayList<String> didYouMean(String movie){
    	ArrayList<String> similar = new ArrayList<String>();
    	for (int i = 0; i < data.size(); i++){
    		if (data.get(i).getStrippedMovie().contains(movie) && movie.length()>2 && !similar.contains(data.get(i).getMovie())){
    			similar.add(data.get(i).getMovie());
    		}
    	}
    	return similar;
    }

}