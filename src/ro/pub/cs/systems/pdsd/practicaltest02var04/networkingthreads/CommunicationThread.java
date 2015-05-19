package ro.pub.cs.systems.pdsd.practicaltest02var04.networkingthreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import ro.pub.cs.systems.pdsd.practicaltest02var04.general.Constants;
import ro.pub.cs.systems.pdsd.practicaltest02var04.general.Utilities;
import android.util.Log;

public class CommunicationThread extends Thread {
	
	private ServerThread serverThread;
	private Socket       socket;
	
	public CommunicationThread(ServerThread serverThread, Socket socket) {
		this.serverThread = serverThread;
		this.socket       = socket;
	}
	
	@Override
	public void run() {
		if (socket != null) {
			try {
				BufferedReader bufferedReader = Utilities.getReader(socket);
				PrintWriter    printWriter    = Utilities.getWriter(socket);
				if (bufferedReader != null && printWriter != null) {
					Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (url)!");
					String url            = bufferedReader.readLine();
					HashMap<String, String> data = serverThread.getData();
					String pageContentInformation = null;
					if (url != null && !url.isEmpty()) {
						if (data.containsKey(url)) {
							Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
							pageContentInformation = data.get(url);
						} else {
							Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
							
							HttpClient httpClient = new DefaultHttpClient();
							HttpGet httpGet = new HttpGet(url);
							ResponseHandler<String> responseHandler = new BasicResponseHandler();
							
							try {
								final String content = httpClient.execute(httpGet, responseHandler);
								pageContentInformation = content;
								serverThread.setData(url, content);
							} catch (ClientProtocolException clientProtocolException) {
								Log.e(Constants.TAG, clientProtocolException.getMessage());
								if (Constants.DEBUG) {
									clientProtocolException.printStackTrace();
								}
							} catch (IOException ioException) {
								Log.e(Constants.TAG, ioException.getMessage());
								if (Constants.DEBUG) {
									ioException.printStackTrace();
								}
							}
										
						}
						
						if (pageContentInformation != null) {
							printWriter.println(pageContentInformation);
							printWriter.flush();
						} else {
							Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast information is null!");
						}
						
					} else {
						Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (url)!");
					}
				} else {
					Log.e(Constants.TAG, "[COMMUNICATION THREAD] BufferedReader / PrintWriter are null!");
				}
				socket.close();
			} catch (IOException ioException) {
				Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
				if (Constants.DEBUG) {
					ioException.printStackTrace();
				}
			} 
		} else {
			Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
		}
	}

}
