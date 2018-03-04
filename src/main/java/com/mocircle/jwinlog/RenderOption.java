package com.mocircle.jwinlog;

import com.mocircle.jwinlog.parser.DefaultEventXmlParser;
import com.mocircle.jwinlog.parser.EventXmlParser;

public class RenderOption {

	public static RenderOption DEFAULT = new RenderOption();

	private EventXmlParser eventXmlParser = new DefaultEventXmlParser();
	private boolean renderEventXml = true;
	private boolean extractEventInfo = true;
	private int localeId = 0;

	public RenderOption() {
	}

	public EventXmlParser getEventXmlParser() {
		return eventXmlParser;
	}

	public void setEventXmlParser(EventXmlParser eventXmlParser) {
		this.eventXmlParser = eventXmlParser;
	}

	public boolean isRenderEventXml() {
		return renderEventXml;
	}

	public void setRenderEventXml(boolean renderEventXml) {
		this.renderEventXml = renderEventXml;
	}

	public boolean isExtractEventInfo() {
		return extractEventInfo;
	}

	public void setExtractEventInfo(boolean extractEventInfo) {
		this.extractEventInfo = extractEventInfo;
	}

	public int getLocaleId() {
		return localeId;
	}

	public void setLocaleId(int localeId) {
		this.localeId = localeId;
	}

}
