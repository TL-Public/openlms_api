package com.tl.reap_admin_api.dto;

import java.util.UUID;

public class BankDto {
	private UUID uuid;
	private String name;

	// Default constructor
	public BankDto() {
	}

	// Constructor with all fields
	public BankDto(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}

	// Getters and setters

	public String getName() {
		return name;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public void setName(String name) {
		this.name = name;
	}

	// You might want to override toString(), equals(), and hashCode() methods
	@Override
	public String toString() {
		return "BankDTO{" + "uuid=" + uuid + ", name='" + name + '\'' + '}';
	}
}