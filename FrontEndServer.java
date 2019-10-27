import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class FrontEndServer implements FrontEndServerInterface{

	private static Registry registry;
	private ReplicaManagerInterface stub1;
	private ReplicaManagerInterface stub2;
	private ReplicaManagerInterface stub3;

	public FrontEndServer(){
		try{
			FrontEndServerInterface skeleton = (FrontEndServerInterface) UnicastRemoteObject.exportObject(this, 0);
		    ReplicaManager rm1 = new ReplicaManager();
		    ReplicaManager rm2 = new ReplicaManager();
		    ReplicaManager rm3 = new ReplicaManager();
		    stub1 = (ReplicaManagerInterface) UnicastRemoteObject.exportObject(rm1, 0);
		    stub2 = (ReplicaManagerInterface) UnicastRemoteObject.exportObject(rm2, 0);
		    stub3 = (ReplicaManagerInterface) UnicastRemoteObject.exportObject(rm3, 0);
		    registry = LocateRegistry.getRegistry();
		    registry.bind("FES", skeleton);
		    registry.bind("RM1", rm1);
		    registry.bind("RM2", rm2);
		    registry.bind("RM3", rm3);
		    System.err.println("> Server ready");
		}
		catch (java.rmi.AlreadyBoundException e){
			System.err.println("There exists an outdated version of the rmiregistry. Please close this and try again.");
			System.exit(0);
		}
		catch (Exception e) {
		    System.err.println("Server exception: " + e.toString());
		    e.printStackTrace();
			}
	}

	public static void main(String[] args){
		FrontEndServer fes = new FrontEndServer();
		new Thread(()->{
				while(true){
					try{
						Thread.sleep(20000);
						fes.gossip();
					}
					catch(InterruptedException e){}
				}
			}).start();
		fes.runCli();
	}

	private void runCli(){
		    System.out.println("\n> The status of a Replica Manager may be set here by using the command 'setRM ID status',\nwhere ID is 1,2 or 3 and status can be 'active', 'offline' or 'over-loaded'. This is set to 'active' by default.");
			Scanner sc = new Scanner(System.in);
			while (true){
				String[] splitMsg = sc.nextLine().toLowerCase().split("\\s+");
				if (splitMsg.length > 3){
					System.out.println("Too many parameters");
				}
				if (splitMsg[0].equals("setrm")){
					try{
						int id = Integer.parseInt(splitMsg[1]);
						String status = splitMsg[2].replace("-","");
						ReplicaManagerInterface stub = null;
						if (id == 1){
							if (status.equals("active")){
								stub1.setStatus("active");
								System.out.println("Replica Manager 1 successfuly set active");
							}
							else if (status.equals("offline")){
								System.out.println("Replica Manager 1 successfuly set offline");
								stub1.setStatus("offline");
							}
							else if (status.equals("overloaded")){
								System.out.println("Replica Manager 1 successfuly set overloaded");
								stub1.setStatus("overloaded");
							}
							else{
								System.out.println("No such status");
							}
						}
						else if (id == 2){
							if (status.equals("active")){
								stub2.setStatus("active");
								System.out.println("Replica Manager 2 successfuly set active");
							}
							else if (status.equals("offline")){
								stub2.setStatus("offline");
								System.out.println("Replica Manager 2 successfuly set offline");
							}
							else if (status.equals("overloaded")){
								stub2.setStatus("overloaded");
								System.out.println("Replica Manager 2 successfuly set overloaded");
							}
							else{
								System.out.println("No such status");
							}
						}
						else if (id == 3){
							if (status.equals("active")){
								stub3.setStatus("active");
								System.out.println("Replica Manager 3 successfuly set active");
							}
							else if (status.equals("offline")){
								stub3.setStatus("offline");
								System.out.println("Replica Manager 3 successfuly set offline");
							}
							else if (status.equals("overloaded")){
								stub3.setStatus("overloaded");
								System.out.println("Replica Manager 3 successfuly set overloaded");
							}
							else{
								System.out.println("No such status");
							}
						}
						else{
							System.out.println("No such Replica Manager");
						}
					}
					catch(NumberFormatException e){
						System.out.println("RM number must be an integer");
					}
					catch(ArrayIndexOutOfBoundsException e){
						System.out.println("The setRM command must be followed with 2 parameters");
					}
					catch(RemoteException e){
						e.printStackTrace();
					}
				}
				else{
					System.out.println("No such command");
				}
			}
		}

	public void gossip(){
		System.out.println("Servers are gossiping...");
		try{
			ArrayList<Rating> log1 = stub1.getLog();
			ArrayList<Rating> log2 = stub2.getLog();
			ArrayList<Rating> log3 = stub3.getLog();
			if (log1.isEmpty() && log2.isEmpty() && log3.isEmpty()){
				System.out.println("No updates found");
			}
			else{
				CopyOnWriteArrayList<Rating> unordered = new CopyOnWriteArrayList<Rating>();
				CopyOnWriteArrayList<Rating> ordered = new CopyOnWriteArrayList<Rating>();
				ArrayList<Long> timestamps = new ArrayList<Long>();
				for (Rating r:log1){
					unordered.add(r);
					timestamps.add(r.getTimestamp());
				}
				for (Rating r:log2){
					unordered.add(r);
					timestamps.add(r.getTimestamp());
				}
				for (Rating r:log3){
					unordered.add(r);
					timestamps.add(r.getTimestamp());
				}
				timestamps.sort(null);
				for (int i = 0; i < unordered.size(); i++){
					for (Rating r: unordered){
						if (r.getTimestamp() == timestamps.get(i)){
							ordered.add(r);
							unordered.remove(r);
						}
					}
				}
				for (Rating r:ordered){
					System.out.println("Replica Manager 1:");
					stub1.addNewRating(r.getStrippedMovie(), r.getRating(), r.getUserId());
					System.out.println("Replica Manager 2:");
					stub2.addNewRating(r.getStrippedMovie(), r.getRating(), r.getUserId());
					System.out.println("Replica Manager 3:");
					stub2.addNewRating(r.getStrippedMovie(), r.getRating(), r.getUserId());
				}
			}
			stub1.emptyLog();
			stub2.emptyLog();
			stub3.emptyLog();
			System.out.println("Gossip complete");
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
	}

	public ReplicaManagerInterface establishConnection(){
		ReplicaManagerInterface stub = null;
		try{
			//Connect to the first Replica Manager
			if (stub1.getStatus().equals("offline")){
				System.err.println("RM1 Server currently offline");
				stub = null;
				throw new RemoteException("RM1 Server currently offline");
			}
			else if (stub1.getStatus().equals("overloaded")){
				System.err.println("RM1 Server currently over-loaded");
				stub = null;
				throw new RemoteException("RM1 Server currently over-loaded");
			}
			else{
				stub = stub1;
				System.out.println("Connected to RM1");
			}
		}
		catch(RemoteException e1){
			System.out.println("Failed to connect to RM1. Connecting to RM2...");
			try{
				//Connect to the second Replica Manager
				if (stub2.getStatus().equals("offline")){
					System.err.println("RM2 Server currently offline");
					stub = null;
					throw new RemoteException("RM2 Server currently offline");
				}
				else if (stub2.getStatus().equals("overloaded")){
					System.err.println("RM2 Server currently over-loaded");
					stub = null;
					throw new RemoteException("RM2 Server currently over-loaded");
				}
				else{
					stub = stub2;
					System.out.println("Connected to RM2");
				}
			}
			catch(RemoteException e2){
				System.out.println("Failed to connect to RM2. Connecting to RM3...");
				try{
					//Connect to the third Replica Manager
					if (stub3.getStatus().equals("offline")){
						System.err.println("RM3 Server currently offline");
						stub = null;
						throw new RemoteException("RM3 Server currently offline");
					}
					else if (stub3.getStatus().equals("overloaded")){
						System.err.println("RM3 Server currently over-loaded");
						stub = null;
						throw new RemoteException("RM3 Server currently over-loaded");
					}
					else{
						stub = stub3;
						System.out.println("Connected to RM3");
					}
				}
				catch(RemoteException e3){
					try{
						if (stub1.getStatus().equals("overloaded")){
							stub = stub1;
							System.out.println("No servers are active - connecting to the overloaded server RM1");
						}
						else if (stub2.getStatus().equals("overloaded")){
							stub = stub1;
							System.out.println("No servers are active - connecting to the overloaded server RM2");
						}
						else if (stub3.getStatus().equals("overloaded")){
							stub = stub3;
						}
						else{
							System.out.println("Failed to connect to any of the Replica Manager Servers.\nIf all are offline, try restarting the program or setting them active.");
							stub = null;
						}
					}
					catch (RemoteException e){
						e.printStackTrace();
					}
				}
			}
		}
		return stub;
	}


	public float getRating(String movie){
		float response = 0;
		ReplicaManagerInterface stub = establishConnection();
		try{
			if (stub == null){
				response = -1;
			}
			else{
				response = stub.getRating(movie);
			}
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		return response;
	}
	public ArrayList<String> getUserRatings(int userId){
		ArrayList<String> response = null;
		ReplicaManagerInterface stub = establishConnection();
		try{
			if(stub != null){
				response = stub.getUserRatings(userId);
			}
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		return response;
	}
	public float getUserRating(int userId, String movie){
		float response = 0;
		ReplicaManagerInterface stub = establishConnection();
		try{
			if (stub == null){
				response = -1;
			}
			else{
				response = stub.getUserRating(userId, movie);
			}
		}
		catch (RemoteException e){
			e.printStackTrace();
		}
		return response;
	}
	public String addNewRating(String movie, float rating, int userId){
		String response = "";
		ReplicaManagerInterface stub = establishConnection();
		try{
			if (stub == null){
				response = "-1";
			}
			else{
				response = stub.addNewRating(movie,rating,userId);
			}
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		return response;
	}

	public ArrayList<String> didYouMean(String movie){
		ArrayList<String> response = null;
		try{
			ReplicaManagerInterface stub = establishConnection();
			response = stub.didYouMean(movie);
			return (response);
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		return response;
	}
}