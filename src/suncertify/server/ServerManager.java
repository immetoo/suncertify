/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.server;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ServerManager manages server beans and create and inject depencicies on the beans.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class ServerManager {

	private ThreadPoolExecutor threadPoolExecutor = null;
	private Map<String,Class<?>> beans = new HashMap<String,Class<?>>(5);
	private Map<String,Object> initBeans = new HashMap<String,Object>(5);

	/**
	 * Creates an ServerManager with an worker pool of threads.
	 */
	public ServerManager() {		
		// core, max, keepalive
        threadPoolExecutor = new ThreadPoolExecutor (
        		3,10,2,
        		TimeUnit.SECONDS,
        		new LinkedBlockingQueue<Runnable>(),
        		new NameingThreadFactory("server-")
        	);
	}
	
	/**
	 * Starts the serverManager backend thread pool.
	 */
	public void start() {
		threadPoolExecutor.prestartAllCoreThreads();
	}
	
	/**
	 * Stops the ServerManager backend thread pool.
	 */
	public void stop() {
		threadPoolExecutor.shutdown();
	}
	
	/**
	 * Execute an Runnable object in the ServerManager thread pool.
	 * @param run	The Object to run.
	 */
	public void execute(Runnable run) {
		threadPoolExecutor.execute(run);
	}
	
	/**
	 * Adds an bean by its class.
	 * @param serverBeanRemote
	 * @param serverBean
	 */
	public void putServerBean(Class<?> serverBeanRemote,Class<?> serverBean) {
		beans.put(serverBeanRemote.getName(),serverBean);
	}

	/**
	 * Add an bean which is already created.
	 * @param name
	 * @param serverBean
	 */
	public void putServerInitBean(String name,Object serverBean) {
		initBeans.put(name,serverBean);
	}
	
	/**
	 * Request an server bean.
	 * This methode also makes sure all depencies are injectes into the bean.
	 * 
	 * @param name
	 * @return
	 * @throws ServerException
	 */
	public Object getServerBean(String name) throws ServerException {
		
		// let init bean be first
		if (initBeans.containsKey(name)) {
			return initBeans.get(name);
		}
		
		Class<?> c = beans.get(name);
		if (c==null) {
			throw new ServerException("Could not find bean for: "+name);
		}
				
		Object result = null;
		try {
			result = c.newInstance();
		} catch (Exception e) {
			throw new ServerException("Could error while init bean: '"+name+"' "+e.getMessage(),e);
		}
		
		for (Field field:result.getClass().getFields()) {
			ServerResource resource = field.getAnnotation(ServerResource.class);
			if (resource==null) {
				continue;
			}
			String beanName = resource.beanName();
			if ("null".equals(beanName)) {
				beanName = field.getType().getName();
			}
			Object bean = getServerBean(beanName);
			if (bean==null) {
				continue;
			}
			try {
				field.set(result,bean);
			} catch (Exception e) {
				throw new ServerException("Could not load resource for bean: "+name+" resource: "+beanName);
			}
		}
		
		return result;
	}
	
	/**
	 * Get all the Beans know in the server.
	 * @return
	 */
	public List<String> getServerBeanNames() {
		List<String> result = new ArrayList<String>(beans.keySet());
		result.addAll(initBeans.keySet());
		return result;
	}
	
	/** 
	 * Get local InvocationHandler which is used for none network mode for this server.
	 * @return
	 */
	public InvocationHandler getLocalInvocationHandler() {
		return new LocalRequestProxy(this);
	}
	
	/**
	 * Custum ThreadFactory for getting thread nameing correctly. 
	 */
	private class NameingThreadFactory implements ThreadFactory {
	    final ThreadGroup group;
	    final AtomicInteger threadNumber = new AtomicInteger(1);
	    final String namePrefix;
	
	    public NameingThreadFactory(String namePrefix) {
	    	if (namePrefix==null) {
	    		namePrefix = "thread-";
	    	}
	    	this.namePrefix=namePrefix;
	        SecurityManager s = System.getSecurityManager();
	        if (s != null) {
	        	group = s.getThreadGroup();
	        } else {
	        	group = Thread.currentThread().getThreadGroup();
	        }
	    }
	
	    public Thread newThread(Runnable r) {
	        Thread t = new Thread(group, r,namePrefix+threadNumber.getAndIncrement(),0);
	        if (t.isDaemon()) {
	            t.setDaemon(false);
	        }
	        if (t.getPriority() != Thread.NORM_PRIORITY) {
	            t.setPriority(Thread.NORM_PRIORITY);
	        }
	        return t;
	    }
	}
	
	/**
	 * InvocationHandler for handeling the method calls of the proxy bean.
	 *
	 */
	private class LocalRequestProxy implements InvocationHandler {
		
		private ServerManager serverManager = null;
		
		public LocalRequestProxy(ServerManager serverManager) {
			this.serverManager=serverManager;
		}
		
	    public Object invoke(Object proxy, Method method,Object[] args) throws Throwable {
	    	Object bean = serverManager.getServerBean(proxy.getClass().getName());
	    	int pS = 0;
	    	if (args!=null) {
	    		pS = args.length;
	    	}
	    	Class<?>[] para = new Class[pS];
	    	for (int i=0;i<pS;i++) {
	    		para[i] = args[i].getClass();
	    	}
	    	Method mm = bean.getClass().getMethod(method.getName(), para);
	    	Object resultObject = mm.invoke(bean,args);
	    	return resultObject;
	    }
	}
}
