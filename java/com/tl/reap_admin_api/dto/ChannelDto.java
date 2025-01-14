package com.tl.reap_admin_api.dto;
import java.util.UUID;

public class ChannelDto {
    private UUID uuid;
    private String name;
    private String description;
  
    // Constructors, getters, and setters

    public ChannelDto() {
    }    
    // Getters and setters for all fields

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}