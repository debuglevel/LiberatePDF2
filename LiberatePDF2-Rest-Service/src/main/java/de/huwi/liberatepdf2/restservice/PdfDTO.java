package de.huwi.liberatepdf2.restservice;

import org.springframework.web.multipart.MultipartFile;

import lombok.ToString;

/**
 * A PDF file
 */
@ToString
public class PdfDTO {
	/**
	 * Byte content of the PDF
	 */
	private MultipartFile file;

	/**
	 * File name of the PDF
	 */
	private String password;

	public MultipartFile getFile() {
		return this.file;
	}

	public String getPassword() {
		return this.password;
	}

	public void setFile(final MultipartFile file) {
		this.file = file;
	}

	public void setPassword(final String password) {
		this.password = password;
	}
}