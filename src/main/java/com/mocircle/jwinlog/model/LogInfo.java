package com.mocircle.jwinlog.model;

import java.util.Date;

public class LogInfo {

	private Date creationTime;
	private Date lastAccessTime;
	private Date lastWriteTime;
	private long fileSize;
	private int logAttributes;
	private long totalRecordNumber;
	private long oldestRecordNumber;
	private boolean isFull;

	public LogInfo() {
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(Date lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public Date getLastWriteTime() {
		return lastWriteTime;
	}

	public void setLastWriteTime(Date lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int getLogAttributes() {
		return logAttributes;
	}

	public void setLogAttributes(int logAttributes) {
		this.logAttributes = logAttributes;
	}

	public long getTotalRecordNumber() {
		return totalRecordNumber;
	}

	public void setTotalRecordNumber(long totalRecordNumber) {
		this.totalRecordNumber = totalRecordNumber;
	}

	public long getOldestRecordNumber() {
		return oldestRecordNumber;
	}

	public void setOldestRecordNumber(long oldestRecordNumber) {
		this.oldestRecordNumber = oldestRecordNumber;
	}

	public boolean isFull() {
		return isFull;
	}

	public void setFull(boolean isFull) {
		this.isFull = isFull;
	}

}
