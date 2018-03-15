package org.ping.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.ping.services.report.Report;
import org.ping.services.report.ReportPart;

/**
 * 
 * @author knoviseth
 *
 */
public class PingTCP extends AbstractPing implements Callable<String>{
	
	String lastReply = "";
	HttpURLConnection urlConnection;
    URL url;
    int timeOutInMs;
    
	/**
	 * If multiple instances of PingTCP, forbid the call to the host
	 * by checking the queue.
	 * If empty queue, the current instance inits it and 
	 * runs the call, otherwise, it skips the call.
	 */
	static BlockingQueue<String> concurrentQueueForHost  = new LinkedBlockingQueue<String>();
	
	public PingTCP(Report report, String host, int delay, int timeOut) {
		super(report, host, delay);
		this.host = "http://" + host;
		this.timeOutInMs = timeOut;
	}

	@Override
	public String call() throws Exception {
		while (true) {
			try {
				// use the concurrentQueue to be sure no other PingTracer instance tries to 
				// ping the current host
				//
				if (concurrentQueueForHost.contains(host)) {
					continue; 
				} else {
					concurrentQueueForHost.add(host);
				}
				
				//at this point , we are sure to be the only PingTCP instance
				// to ping the current host
					//	
					// let's do this
					long start = System.currentTimeMillis();
				 	url = new URL(host);
				 	urlConnection = (HttpURLConnection) url.openConnection();
				 	urlConnection.setRequestMethod("HEAD");
				 	/* connect Time Out : time out to first connection
				 	 * read    Time Out : time out to retrieve the data
				 	 */
				 	urlConnection.setConnectTimeout(timeOutInMs);
			        int code = urlConnection.getResponseCode();
			        long duration = System.currentTimeMillis() - start;
			        lastReply = "host [" + host + "], time [" + duration + "], statusCode ["+ code +"]" ;
					
					//update the report continuously even if everything is OK 
					updateReport(host, ReportPart.TCP, lastReply);
					TimeUnit.SECONDS.sleep(delay);
			
			} catch(UnknownHostException unknown) {
				reportIssue(" UnknownHostException " + host);
				break;
			} catch(IOException ex) {
				System.out.println("\n---- ping tcp issue ! " + ex.getLocalizedMessage());
				reportIssue(host + " : " +ex.getLocalizedMessage());
				break;
			} finally {
				concurrentQueueForHost.remove(host);
			}
		}// end while
		return "ok TCP"; 
				
	}
	
	private void reportIssue(String what) {
		super.updateReportWithIssue(host, ReportPart.TCP, what);
	}

}
