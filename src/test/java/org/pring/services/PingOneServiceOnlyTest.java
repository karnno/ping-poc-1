package org.pring.services;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.ping.services.PingICMP;
import org.ping.services.PingTCP;
import org.ping.services.PingTRACER;
import org.ping.services.report.Report;


public class PingOneServiceOnlyTest {

	@Test
	public void testPingIcmpOnly() {
		 
		String   host = "google.com";
		int      delay = 1;
		Report   oneReport = new Report(host);
		PingICMP icmp1 = new PingICMP(oneReport, host, delay);
		 
		//run the ping threads
		ExecutorService executor = Executors.newFixedThreadPool(1);
		
		try {
			Future<String> future =executor.submit(icmp1);
			 
                try{
                    System.out.println("future: call ="+future.get(/*timeout*/ 3, TimeUnit.SECONDS ));
                }
                catch (CancellationException ce) {
//                    ce.printStackTrace();
                } catch (ExecutionException ee) {
//                    ee.printStackTrace();
                } catch (TimeoutException toe) {
//                    toe.printStackTrace();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // ignore/reset
                }finally {
                	Assert.assertTrue(!oneReport.getIcmp().isEmpty());
            		Assert.assertTrue(oneReport.getTcp().isEmpty());
            		Assert.assertTrue(oneReport.getTracer().isEmpty());
                }
			
		} catch (Exception excp) {
			excp.printStackTrace();
			 
		}
		executor.shutdown();
	}
	
	@Test
	public void testPingTcpOnly() {
		 
		String   host = "google.com";
		int      delay = 1;
		Report   oneReport = new Report(host);
		int      timeOutInMs = 5000;
		PingTCP  icmp1 = new PingTCP(oneReport, host, delay, timeOutInMs);
		 
		//run the ping threads
		ExecutorService executor = Executors.newFixedThreadPool(1);
		
		try {
			Future<String> future =executor.submit(icmp1);
			 
                try{
                    System.out.println("future: call ="+future.get(/*timeout*/ 3, TimeUnit.SECONDS ));
                }
                catch (CancellationException ce) {
//                    ce.printStackTrace();
                } catch (ExecutionException ee) {
//                    ee.printStackTrace();
                } catch (TimeoutException toe) {
//                    toe.printStackTrace();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // ignore/reset
                }finally {
                		Assert.assertTrue(oneReport.getIcmp().isEmpty());
                		Assert.assertTrue(!oneReport.getTcp().isEmpty());
                		Assert.assertTrue(oneReport.getTracer().isEmpty());
                }
			
		} catch (Exception excp) {
			excp.printStackTrace();
			 
		}
		executor.shutdown();
	}
	
	@Test
	public void testPingTracerOnly() {
		 
		String   host = "hostMain";
		int      delay = 1;
		Report   oneReport = new Report(host);
		PingTRACER  icmp1 = new PingTRACER(oneReport, host, delay);
		 
		//run the ping threads
		ExecutorService executor = Executors.newFixedThreadPool(1);
		
		try {
			Future<String> future =executor.submit(icmp1);
			 
                try{
                    System.out.println("future: call ="+future.get(/*timeout*/ 3, TimeUnit.SECONDS ));
                }
                catch (CancellationException ce) {
//                    ce.printStackTrace();
                } catch (ExecutionException ee) {
//                    ee.printStackTrace();
                } catch (TimeoutException toe) {
//                    toe.printStackTrace();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // ignore/reset
                }finally {
                		Assert.assertTrue(oneReport.getIcmp().isEmpty());
                		Assert.assertTrue(oneReport.getTcp().isEmpty());
                		Assert.assertTrue(!oneReport.getTracer().isEmpty());
                }
			
		} catch (Exception excp) {
			excp.printStackTrace();
			 
		}
		executor.shutdown();
	}
}
