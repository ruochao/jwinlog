package com.mocircle.jwinlog.parser;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mocircle.jwinlog.model.EventType;
import com.mocircle.jwinlog.model.ObjectFactory;

public class DefaultEventXmlParser implements EventXmlParser {

	private static final Logger logger = LoggerFactory.getLogger(DefaultEventXmlParser.class);
	private static Unmarshaller jaxbUnmarshaller;

	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException("Cannot initialize JAXB binding object", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public EventType parse(String eventXml) {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(eventXml.getBytes())) {
			JAXBElement<EventType> jaxb = (JAXBElement<EventType>) jaxbUnmarshaller.unmarshal(bais);
			return jaxb.getValue();
		} catch (Exception e) {
			logger.warn("Failed to parse event data", e);
			return null;
		}
	}

}
