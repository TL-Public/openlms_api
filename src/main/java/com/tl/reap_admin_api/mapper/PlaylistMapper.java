package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.PlaylistDto;
import com.tl.reap_admin_api.model.Playlist;
import com.tl.reap_admin_api.model.Channel;
import org.springframework.stereotype.Component;

@Component
public class PlaylistMapper {

    public PlaylistDto toDto(Playlist playlist) {
        if (playlist == null) {
            return null;
        }

        PlaylistDto dto = new PlaylistDto();
        dto.setUuid(playlist.getUuid());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setChannelId(playlist.getChannel() != null ? playlist.getChannel().getId() : null);
 
        return dto;
    }

    public Playlist toEntity(PlaylistDto dto) {
        if (dto == null) {
            return null;
        }

        Playlist playlist = new Playlist();
        playlist.setUuid(dto.getUuid());
        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        if (dto.getChannelId() != null) {
            Channel channel = new Channel();
            channel.setId(dto.getChannelId());
            playlist.setChannel(channel);
        }
 
        return playlist;
    }
}