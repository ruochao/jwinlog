package com.mocircle.jwinlog.parser;

import com.mocircle.jwinlog.model.EventType;

public interface EventXmlParser {

	EventType parse(String eventXml);

}
