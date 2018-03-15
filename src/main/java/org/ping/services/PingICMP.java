package org.ping.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.ping.services.report.Report;
import org.ping.services.report.ReportPart;

public class PingICMP extends AbstractPing implements Callable<String>{

	ProcessBuilder processBuilder;
    String reply=null;
    String lastReply="";
    
	/**
	 * If multiple instances of PingICMP, forbid the call to the host
	 * by checking the queue.
	 * If empty queue, the current instance inits it and 
	 * runs the call, otherwise, it skips the call.
	 */
	static BlockingQueue<String> concurrentQueueForHost  = new LinkedBlockingQueue<String>();
	
	public PingICMP(Report report, String host, int delay) {
		super(report, host, delay);
		this.processBuilder=new ProcessBuilder("ping","-n",host);
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
				}else {
					concurrentQueueForHost.add(host);
				}
				
				//at this point , we are sure to be the only PingICMP instance
				// to ping the current host
				
					// let's do this 
					Process process = processBuilder.start();
	                InputStream inputStream = process.getInputStream();
	                BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(inputStream));

	                while ((reply = bufferedInputStream.readLine()) != null) {
	                    
	                		if (!(reply.contains("bytes") || reply.contains("Pinging") || reply.contains(""))) {
	                			reportIssue(reply);
	                        break;
	                    }
	                   
	                    if(reply.contains("failure") || reply.contains("timeout")) {
	                    		reportIssue(reply);
	                    		break;
	                    }
	                    
	                    if (reply.contains("bytes")) {
//	                        System.out.println(reply);
	                        lastReply = reply;
	                    }
	                    //update the report continuously even if everything is OK 
						updateReport(host, ReportPart.ICMP, lastReply);
						TimeUnit.SECONDS.sleep(delay);
						
	                } // end while reading
	                
	                if (reply==null || reply.contains("could not find")) {
	                		reportIssue("no answer from host " + host);
	                    break;
	                }

	                System.out.println("Last reply from " + this.host + " was this: " + lastReply);
					
	                // something happened. The ping failed or the host is unknown
	                // wait a moment and rerun the ping process
					try {TimeUnit.SECONDS.sleep(delay);} catch (Exception e) {e.printStackTrace();}
			
			}catch(Exception ex) {
				System.out.println("\n----ping icmp issue! : " + ex.getLocalizedMessage());
				reportIssue(lastReply);
				break;
			}finally {
				concurrentQueueForHost.remove(host);
			}
		} // end while
		return "ok"; 
				
	}
	
	private void reportIssue(String what) {
		super.updateReportWithIssue(host, ReportPart.ICMP, what);
	}

}
