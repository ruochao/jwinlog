package com.mocircle.jwinlog;

import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;

import com.mocircle.jwinlog.api.EventLogApi;
import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Wevtapi;
import com.sun.jna.platform.win32.WevtapiUtil;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Winevt;
import com.sun.jna.platform.win32.Winevt.EVT_HANDLE;
import com.sun.jna.ptr.IntByReference;

/**
 * Hello world!
 *
 */
public class Main {
	
	public static void main(String[] args) throws Exception {
		
		EventLogApi.getLogInfo(null, "Application");				
	}

	public static void main2(String[] args) throws Exception {
		EventLogManager eventLogMgr = EventLogFactory.createLocalEventLogManager();

		RenderOption option = new RenderOption();
		option.setExtractEventInfo(true);
		EventEntryIterator iterator = eventLogMgr.retrieveEventLogsFromOldestRecord("Security", option);
		while (true) {
			while (iterator.hasNext()) {
				EventEntry event = iterator.next();
				System.out.println("Record: " + event.getEventObject().getSystem().getEventRecordID().getValue());

			}

			System.out.println("Wait for next event...");
			Thread.sleep(3000);

			// break;
		}

	}

	private static void getEventLogs(String channelName, Long startRecord) throws JAXBException {
		EVT_HANDLE bookmark = EventLogApi.createBookmark(channelName, startRecord);
		EVT_HANDLE subscriptionHandle = EventLogApi.subscribeEvent(null, bookmark, channelName,
				Winevt.EVT_SUBSCRIBE_FLAGS.EvtSubscribeStartAfterBookmark);

		while (true) {
			EVT_HANDLE[] eventHandles = EventLogApi.handleEvent(subscriptionHandle, 512);
			if (eventHandles == null) {
				break;
			}
			for (EVT_HANDLE eventHandle : eventHandles) {

				break;
			}
		}

		EventLogApi.closeHandle(bookmark);
		EventLogApi.closeHandle(subscriptionHandle);
	}

}
