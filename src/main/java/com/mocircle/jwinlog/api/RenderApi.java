package com.mocircle.jwinlog.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mocircle.jwinlog.EventEntry;
import com.mocircle.jwinlog.RenderOption;
import com.mocircle.jwinlog.model.EventType;
import com.mocircle.jwinlog.parser.EventXmlParser;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.Winevt.EVT_HANDLE;

public final class RenderApi {

	private static final Logger logger = LoggerFactory.getLogger(RenderApi.class);

	private RenderApi() {
	}

	public static EventEntry render(EVT_HANDLE session, EVT_HANDLE eventHandle, RenderOption option) {
		if (option == null) {
			option = new RenderOption();
		}

		EventEntry event = new EventEntry();

		// For event xml rendering
		String eventRaw = null;
		if (option.isRenderEventXml()) {
			try {
				eventRaw = EventLogApi.formatEventXml(session, eventHandle, option.getLocaleId());
			} catch (Win32Exception e) {
				logger.warn("Failed to format event xml content, skip and keep original content.", e);
				eventRaw = EventLogApi.renderEventXml(eventHandle);
			}
		} else {
			eventRaw = EventLogApi.renderEventXml(eventHandle);
		}
		eventRaw = eventRaw.trim();
		event.setEventRaw(eventRaw);

		// For event object parsing
		if (eventRaw != null && option.isExtractEventInfo()) {
			EventXmlParser parser = option.getEventXmlParser();
			if (parser != null) {
				EventType eventObj = parser.parse(eventRaw);
				event.setEventObject(eventObj);
			} else {
				logger.warn("Event XML parser is null");
			}
		}

		// For record id
		Long recordId = null;
		if (option.isExtractEventInfo()) {
			if (event.getEventObject() != null && event.getEventObject().getSystem() != null
					&& event.getEventObject().getSystem().getEventRecordID() != null
					&& event.getEventObject().getSystem().getEventRecordID().getValue() != null) {
				recordId = event.getEventObject().getSystem().getEventRecordID().getValue().longValue();
			}
		}
		if (recordId == null) {
			recordId = EventLogApi.getEventRecordId(eventHandle);
		}
		event.setRecordId(recordId);

		return event;
	}

}
