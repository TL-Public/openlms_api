package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.ChannelDto;
import com.tl.reap_admin_api.model.Channel;
import org.springframework.stereotype.Component;

@Component
public class ChannelMapper {

    public ChannelDto toDto(Channel channel) {
        if (channel == null) {
            return null;
        }

        ChannelDto dto = new ChannelDto();
        dto.setUuid(channel.getUuid());
        dto.setName(channel.getName());
        dto.setDescription(channel.getDescription());
        return dto;
    }

    public Channel toEntity(ChannelDto dto) {
        if (dto == null) {
            return null;
        }

        Channel channel = new Channel();
        channel.setUuid(dto.getUuid());
        channel.setName(dto.getName());
        channel.setDescription(dto.getDescription());

        return channel;
    }
}