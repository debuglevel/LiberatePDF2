package de.huwi.liberatepdf2.restservice;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A PDF file
 */
@ToString
public class PdfsDTO {
	/**
	 * File name of the PDF 
	 */
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public MultipartFile[] getFiles() {
		return files;
	}

	public void setFiles(MultipartFile[] files) {
		this.files = files;
	}

	/**
	 * Byte content of the PDF
	 */
	private MultipartFile[] files;
}