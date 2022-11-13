

package suncertify.db;	

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import suncertify.core.LoadHotelRoomDB;
import suncertify.db.Data;
import suncertify.db.data.RowLock;
import suncertify.db.data.Table;
import suncertify.models.HotelRoom;
import suncertify.models.HotelRoomDBConverter;
import junit.framework.TestCase;


public class InsertTest extends TestCase {
	
	
	public void printRecord(String[] record) {
		StringBuilder buf = new StringBuilder(200); 
		for (String value:record) {
			buf.append("'");
			buf.append(value);
			buf.append("'\t");
		}
		buf.append("");
		
		Logger.getAnonymousLogger().info("data: "+buf);
	}
	
	public List<HotelRoom> getKamers() throws Exception {
		
		URL u = new URL("url-not-there......");
		InputStream in = u.openStream();
		
		InputStreamReader fileReader = new InputStreamReader(in,"UTF-8");
        LineNumberReader lineReader = new LineNumberReader(fileReader);
		
        List<HotelRoom> result = new ArrayList<HotelRoom>(200);
        
        String line = null;
        int i = 0;
        while ((line = lineReader.readLine()) !=null) {
        	if (line.contains("offerid")) {
        		continue; // skip header
        	}
        	i++;
        	
        	String[] d = line.split("\t");
        	
        	HotelRoom r = new HotelRoom();
        	r.setDateAvailable(new Date()); // d[11]
        	
        	String s = d[1];
        	if (s.length()>63) {
        		s = s.substring(0,63);
        	}
        	r.setLocation(s);
        	
        	if ("".equals(s)) {
        		continue;
        	}
        	
        	s = d[2];
        	if (s.length()>63) {
        		s = s.substring(0,63);
        	}
        	r.setName(s);
        	
        	if ("".equals(s)) {
        		continue;
        	}
        	
        	r.setCustomerId(i);
        	r.setPriceRate(new Long(d[5]));
        	r.setSize(1);
        	r.setSmoking(false);

        	result.add(r);
        }
		
		
		
		return result;
	}
	
	
	public void testData() throws Exception {
		DataBaseManager dataBaseManager = new DataBaseManager();
		new LoadHotelRoomDB().loadTable(dataBaseManager);
		Table table = dataBaseManager.getHotelRoomTable();	
			
		Data d = new Data(dataBaseManager.getHotelRoomTable());
		HotelRoomDBConverter c = new HotelRoomDBConverter(table);
		
		for (HotelRoom r:getKamers()) {
			String[] data = c.encode(r);
			printRecord(data);
			d.create(data);
		}
		
		table.closeTable();
	}
}