package de.huwi.liberatepdf2.restservice;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A PDF file
 */
@ToString
public class Pdf {
	/**
	 * File name of the PDF 
	 */
	private String filename;
	
//	/**
//	 * Byte content of the PDF
//	 */
//	private byte[] content;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

//	public byte[] getContent() {
//		return content;
//	}
//
//	public void setContent(byte[] content) {
//		this.content = content;
//	}
}
