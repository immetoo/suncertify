

package suncertify.net;	

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.List;

import suncertify.models.HotelRoom;
import suncertify.server.beans.HotelRoomManagerRemote;
import junit.framework.TestCase;

/**
 * Tests the network layer with multi threads and connections
 * 
 * @author Willem Cazander
 */
public class ThreadedClientTest extends TestCase {

	NetworkClient client = null;
	
	@Override
	protected void setUp() throws Exception {
		client = new NetworkClient(InetAddress.getByName("localhost"), 9090);
		client.start();
	}

	@Override
	protected void tearDown() throws Exception {
		client.stop();
	}
	
	public void testSingleRequest() throws Exception {
	     final HotelRoomManagerRemote remote = (HotelRoomManagerRemote)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
	              new Class[] { HotelRoomManagerRemote.class },
	              client.getInvocationHandler(HotelRoomManagerRemote.class));
		  long startTime = System.currentTimeMillis();
		  List<HotelRoom> rooms = remote.getAllHotelRooms();
		  long stopTime = System.currentTimeMillis();
		  System.out.println("got rooms1: "+rooms.size()+" from threads: "+Thread.currentThread().getName()+" in "+(stopTime-startTime)+" ms.");
		
		  if (rooms.isEmpty()) {
			  assertEquals(true,false);
		  }
	}
	
	public void testThreadedRequestSingleRemote() throws Exception {
	     final HotelRoomManagerRemote remote = (HotelRoomManagerRemote)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
	              new Class[] { HotelRoomManagerRemote.class },
	              client.getInvocationHandler(HotelRoomManagerRemote.class));
		
	      for (int i=0;i<1;i++) {	    	  
	    	  Thread tt = new Thread(new Runnable() {
	    		  public void run() {
	    			  try {
	    				  for (int i=0;i<20000;i++) {
	    					  long startTime = System.currentTimeMillis();
	    					  List<HotelRoom> rooms = remote.getAllHotelRooms();
	    					  long stopTime = System.currentTimeMillis();
	    					  if (i%1000==0) {
	    						  System.out.println(i+" got rooms: "+rooms.size()+" from threads: "+Thread.currentThread().getName()+" in "+(stopTime-startTime)+" ms.");
	    					  }
	    				  }
	    			  } catch (Exception e) {
	    				  e.printStackTrace();
	    			  }
	    		  }
	    	  });
	    	  tt.setPriority(10);
	    	  tt.setDaemon(false);
	    	  tt.setName("client-bench-"+i);
	    	  tt.start();
	      }
	      
	      Thread.sleep(22000);
	}
	
	public void testThreadedRequestThreadRemote() throws Exception {
	      for (int i=0;i<10;i++) {	    	  
	    	  Thread tt = new Thread(new Runnable() {
	    		  public void run() {
	    			  try {
	    				  HotelRoomManagerRemote remote = (HotelRoomManagerRemote)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
	    			              new Class[] { HotelRoomManagerRemote.class },
	    			              client.getInvocationHandler(HotelRoomManagerRemote.class));
	    				  for (int i=0;i<1000;i++) {
	    					  //Thread.sleep(new Random().nextInt(400)	);
	    					  long startTime = System.currentTimeMillis();
	    					  List<HotelRoom> rooms = remote.getAllHotelRooms();
	    					  long stopTime = System.currentTimeMillis();
	    					  if (i%100==0) {
	    						  System.out.println(i+" got rooms: "+rooms.size()+" from threads: "+Thread.currentThread().getName()+" in "+(stopTime-startTime)+" ms.");
	    					  }
	    				  }
	    			  } catch (Exception e) {
	    				  e.printStackTrace();
	    			  }
	    		  }
	    	  });
	    	  tt.setPriority(10);
	    	  tt.setDaemon(false);
	    	  tt.setName("client-bench-"+i);
	    	  tt.start();
	      }
	      Thread.sleep(22000);
	}
	
	public void testThreadedRequestPerThreadClient() throws Exception {	
	      for (int i=0;i<10;i++) {	    	  
	    	  Thread tt = new Thread(new Runnable() {
	    		  NetworkClient clientThread = null;
	    		  public void run() {
	    			  try {
	    				  clientThread = new NetworkClient(InetAddress.getByName("localhost"), 9090);
	    				  clientThread.start();
	    				  HotelRoomManagerRemote remote = (HotelRoomManagerRemote)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
	    			              new Class[] { HotelRoomManagerRemote.class },
	    			              clientThread.getInvocationHandler(HotelRoomManagerRemote.class));
	    				  for (int i=0;i<1000;i++) {
	    					  //Thread.sleep(new Random().nextInt(400)	);
	    					  long startTime = System.currentTimeMillis();
	    					  List<HotelRoom> rooms = remote.getAllHotelRooms();
	    					  long stopTime = System.currentTimeMillis();
	    					  if (i%100==0) {
	    						  System.out.println(i+" got rooms: "+rooms.size()+" from threads: "+Thread.currentThread().getName()+" in "+(stopTime-startTime)+" ms.");
	    					  }
	    				  }
	    			  } catch (Exception e) {
	    				  e.printStackTrace();
	    			  } finally {
	    				  try {
							clientThread.stop();
						} catch (IOException e) {
							e.printStackTrace();
						}
	    			  }
	    		  }
	    	  });
	    	  tt.setPriority(10);
	    	  tt.setDaemon(false);
	    	  tt.setName("client-bench-"+i);
	    	  tt.start();
	      }
	      Thread.sleep(22000);
	}
}