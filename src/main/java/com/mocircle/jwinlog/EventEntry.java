package com.mocircle.jwinlog;

import com.mocircle.jwinlog.model.EventType;

public class EventEntry {

	private long recordId;
	private String eventRaw;
	private EventType eventObject;

	public EventEntry() {
	}

	public long getRecordId() {
		return recordId;
	}

	public void setRecordId(long recordId) {
		this.recordId = recordId;
	}

	public String getEventRaw() {
		return eventRaw;
	}

	public void setEventRaw(String eventRaw) {
		this.eventRaw = eventRaw;
	}

	public EventType getEventObject() {
		return eventObject;
	}

	public void setEventObject(EventType eventObject) {
		this.eventObject = eventObject;
	}

}
