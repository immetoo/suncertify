/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.core;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;

import suncertify.client.MainView;
import suncertify.db.DataBaseManager;
import suncertify.net.NIOConnector;
import suncertify.net.NetworkClient;
import suncertify.net.NetworkServer;
import suncertify.net.NetworkServer.ServerBackendProvider;
import suncertify.server.ServerManager;
import suncertify.server.beans.HotelRoomManagerRemote;


/**
 * The Main methode to start the SUN Certify UrlBurd submission
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class SunCertifyStarter {
	
	/**
	 * The startup modes.
	 * Which can be used as startup argument.
	 */
	public enum StartupMode {
		server,
		alone,
		client
	}
	
	/** The current mode we are started with. */
	private StartupMode mode = null;
	
	/** The default mode if no arguments are given. */
	static public final StartupMode DEFAULT_MODE = StartupMode.client;
	
	/** The server manager */
	private ServerManager serverManager = null;
	
	/** The database manager */
	private DataBaseManager dataBaseManager = null; 
	
	/** The network manager (server or client) */
	private NIOConnector networkManager = null;
	
	/**
	 * Constructor with the startup mode.
	 * @param mode	The mode to startup the software.
	 */
	public SunCertifyStarter(StartupMode mode) {
		this.mode=mode;
	}
		
	/**
	 * Starts all managers with are needed in the selected mode.
	 * And opens the mainview if nessery.
	 * @throws IOException
	 */
	public void start() throws IOException {
		
		if (mode!=StartupMode.client) {
			// do startup for server and alone
			dataBaseManager = new DataBaseManager();
			dataBaseManager.start();
			serverManager = new ServerManager();
			serverManager.start();
			
			// loading 
			new LoadHotelRoomDB().loadTable(dataBaseManager);
			new LoadServerBeans().loadBeans(serverManager,dataBaseManager);
		}
		
		
		switch(mode) {
		case alone:
			HotelRoomManagerRemote local = (HotelRoomManagerRemote)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
		            new Class[] { HotelRoomManagerRemote.class },
		            serverManager.getLocalInvocationHandler());
			MainView aloneView = new MainView(local);
			aloneView.openView();
			break;
		case client:
			NetworkClient client = new NetworkClient(InetAddress.getByName("localhost"), 9090);
			client.start();
			HotelRoomManagerRemote remote = (HotelRoomManagerRemote)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
		            new Class[] { HotelRoomManagerRemote.class },
		            client.getInvocationHandler(HotelRoomManagerRemote.class));
			MainView clientView = new MainView(remote);
			clientView.openView();
			networkManager = client;
			
			PropertiesManager propertiesManager = new PropertiesManager();
			propertiesManager.start();
			
			break;
		case server:
			ServerBackendProvider serverBackendProvider = new ServerBackendProvider() {
				public void executeWorker(Runnable runable) {
					serverManager.execute(runable);
				}
				public Runnable dataWorker(NetworkServer server,SocketChannel socket,byte[] data) {
					return new NetworkServerWorker(serverManager,server,socket,data);
				}
			};
			networkManager = new NetworkServer(null, 9090, serverBackendProvider);
			break;
		}
		
		if (networkManager!=null) {
			networkManager.start();
		}
	}

	/**
	 * Stops all not null Managers.
	 */
	public void stop() throws IOException {
		try {
			if (networkManager!=null) {
				networkManager.stop();
			}
		} finally {
			try {
				if (serverManager!=null) {
					serverManager.stop();
				}
			} finally {
				if (dataBaseManager!=null) {
					dataBaseManager.stop();
				}
			}
		}
	}
	
	/**
	 * The main startup method of SunCertify Software Application
	 * @param args	The mode to startup with.
	 */
	public static void main(String[] args) {
		StartupMode mode = null;
		if (args.length<1) {
			mode = DEFAULT_MODE;
		} else if (args.length==1) {
			mode = StartupMode.valueOf(args[0]);
		} else if (args.length>1) {
			System.err.println("To many arguments given, only one optionale argument supperted with values: "+StartupMode.values());
			System.exit(1);
		}
		SunCertifyStarter starter = new  SunCertifyStarter(mode);
		try {
			starter.start();
		} catch (Exception e) {
			System.err.println("Could not start SunCertify: "+e.getMessage());
			System.exit(1);
		}
	}
}