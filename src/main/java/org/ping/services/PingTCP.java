package org.ping.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.ping.services.report.Report;
import org.ping.services.report.ReportPart;

/**
 * 
 * @author knoviseth
 *
 */
public class PingTCP extends AbstractPing implements Callable<String>{

	/**
	 * If multiple instances of PingTCP, forbid the call to the host
	 * by checking the queue.
	 * If empty queue, the current instance inits it and 
	 * runs the call, otherwise, it skips the call.
	 */
	static BlockingQueue<String> concurrentQueueForHost  = new LinkedBlockingQueue<String>();
	
	public PingTCP(Report report, String host, int delay) {
		super(report, host, delay);
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
				
				//at this point , we are sure to be the only PingTCP instance
				// to ping the current host
					//	
					// do something that may cause an exception :-) 	
					int rInt = ThreadLocalRandom.current().nextInt(20);
					if (rInt<3) {
						throw new ArithmeticException(" just throw it");
					}else {
						System.out.println(".t.");
					}
					
					//update the report continuously even if everything is OK 
					updateReport(host, ReportPart.TCP, "tcp OK");
					TimeUnit.SECONDS.sleep(delay);
			
			}catch(Exception ex) {
				System.out.println("\n---- ping tcp issue ! " + ex.getLocalizedMessage());
				reportIssue("one issue ");
				break;
			}finally {
				concurrentQueueForHost.remove(host);
			}
		}
		return "ok"; 
				
	}
	
	private void reportIssue(String what) {
		super.updateReportWithIssue(host, ReportPart.TCP, "tcp FAILED");
	}

}
