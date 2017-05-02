package de.huwi.liberatepdf2.restservice;

import org.springframework.web.multipart.MultipartFile;

import lombok.ToString;

/**
 * A PDF file
 */
@ToString
public class PdfsDTO {
	/**
	 * Byte content of the PDF
	 */
	private MultipartFile[] files;

	/**
	 * File name of the PDF
	 */
	private String password;

	public MultipartFile[] getFiles() {
		return this.files;
	}

	public String getPassword() {
		return this.password;
	}

	public void setFiles(final MultipartFile[] files) {
		this.files = files;
	}

	public void setPassword(final String password) {
		this.password = password;
	}
}