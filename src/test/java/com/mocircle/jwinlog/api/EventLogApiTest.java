package com.mocircle.jwinlog.api;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mocircle.jwinlog.test.WindowsOnlyTestRunner;

@RunWith(WindowsOnlyTestRunner.class)
public class EventLogApiTest {

	@Test
	public void testGetChannels() {
		String[] channels = EventLogApi.getChannels(null);
		Assert.assertTrue(channels.length > 0);
		List<String> channelList = Arrays.asList(channels);
		Assert.assertTrue(channelList.contains("Application"));
		Assert.assertTrue(channelList.contains("System"));
		Assert.assertTrue(channelList.contains("Security"));
	}

	@Test
	public void testGetPublishers() {
		String[] publishers = EventLogApi.getPublishers(null);
		Assert.assertTrue(publishers.length > 0);
	}

}
