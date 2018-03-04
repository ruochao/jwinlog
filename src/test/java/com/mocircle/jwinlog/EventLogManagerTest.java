package com.mocircle.jwinlog;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.runner.RunWith;

import com.mocircle.jwinlog.test.WindowsOnlyTestRunner;

//@RunWith(WindowsOnlyTestRunner.class)
public class EventLogManagerTest {

	// @Test
	public void test() throws URISyntaxException {
		File testEvtx = new File(getClass().getClassLoader().getResource("WevtapiTest.sample1.evtx").toURI());
		System.out.println(testEvtx.getPath());
		EventLogManager eventLogMgr = EventLogFactory.createLocalEventLogManager();
		EventEntryIterator iterator = eventLogMgr.retrieveEventLogsFromOldestRecord(testEvtx.getPath(),
				RenderOption.DEFAULT);
		while (iterator.hasNext()) {
			EventEntry event = iterator.next();
			System.out.println(event.getEventRaw());
		}
	}

}
