
package suncertify.net;	

import java.io.IOException;
import java.nio.channels.SocketChannel;

import suncertify.core.LoadHotelRoomDB;
import suncertify.core.LoadServerBeans;
import suncertify.core.NetworkServerWorker;
import suncertify.db.DataBaseManager;
import suncertify.net.NetworkServer.ServerBackendProvider;
import suncertify.server.ServerManager;

/**
 * Starts the server.
 * 
 * Currently max: (note: fully cached results)
 * Dec 15, 2008 1:19:01 AM suncertify.net.NetworkNIOConnector$1 run
 * INFO: sendObject() Statistics TPM: 86326 TPS: 1438
 *
 * @author Willem Cazander
 */
public class StartServer {
	
	private NetworkServer server = null;
	
	public void start() throws Exception {
		DataBaseManager dataBaseManager = new DataBaseManager();
		dataBaseManager.start();
		final ServerManager serverManager = new ServerManager();
		serverManager.start();
		
		new LoadHotelRoomDB().loadTable(dataBaseManager);
		new LoadServerBeans().loadBeans(serverManager,dataBaseManager);
	    	
		ServerBackendProvider serverBackendProvider = new ServerBackendProvider() {
			public void executeWorker(Runnable runable) {
				serverManager.execute(runable);
			}
			public Runnable dataWorker(NetworkServer server,SocketChannel socket,byte[] data) {
				return new NetworkServerWorker(serverManager,server,socket,data);
			}
		};
		server = new NetworkServer(null, 9090, serverBackendProvider);
		server.start();
	}
	
	public void stop() throws IOException {
		server.stop();
	}
	
	public static void main(String[] args) {
		try {
			new StartServer().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}