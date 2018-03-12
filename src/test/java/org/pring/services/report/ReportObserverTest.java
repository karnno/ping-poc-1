package org.pring.services.report;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ping.services.report.Report;
import org.ping.services.report.ReportObserver;

public class ReportObserverTest {

	
	@Test
	public void testReportObserver() {
		final String FAKE_HOST = "host";
		final String FAKE_URL  = "url";
		// create a report 
		Report aReport = new Report(FAKE_HOST);
		
		List<ReportObserver> listReportObserver = java.util.Arrays.asList(
				new ReportObserver("ro1", FAKE_URL, false), 
				new ReportObserver("ro2", FAKE_URL, false));
		
		listReportObserver.forEach(o -> aReport.addObserver(o));
		
		
		aReport.setIcmp("testIcmp");
		aReport.setTcp("testTcp");
		aReport.setTracer("testTracer");
		
		String jsonInitial = aReport.getJson();
		aReport.wakeObservers();
		
		listReportObserver.forEach(o -> Assert.assertTrue(jsonInitial.equals(o.getJsonReport()) ) );
	}
}
