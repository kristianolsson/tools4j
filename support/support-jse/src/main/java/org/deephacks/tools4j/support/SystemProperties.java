package org.deephacks.tools4j.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.deephacks.tools4j.support.io.FileUtils;
/**
 * SystemProperties provides a way for reading simple properties.
 * 
 * Properties will be read from multiple sources using a fall-back mechanism. The first
 * source to return a valid value will be returned.
 * 
 * Properties are read in the following order:
 * 
 * 1) System.getProperty
 * 2) User home level file
 * 3) System level file    
 * 4) Class path file
 * 
 */
public class SystemProperties {
	private static final String DEFAULT_GLOBAL_PROPERTY_FILENAME = "tools4j.conf";
	private static final String DEFAULT_PROPERTY_FILE_DIR = ".tools4j";

	private static final String GLOBAL_PROPERTY_FILE_DIR_UNIX = "/etc";
	private static final String GLOBAL_PROPERTY_FILE_DIR_WIN = "c:\\";
	
	private String dir;
	private String filename;
	
	private SystemProperties(String dir, String filename){
		this.dir = dir;
		this.filename = filename;
	}
	public static SystemProperties createDefault(){
		return new SystemProperties(DEFAULT_PROPERTY_FILE_DIR, DEFAULT_GLOBAL_PROPERTY_FILENAME);
	}
	public static SystemProperties create(String dir, String filename){
		return new SystemProperties(dir, filename);
	}
	public String get(String name){
		String v = getSystemProperty(name);
		if(v != null){
			return v;
		}
		v = getUserHomeProperty(name);
		if(v != null){
			return v;
		}
		v = getGlobalProperty(name);
		if(v != null){
			return v;
		}
		URL url = SystemProperties.class.getClassLoader().getResource(filename);
		if(url != null){
			v = getFileProperty(name, url);
			if(v != null){
				return v;
			}
		}
		return null;
	}
	
	private String getGlobalProperty(String name) {
		File propFile = null;
		if(!OS.isMac() && !OS.isUnix()){
			propFile = new File(GLOBAL_PROPERTY_FILE_DIR_WIN, filename);
		} else {
			propFile = new File(GLOBAL_PROPERTY_FILE_DIR_UNIX, filename);
		}
		String v = getFileProperty(name, propFile);
		return v;
	}

	public  String getSystemProperty(String name){
		return System.getProperty(name);
	}
	
	public  String getUserHomeProperty(String name){
			File home = getUserHome();
			if(home == null){
				return null;
			}
			File propFileDir = new File(home, dir);
			File propFile = new File(propFileDir, filename);
			if(!propFile.exists()){
				return null;
			}
			return getFileProperty(name, propFile);
	}
	
	private  String getFileProperty(String name, File file){
		try {
			return getFileProperty(name, new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	private  String getFileProperty(String name, URL url){
		try {
			return getFileProperty(name, url.openStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private  String getFileProperty(String name, InputStream in){
		try {
			Properties p = new Properties();
			p.load(in);
			Object v = p.get(name);
			if(v == null){
				return null;
			}
			return v.toString();
		} catch (IOException e) {
			// user might want to know about this
			throw new RuntimeException(e);
		} finally {
			FileUtils.close(in);
		}		
	}
	private  File getUserHome() {
		String home = System.getProperty("user.home");
		if(home == null || "".equals(home)){
			return null;
		}
		File homeDir = new File(home);
		if(!homeDir.exists()){
			return null;
		}
		return homeDir;
	}
}
