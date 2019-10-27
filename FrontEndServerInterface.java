import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface FrontEndServerInterface extends Remote {

    float getRating(String movie) throws RemoteException;

    ArrayList<String> getUserRatings(int userId) throws RemoteException;

    String addNewRating(String movie, float rating, int userId) throws RemoteException;

    ArrayList<String> didYouMean(String movie) throws RemoteException;

    float getUserRating(int userId, String movie) throws RemoteException;
}
