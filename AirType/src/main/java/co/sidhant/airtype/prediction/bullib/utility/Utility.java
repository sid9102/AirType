package bullib.utility;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import bullib.network.Weight;

public class Utility{
	
	public static Comparator<Entry<?,Weight>> entryComparator = new Comparator<Entry<?,Weight>>(){
		@Override
		public int compare(Entry<?, Weight> o1, Entry<?, Weight> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	};
	
	public static File getFile(String filename){
		File found = null;
		try{
			found = new File(filename);
		}
		catch(Exception x1){
			try{
				found = new File(System.getProperty("user.dir")+"\\"+filename);
			}
			catch(Exception x2){
				System.out.println("Utility.getFile("+filename+") was unable to find the specified file, returning a null reference.");
			}
		}
		return found;
	}
	
	public static Object deserialize(byte[] serialized) throws Exception {
		ByteArrayInputStream bi = new ByteArrayInputStream(serialized);
		ObjectInputStream si = new ObjectInputStream(bi);
		Object retobj = si.readObject();
		si.close();
		return retobj;
	}
	
	public static Object deserialize(File f) throws Exception{
		FileInputStream fi = new FileInputStream(f);
		ObjectInputStream oi = new ObjectInputStream(fi);
		Object retobj = oi.readObject();
		oi.close();
		return retobj;
	}

	public static byte[] serialize(Object target) throws Exception {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream so = new ObjectOutputStream(bo);
		so.writeObject(target);
		byte[] bytes = bo.toByteArray();
		bo.close();
		return bytes;
	}
	
	public static void serialize(Object t, File f) throws Exception{
		FileOutputStream fo = new FileOutputStream(f);
		ObjectOutputStream oo = new ObjectOutputStream(fo);
		oo.writeObject(t);
		oo.close();
	}

	public static byte[] load(String filename)	throws Exception {
		RandomAccessFile file = new RandomAccessFile(new File(filename), "r");
		byte[] b = new byte[(int)file.length()];
		file.read(b);
		file.close();
		return b;
	}
	
	public static FileInputStream load(File f) throws Exception{
		return new FileInputStream(f);
	}

	public static void save(String filename, byte[] value) throws Exception {
		RandomAccessFile file = new RandomAccessFile(new File(filename), "rw");
		file.write(value);
		file.close();
	}
	
	public static FileOutputStream save(File f) throws Exception{
		return new FileOutputStream(f);
	}

	public static Collection<String> executeRegex(Pattern pattern, String text){
		LinkedList<String> matches = new LinkedList<String>();
		Matcher m = pattern.matcher(text);
		while (m.find()) {
			matches.add(m.group(0));
		}
		return matches;
	}
}