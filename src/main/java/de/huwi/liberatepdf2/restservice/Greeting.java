package de.huwi.liberatepdf2.restservice;

public class Greeting {

	private final String content;
	private final long id;

	public Greeting(final long id, final String content) {
		this.id = id;
		this.content = content;
	}

	public String getContent() {
		return this.content;
	}

	public long getId() {
		return this.id;
	}
}
