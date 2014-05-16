package nz.co.salespartner.Helpers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

import com.embarcadero.javaandroid.DBXException;
import com.embarcadero.javaandroid.DSProxy.TServerMethods1;

import android.util.Log;

public class FileDownloader {
	private static String TAG = "FileDownloader";

	public static File downloadFile(String fileName, InputStream is, String directory){

		
		File dirF = new File(directory);
		if(dirF.exists() == false) //make directory if doesn't exist
			dirF.mkdirs();

		File file = new File(directory, fileName);  //make file object at dir and with fileNaame
		if(file.exists() == false)
		{
			try{
				
				file.createNewFile();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(bis.available());  //TODO change capacity?
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}

				FileOutputStream fos = new FileOutputStream(file);
				fos.write(baf.toByteArray()); //TODO fix errors here
				fos.close();
				Log.i("",file.getName()+" file downloaded");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("Listing Doc", "Error: " + e);
			}
		}
		return file;
	}
	
	public static void DownloadFromUrl(String imageURL, String fileName) {
		// this is the downloader method
		try {
			URL url = new URL(imageURL); // you can write here any link
			File file = new File(fileName);

			long startTime = System.currentTimeMillis();
			Log.d("ImageManager", "download begining");
			Log.d("ImageManager", "download url:" + url);
			Log.d("ImageManager", "downloaded file name:" + fileName);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();
			Log.d("ImageManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

		} catch (IOException e) {
			e.printStackTrace();
			Log.d("ImageManager", "Error: " + e);
		}
	}

	public static InputStream getInputStreamFromUrl(String url) {
		Log.i(TAG, "getImputStreamFromUrl");
		InputStream content = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
			HttpClient httpclient = new DefaultHttpClient(httpParams);
			// Execute HTTP Get Request

			HttpResponse response = httpclient.execute(httpGet);
			content = response.getEntity().getContent();
		} catch (Exception e) {
			Log.w(TAG, "Failed to download " + url); 
			e.printStackTrace();
		}
		return content;
	}

	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		Log.i(TAG, "convertStreamToString");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String downloadURLToString(String url) {
		Log.i(TAG, "downloadURLToString: " + url);

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String s = EntityUtils.toString(entity);
			return s;
		} catch (Exception ex) {
			Log.w(TAG, "Failed to download " + url);
			ex.printStackTrace();
			return "";
		}

	}

	public static void DownloadImage(String ref, int picNum, String fileName, TServerMethods1 proxy, int screenW, int screenH) throws IOException {
		File file = new File(fileName); 
		try {
			InputStream is = proxy.GetPicture(ref, picNum, screenW, screenH);
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();
		} 
		catch (DBXException e) {
			e.printStackTrace();
		}	

	}

	/*
	 * InputStream content = getInputStreamFromUrl(url); if (content != null)
	 * return convertStreamToString(content); else return "";
	 * 
	 * }
	 */

}
