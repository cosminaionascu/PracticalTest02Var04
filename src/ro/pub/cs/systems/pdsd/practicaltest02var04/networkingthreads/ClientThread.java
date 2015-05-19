package ro.pub.cs.systems.pdsd.practicaltest02var04.networkingthreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.pdsd.practicaltest02var04.general.Constants;
import ro.pub.cs.systems.pdsd.practicaltest02var04.general.Utilities;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

public class ClientThread extends Thread {
	
	private String   address;
	private int      port;
	private String   url;
	private TextView pageContent;
	private WebView resultWebView;
	
	private Socket   socket;
	
	public ClientThread(
			String address,
			int port,
			String url,
			TextView pageContent,
			WebView resWebView) {
		this.address                 = address;
		this.port                    = port;
		this.url                    = url;
		this.pageContent = pageContent;
		this.resultWebView = resWebView;
	}
	
	@Override
	public void run() {
		try {
			socket = new Socket(address, port);
			if (socket == null) {
				Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
			}
			
			BufferedReader bufferedReader = Utilities.getReader(socket);
			PrintWriter    printWriter    = Utilities.getWriter(socket);
			if (bufferedReader != null && printWriter != null) {
				printWriter.println(url);
				printWriter.flush();
				String pageContentInfo;
				final StringBuffer resultView = new StringBuffer();
				while ((pageContentInfo = bufferedReader.readLine()) != null) {
					final String finalizedpageContentInfo = pageContentInfo;
					resultView.append(finalizedpageContentInfo);
					
					pageContent.post(new Runnable() {
						@Override
						public void run() {
							pageContent.append(finalizedpageContentInfo + "\n");
						}
					});
				}
				
				resultWebView.post(new Runnable() {
					@Override
					public void run() {
						resultWebView.loadDataWithBaseURL(url, resultView.toString(), Constants.MIME_TYPE, Constants.CHARACTER_ENCODING, null);
					}
				});
				
			} else {
				Log.e(Constants.TAG, "[CLIENT THREAD] BufferedReader / PrintWriter are null!");
			}
			socket.close();
		} catch (IOException ioException) {
			Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
			if (Constants.DEBUG) {
				ioException.printStackTrace();
			}
		}
	}

}
