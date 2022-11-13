/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import suncertify.db.DuplicateKeyException;
import suncertify.db.RecordNotFoundException;
import suncertify.db.data.column.Column;
import suncertify.db.data.column.DeleteFlagConverter;

/**
 * The Table class manages one open database file on disk.
 * 
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class Table {
	
	/** The NMAP'ed bytebuffer to the file database on disk. */
	private MappedByteBuffer byteBuffer = null;
	/** Object to locking the bytebuffer. */
	private Object byteBufferLock = new Object();
	/** All the columns of this table */
	private List<Column> columns = null;
	/** The logger to log to. */
	private Logger logger = null;
	/** The total records which are in this file.(incl deleted) */
	private int records = 0;
	/** Offset where the data starts in the byteBuffer */
	private int recordsOffset = 0;
	/** The charset encoding used for the data. */
	private Charset charset = null;
	/** The name of the file */
	private String name = null;
	/** locks for rows */
	private Map<Integer,RowLock> locks = Collections.synchronizedMap(new HashMap<Integer,RowLock>());
	/** cach for rows */
	private Map<Integer,RowCache> cache = new HashMap<Integer,RowCache>();
	/** The maximum of rows in the cache. */
	private int cacheMax = 100;
	/** File which is opened */
	private RandomAccessFile tableFile = null;
	/** The number of records added when table is enlarged. */
	private int extraRecords = 4;
	
	// declair final so can't be altered by reflection
	/** The readLock for the cache */
	final private Lock readLock;
	/** The writeLock for the cache */
	final private Lock writeLock;
	/** The row lock counter */
	final private AtomicInteger rowLocks;
	
	// REST ARE cache values
	
	private int totalColumnsByteSize = 0;
	
	private boolean isFinerLog = false;
	
	/**
	 * Creates the table object.
	 */
	public Table() {
		logger = Logger.getLogger(Table.class.getName());
		isFinerLog = logger.isLoggable(Level.FINER);
		columns = new ArrayList<Column>(10);
		charset = Charset.forName("US-ASCII");
		
		ReentrantReadWriteLock locking = new ReentrantReadWriteLock();
		readLock = locking.readLock();
		writeLock = locking.writeLock();
		
		Random random = new Random();
		rowLocks= new AtomicInteger(random.nextInt());
	}
	
	/**
	 * Opens a table.
	 * 
	 * note: no locking here because connurent access is pas possible
	 * when the table is put in DBManager table storage, which is after this method call.
	 * 
	 * @param file
	 */
	public void openTable(File file) throws IOException {
		// http://www.codefund.com/52/nio-mappedbytebuffer-filechannel-resizing-524047.shtm
		// 
		long startTime = System.currentTimeMillis();
		name = file.getName();
		
		tableFile = new RandomAccessFile(file,"rw");			
		FileChannel channel = tableFile.getChannel();
		int length = (int)channel.size();
		logger.fine("Mapping table size: "+length);
		byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, length);
		logger.finer("Buf info direct: "+byteBuffer.isDirect()+" loaded: "+byteBuffer.isLoaded()+" readonly: "+byteBuffer.isReadOnly());
		// read 4 magic bytes
		int cookie = byteBuffer.getInt();
		logger.fine("DB File cookie: "+cookie);
		if (259!=cookie) {
			throw new IllegalStateException("DB file magic cookie is not correct 259!="+cookie);
		}
		
		// # fields
		char fields = byteBuffer.getChar();
		
		// dele flag
		Column c = new Column();
		c.setCharset(charset);
		c.setFieldLength(1);
		c.setFieldName("___DB___DELETED_ROW_FLAG");
		c.setFieldNameLength(c.getFieldName().length()); // should column 0 should not be saved.
		c.setObjectConverter(new DeleteFlagConverter());
		c.setColumnIndex(0);
		addColumn(c);
		
		// header
		for (int i=0;i<fields;i++) {
			byte size = byteBuffer.get();
			byte[] fieldName = new byte[size];
			byteBuffer.get(fieldName);
			String fieldNameStr = new String(fieldName,charset);
			byte fieldSize = byteBuffer.get();
			//  note +1  is for del flag column
			logger.fine("Got field: "+(i+1)+" name: "+fieldNameStr+" size: "+fieldSize);
			
			c = new Column();
			c.setCharset(charset);
			c.setFieldLength(fieldSize);
			c.setFieldName(fieldNameStr);
			c.setFieldNameLength(size);
			c.setColumnIndex(i+1);
			
			addColumn(c);
		}
		
		recordsOffset = byteBuffer.position();
		records = (length-recordsOffset)/getTotalColumnsByteSize();
		
		logger.info("Total  records: "+records);
		logger.fine("Records offset: "+recordsOffset);
		
		long stopTime = System.currentTimeMillis();
		logger.info("DB Loaded: "+file.getName()+" in "+(stopTime-startTime)+" ms.");
	}
	
	/**
	 * Creates a table with a given format
	 * 
	 * @param file
	 */
	public void createTable(File file,List<Column> columns) throws IOException {
		
		if (file.exists()) {
			throw new IllegalArgumentException("File: "+file.getName()+" already excists.");
		}
		if (columns.size()>255) {
			throw new IllegalArgumentException("More then 255 columns is not supported.");
		}
		char colSize = (char)columns.size();
		
		// create file
		FileOutputStream outputFile = new FileOutputStream(file, true);
	    logger.info("File stream created successfully: "+file.getName());
	    
	    try {
	    	long startTime = System.currentTimeMillis();

	    	FileChannel channel = outputFile.getChannel();
	    	MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
	    	logger.finer("Buf info direct: "+byteBuffer.isDirect()+" loaded: "+byteBuffer.isLoaded()+" readonly: "+byteBuffer.isReadOnly());
	    	
	    	// write 4 magic bytes
	    	byteBuffer.putInt(259);
		
	    	// # fields
	    	byteBuffer.putChar(colSize);
				
	    	// write field headers
	    	for (int i=0;i<colSize;i++) {
	    		Column c = columns.get(i);
	    		byteBuffer.put((byte)c.getFieldNameLength());
	    		byte[] fieldName = c.getFieldName().getBytes(c.getCharset());
	    		byteBuffer.put(fieldName);
	    		byteBuffer.put((byte)c.getFieldLength());		
	    	}
	    	long stopTime = System.currentTimeMillis();
	    	logger.info("Created DB: "+file.getName()+" in "+(stopTime-startTime)+" ms.");
	    } finally {
	    	outputFile.close();
	    }
	}
	
	/**
	 * Closes the table and resources.
	 * @throws IOException
	 */
	public void closeTable() throws IOException {
		synchronized (byteBufferLock) {
			byteBuffer.force();
		}
		tableFile.close();
	}
	
	private void resizeTable() {
		synchronized (byteBufferLock) {
			try {
				byteBuffer.force();
				closeByteBuffer(byteBuffer);
				byteBuffer=null;
				
				FileChannel channel = tableFile.getChannel();
				int lengthOrg = (int)channel.size();
				int length=lengthOrg+(extraRecords*getTotalColumnsByteSize());
				
				logger.info("ReMapping size: "+length+" was: "+lengthOrg+" for extra records: "+extraRecords);
				byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, length);
				
				
				// filling space with free(deleted) records
				
				byteBuffer.position(lengthOrg);
				
				for (int i=lengthOrg;i<length;i++) {
					byteBuffer.put(i, (byte)00);
				}
				
				Column c = columns.get(0);
				for (int i=0;i<extraRecords;i++) {
					byte[] data = c.getObjectConverter().encodeStorage(c,"true");
					setColumnDataByte(records+i,0,data);
				}
				
				// new total
				records = records + extraRecords;
				
			} catch (Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Workaround bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038
	 * 
	 * @param buffer
	 * @throws Exception
	 */
	@SuppressWarnings(value="unchecked")
	private void closeByteBuffer(final Object buffer) {
//		AccessController.doPrivileged(
//				new PrivilegedAction() {
//					public Object run() {
//						try {
//							Method getCleanerMethod = buffer.getClass().getMethod("cleaner",new Class[0]);
//							getCleanerMethod.setAccessible(true);
//							sun.misc.Cleaner cleaner = (sun.misc.Cleaner)getCleanerMethod.invoke(buffer,new Object[0]);
//							cleaner.clean();
//						} catch(Exception e) {
//							e.printStackTrace();
//						}
//						return null;
//					}
//				}
//		);
	}
	
	/**
	 * Returns the Total amount of records which can or are stored in the table.
	 * @return
	 */
	public int getTotalRecords() {
		return records;
	}
	
	/**
	 * Returns the number of columns in this table.
	 * @return
	 */
	public int getTotalColumns() {
		return columns.size();
	}
	
	/**
	 * Returns the total byte size of all the columns together.
	 * @return
	 */
	public int getTotalColumnsByteSize() {
		
		if (totalColumnsByteSize>0) {
			return totalColumnsByteSize;
		}
		int result = 0;
		for (Column c:columns) {
			result+=c.getFieldLength();
		}
		totalColumnsByteSize = result;
		return result;
	}
	
	/**
	 * Checks if the row Id is in bound of this table.
	 * @param row	The row to check
	 * @throws RecordNotFoundException	Throws an RecordNotFoundException if row is negative or is out of bounds.
	 */
	private void checkBounds(int row) throws RecordNotFoundException {
		if (row<0) {
			throw new RecordNotFoundException("Can't handle negative rows.");
		}
		if (row>records) {
			throw new RecordNotFoundException("row number is out of bounds.");
		}
	}
	
	/**
	 * Check if this row is an deleted row.
	 * @param row	The row to check.
	 * @return	true if row is an deleted row. 
	 */
	private boolean checkDeletedRow(int row) {
		Column c = columns.get(0);
		byte[] data = getColumnDataByte(row,0);
		// extra converting to fit in db interface
		String result = c.getObjectConverter().decodeStorage(c, data);
		if ("true".equals(result)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if an row is deleted.
	 * @param row	The row to check.
	 * @throws RecordNotFoundException	is throw when the row is deleted.
	 */
	private void checkDeleted(int row) throws RecordNotFoundException {
		if (checkDeletedRow(row)) {
			throw new RecordNotFoundException("row is deleted: "+row);
		}
	}
	
	private Map<Integer,Integer> colBytes = new HashMap<Integer,Integer>(20);
	public int getTotalColumnsByteSize(int col) {
	
		// cached value.
		if (colBytes.containsKey(col)) {
			return colBytes.get(col);
		}
		
		int result = 0;
		int cols = 0;
		for (Column c:columns) {
			// we store the number of bytes BEFORE column !!
			colBytes.put(cols, result);
			result+=c.getFieldLength();
			cols++;
		}
		
		if (colBytes.containsKey(col)) {
			return colBytes.get(col);
		}
		
		throw new IllegalArgumentException("Column not found: "+col);
	}
	
	private byte[] getColumnDataByte(int row,int column) {
		Column c = columns.get(column);
		int bb = getTotalColumnsByteSize();
		int offset = recordsOffset+(row*bb)+getTotalColumnsByteSize(column);
		if (isFinerLog) {
			logger.finer("Get raw data byte[] ACCESS: row: "+row+" col: "+column+" off: "+offset+" recs: "+records+" start: "+recordsOffset+" recLen: "+bb);
		}
		byte[] result = new byte[c.getFieldLength()];
		
		// only lock byteBuffer access
		synchronized (byteBufferLock) {
			byteBuffer.position(offset);
			byteBuffer.get(result, 0, c.getFieldLength());
		}
		return result;
		
	}
	
	private void setColumnDataByte(int row,int column,byte[] data) {
		int bb = getTotalColumnsByteSize();
		int offset = recordsOffset+(row*bb)+getTotalColumnsByteSize(column);
		if (isFinerLog) {
			logger.finer("Set raw data byte[] ACCESS: row: "+row+" col: "+column+" off: "+offset+" recs: "+records+" start: "+recordsOffset+" recLen: "+bb+" dataSize: "+data.length);
		}
		if (data.length>bb) {
			throw new SecurityException("May not writes longer data then ");
		}
		if (offset>=byteBuffer.limit()) {
			resizeTable();
		}
		
		// only lock byteBuffer access
		synchronized (byteBufferLock) {
			byteBuffer.position(offset);
			byteBuffer.put(data, 0, data.length);
		}
	}
	
	private String getColumnDataString(int row,int column) {
		Column c = columns.get(column);
		byte[] data = getColumnDataByte(row,column);
		
		// extra converting to fit in db interface
		String result = c.getObjectConverter().decodeStorage(c, data);
		try {
			Object o = c.getObjectConverter().decodeInterface(c, result);
			String result2 = c.getObjectConverter().encodeInterface(c, o);
			return result2;
		} catch (Exception e) {
			throw new IllegalStateException("Could not convert data: "+e.getMessage(),e);
		}
	}
	
	private void putColumnDataString(int row,int column,String data) {
		Column c = columns.get(column);
		// extra converting to fit in db interface
		Object result = c.getObjectConverter().decodeInterface(c, data);
		try {
			String o = c.getObjectConverter().encodeInterface(c, result);
			byte[] result2 = c.getObjectConverter().encodeStorage(c, o);
			setColumnDataByte(row,column,result2);
		} catch (Exception e) {
			throw new IllegalStateException("Could not convert data: "+e.getMessage(),e);
		}
	}
	
	public String[] getInterfaceRow(int row) throws RecordNotFoundException {
		String[] result = cacheGet(row);
		if (result!=null) {
			return result;
		}
		
		checkBounds(row);
		checkDeleted(row);
		
		int cols = getTotalColumns()-1;
		result = new String[cols];
		for (int i=0;i<cols;i++) {
			result[i] = getColumnDataString(row,i+1);
		}
		cachePut(row,result);
		return result;
	}
	
	public void putInterfaceRow(int row,String[] data) throws RecordNotFoundException {
		checkBounds(row);
		
		int cols = getTotalColumns()-1;
		for (int i=0;i<cols;i++) {
			String value = data[i];
			putColumnDataString(row,i+1,value);
		}
		
		// update cache
		String[] result = cacheGet(row);
		if (result!=null) {
			cachePut(row,result);
		}
	}
	
	/**
	 * Adds an Column 
	 * @param column	The column to add.
	 */
	public void addColumn(Column column) {
		columns.add(column);
	}
	
	/**
	 * Gets the List of the Columns.
	 * @return	all columns
	 */
	public List<Column> getColumns() {
		return columns;
	}
	
	/**
	 * Returns the name of this table
	 * @return	The name of this table
	 */
	public String getName() {
		return name;
	}
	
	// search
	
	public int[] searchByInterfaceCriteria(String[] criteria) {
		
		if (criteria==null) {
			throw new NullPointerException("Can't search null criteria");
		}
		// -1 = deleted flag column
		if (criteria.length!=getTotalColumns()-1) {
			throw new IllegalArgumentException("criteria array is not same size as table columns.");
		}
		
		Set<Integer> result = new HashSet<Integer>(50);
		
		
		for (int i=0;i<records;i++) {
		
			boolean found = false;
			boolean st = false;
			int col = 1; // skip del column
			for (String c:criteria) {
				if (c.isEmpty()) {
					col++;
					continue;
				}
				
				if (st) {
					break;
				}
				
				//System.out.println("Searching for: "+c);
				Column column = getColumns().get(col);
				try {
					if (column.getColumnSearcher()!=null & c!=null) {
						
						byte[] data = getColumnDataByte(i,column.getColumnIndex());
						String dataResult = column.getObjectConverter().decodeStorage(column, data);
						
						boolean foundData = column.getColumnSearcher().searchColumn(this, column, c, dataResult);
						if (foundData) {
							found = true;
						} else {
							st = true;
							if (found) {
								found = false;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				col++;
			}
			
			if (found) {
				result.add(i);
			}
			
		}
		
		// put in int array		
		int[] res = new int[result.size()];		
		int index=0;
		for(Integer i:result) {
			res[index]=i;
			index++;
		}
		return res;
	}
	
	// CRUD Actions
	
	public void update(int row, String[] data, long lockId) throws RecordNotFoundException, SecurityException {
		checkBounds(row);
		checkDeleted(row);
		checkLock(row,lockId);
		putInterfaceRow(row,data);
	}
	
	/**
	 * Deletes an Row.
	 * 
	 * Only sets row on deleted mark.
	 * And removes row from cached if it was cached.
	 *
	 * @param row
	 * @param lockId
	 * @throws RecordNotFoundException
	 * @throws SecurityException
	 */
	public void delete(int row, long lockId) throws RecordNotFoundException, SecurityException {
		checkBounds(row);
		checkDeleted(row);
		checkLock(row,lockId);
		putColumnDataString(row,0,"true");
		
		// update cache
		String[] result = cacheGet(row);
		if (result!=null) {
			cacheRemove(row);
		}
	}
	
	/**
	 * Create an Row
	 * @param interfaceData
	 * @return
	 * @throws DuplicateKeyException
	 */
	public int create(String[] interfaceData) throws DuplicateKeyException {
		
		Column c = columns.get(0);
		int emptyRow = -1;
		// seq scanning for deleted records:
		for (int i=0;i<records;i++) {
			byte[] data = getColumnDataByte(i,0);
		
			// extra converting to fit in db interface
			String result = c.getObjectConverter().decodeStorage(c, data);
		
			if ("true".equals(result)) {
				emptyRow = i;
				break;
			}
		}
		if (emptyRow==-1) {
			// new recods
			logger.fine("Creating new records in full file ..");
			emptyRow = records+1;
			putColumnDataString(emptyRow,0,"false");
		}
		putColumnDataString(emptyRow,0,"false");
		try {
			putInterfaceRow(emptyRow,interfaceData);
		} catch (RecordNotFoundException re) {
			throw new DuplicateKeyException("NotFoundException wrapper: "+re.getMessage());
		}
		return emptyRow;
	}
	
	
	// LOCK METHODS
	
	/**
	 * Onlocks an locks wichs as obtained bt lock method.
	 */
	public void unlock(int row,long lockId) throws RecordNotFoundException,SecurityException {
		checkBounds(row);
		checkLock(row,lockId);
		if (isFinerLog) {
			logger.finer("Locking row: "+row+" with lockId: "+lockId);
		}
		locks.remove(row);
	}
	
	/**
	 * Sets an lock on an row record.
	 * @param row
	 * @return
	 * @throws RecordNotFoundException
	 */
	public RowLock lock(int row) throws RecordNotFoundException {
		checkBounds(row);
		checkDeleted(row);
		RowLock lock = locks.get(row);
		if (lock!=null) {
			throw new RecordNotFoundException("row already blocked");
		}
		long lockId = rowLocks.getAndIncrement()+new Double(Math.random()).longValue();
		lock = new RowLock(row,20000,lockId);
		if (isFinerLog) {
			logger.finer("Locking row: "+row+" with lockId: "+lockId);
		}
		locks.put(row, lock);
		return lock;
	}

	/**
	 * Checks if the row has an lock with the lockID.
	 * @param row	The row to check the lock on.
	 * @param lockId	The lock id.
	 * @throws RecordNotFoundException		Is thrown when the row id is invalid.
	 * @throws SecurityException			Is thrown when the lock id is invalid.
	 */
	public void checkLock(int row,long lockId) throws RecordNotFoundException,SecurityException {
		checkBounds(row);
		RowLock lock = locks.get(row);
		if (isFinerLog) {
			logger.finer("Checklock: row: "+row+" lockId: "+lockId+" lockOBj: "+lock);
		}
		if (lock==null) {
			throw new SecurityException("No lock for row found.");
		}
		if (lock.getLockId()!=lockId) {
			throw new SecurityException("LockId is not valid for row lock.");
		}
	}
	
	// CACHE METHODS

	private String[] cacheGet(int row) {
		try {
			readLock.lock();
			RowCache c = cache.get(row);
			if (c==null) {
				return null; // just return null on miss, don't use exceptions in high performace program flow
			}
			return c.getRecord();
		} finally {
			readLock.unlock();
		}
	}
	
	/**
	 * puts an item in the cache
	 * @param row
	 * @param data
	 */
	private void cachePut(int row,String[] data) {
		try {
			readLock.lock();		// read lock to check on the size
			if (cache.size()>cacheMax) {
				return; // don't put anything in cache until there is space
			}
		} finally {
			readLock.unlock();
		}
		try {
			writeLock.lock();		// write lock and add the new cache item
			RowCache c = new RowCache(row,data);
			cache.put(row, c);
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Removes an cache item
	 * @param row
	 */
	private void cacheRemove(int row) {
		try {
			writeLock.lock();		// write lock and removed the cache item.
			cache.remove(row);
		} finally {
			writeLock.unlock();
		}
	}
	
}
