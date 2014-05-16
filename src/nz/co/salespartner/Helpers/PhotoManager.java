package nz.co.salespartner.Helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.embarcadero.javaandroid.DSProxy.TServerMethods1;

import android.util.Log;

public class PhotoManager {
	public final static String LOCATION = "/sdcard/pics1/";
	private static String TAG = "PhotoManager";
	private static int MAXPHOTOS = 19;

	public static File getFile(String ref, int picNum) {
		return new File(getFileName(ref, picNum));
	}

	public static Boolean exists(String ref, int picNum) {
		return getFile(ref, picNum).exists();
	}

	public static Boolean deleteIfExists(String ref, int picNum) {
		// returns true if the file existed
		File f = getFile(ref, picNum);
		if (f.exists()) {
			f.delete();
			return true;
		} else {
			return false;
		}
	}

	public static String getFileName(String ref, int picNum) {
		String fileName = LOCATION;
		if (picNum != 1)
			fileName += "pic" + picNum + "/";
		fileName += ref + ".jpg";
		return fileName;
	}
	
	public static String getThumbName(String ref, int picNum) {
		String fileName = LOCATION;
		if (picNum != 1)
			fileName += "pic" + picNum + "/";
		fileName += ref+"thumbnail" + ".jpg";
		return fileName;
	}

	private static void CreateADirectory(String directory) {
		File dir = new File(directory);
		if (!dir.exists()) {
			Log.i(TAG, "Creating Directoy " + dir.toString());
			dir.mkdir();
		}
	}

	public static void CreateDirectories() {
		CreateADirectory(LOCATION);
		for (int i = 2; i <= MAXPHOTOS; i++) { //from 2 to 19
			CreateADirectory(LOCATION + "pic" + i);
		}
  	}
	
	public static int photoCount(String ref) {		
		for (int i = 1; i <= MAXPHOTOS; i++) {
			if(!exists(ref, i)) {
				return i - 1; //return when we find a photo that doesn't exist
			}
		}
		return MAXPHOTOS; //if we made through the loop, all photos must be present.		
	}
	
	//returns the number of photos deleted
	public static int deleteAllPhotos(String ref) {
		int result = 0;
		List<File> files = getAllFiles(ref);
		for (File aFile : files) {
			if (aFile.exists()) {
				result += 1;
				aFile.delete();
			}
		}
		return result;
	}
	
	public static List<File> getAllFiles(String ref) {
		ArrayList<File> result = new ArrayList<File>();
		int numPhotos = photoCount(ref);
		for (int i = 1; i <= numPhotos; i++) {
			result.add(getFile(ref, i));
		}			
	    return result;	
	}
}
