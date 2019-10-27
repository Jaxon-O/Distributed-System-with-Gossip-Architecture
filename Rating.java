import java.io.Serializable;

public class Rating implements Serializable{

	private int userId = -1;
	private int movieId = -1;
	private float rating = -9999999;
	private String movie = "";
	private String strippedMovie = "";
	private long timestamp = 0;

	public Rating(int userId, int movieId, float rating, String movie){
		setUserId(userId);
		setMovieId(movieId);
		setRating(rating);
		setMovie(movie);
	}

	public void stripMovie(String movieToStrip){
		strippedMovie = movieToStrip.replaceAll("\\(.*?\\)", "").toLowerCase();
		strippedMovie = strippedMovie.replaceAll("[!,:-]","");
		strippedMovie = strippedMovie.replace("*","");
		strippedMovie = strippedMovie.replace("'","");
		strippedMovie = strippedMovie.replace(".","");
		strippedMovie = strippedMovie.replace("\"","");
		if (strippedMovie.length() > 4){
			if (strippedMovie.substring(0,2).equals("a ")){
				strippedMovie = strippedMovie.replace(" ","");
					strippedMovie = strippedMovie.substring(1) + strippedMovie.substring(0,1);
				}
			else if (strippedMovie.substring(0,4).equals("the ")){
				strippedMovie = strippedMovie.replace(" ","");
				strippedMovie = strippedMovie.substring(3) + strippedMovie.substring(0,3);
			}
		}
		strippedMovie = strippedMovie.replace(" ","");
	}

	public void setUserId(int userId){
		this.userId = userId;
	}
	public void setMovieId(int movieId){
		this.movieId = movieId;
	}
	public void setRating(float rating){
		this.rating = rating;
	}
	public void setMovie(String movie){
		this.movie = movie;
		stripMovie(movie);
	}
	public void setTimestamp(long timestamp){
		this.timestamp = timestamp;
	}
	public int getUserId(){
		return userId;
	}
	public int getMovieId(){
		return movieId;
	}
	public float getRating(){
		return rating;
	}
	public String getMovie(){
		return movie;
	}
	public String getStrippedMovie(){
		return strippedMovie;
	}
	public long getTimestamp(){
		return timestamp;
	}

}