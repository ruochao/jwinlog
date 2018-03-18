package com.mocircle.jwinlog;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mocircle.jwinlog.model.LogInfo;
import com.mocircle.jwinlog.test.WindowsOnlyTestRunner;

@RunWith(WindowsOnlyTestRunner.class)
public class EventLogManagerTest {

	@Test
	public void testRetrieveEvents() throws URISyntaxException {
		tryTestEventParsingAndSize("Application");
		tryTestEventParsingAndSize("System");
		tryTestEventParsingAndSize("Security");
	}

	private void tryTestEventParsingAndSize(String channelName) {
		EventLogManager eventLogMgr = EventLogFactory.createLocalEventLogManager();
		EventEntryIterator iterator = eventLogMgr.retrieveEventLogsFromOldestRecord(channelName, RenderOption.DEFAULT);
		LogInfo infoBefore = eventLogMgr.getLogInfo(channelName);
		int size = 0;
		while (iterator.hasNext()) {
			EventEntry event = iterator.next();
			Assert.assertNotNull(event);
			Assert.assertNotNull(event.getEventRaw());
			Assert.assertNotNull(event.getEventObject());
			size++;
		}
		LogInfo infoAfter = eventLogMgr.getLogInfo(channelName);
		if (infoBefore.getTotalRecordNumber() == infoAfter.getTotalRecordNumber()) {
			Assert.assertEquals(infoAfter.getTotalRecordNumber(), size);
		}
	}

}
