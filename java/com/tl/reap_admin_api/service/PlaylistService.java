package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.PlaylistDao;
import com.tl.reap_admin_api.dao.ChannelDao;
import com.tl.reap_admin_api.dto.PlaylistDto;
import com.tl.reap_admin_api.model.Playlist;
import com.tl.reap_admin_api.model.Channel;
import com.tl.reap_admin_api.exception.PlaylistNotFoundException;
import com.tl.reap_admin_api.exception.ChannelNotFoundException;
import com.tl.reap_admin_api.mapper.PlaylistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private final PlaylistDao playlistDao;
    private final ChannelDao channelDao;
    private final PlaylistMapper playlistMapper;

    @Autowired
    public PlaylistService(PlaylistDao playlistDao, ChannelDao channelDao, PlaylistMapper playlistMapper) {
        this.playlistDao = playlistDao;
        this.channelDao = channelDao;
        this.playlistMapper = playlistMapper;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public PlaylistDto createPlaylist(PlaylistDto playlistDto) {
        Playlist playlist = playlistMapper.toEntity(playlistDto);
        playlist.setUuid(UUID.randomUUID());
        playlist.setCreatedAt(ZonedDateTime.now());
        playlist.setUpdatedAt(ZonedDateTime.now());
        playlist.setCreatedBy("system"); // Replace with actual user when authentication is implemented
        playlist.setUpdatedBy("system"); // Replace with actual user when authentication is implemented

        if (playlistDto.getChannelId() != null) {
            Channel channel = channelDao.findById(playlistDto.getChannelId())
                    .orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + playlistDto.getChannelId()));
            playlist.setChannel(channel);
        }

        Playlist savedPlaylist = playlistDao.save(playlist);
        return playlistMapper.toDto(savedPlaylist);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF', 'TRAINER', 'TRAINEE')")
    public PlaylistDto getPlaylistByUuid(UUID uuid) {
        Playlist playlist = playlistDao.findByUuid(uuid)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found with uuid: " + uuid));
        return playlistMapper.toDto(playlist);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF', 'TRAINER', 'TRAINEE')")
    public List<PlaylistDto> getAllPlaylists() {
        return playlistDao.findAll().stream()
                .map(playlistMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public PlaylistDto updatePlaylist(UUID uuid, PlaylistDto playlistDto) {
        Playlist existingPlaylist = playlistDao.findByUuid(uuid)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found with uuid: " + uuid));

        Playlist updatedPlaylist = playlistMapper.toEntity(playlistDto);
        updatedPlaylist.setId(existingPlaylist.getId());
        updatedPlaylist.setUuid(existingPlaylist.getUuid());
        updatedPlaylist.setCreatedAt(existingPlaylist.getCreatedAt());
        updatedPlaylist.setCreatedBy(existingPlaylist.getCreatedBy());
        updatedPlaylist.setUpdatedAt(ZonedDateTime.now());
        updatedPlaylist.setUpdatedBy("system"); // Replace with actual user when authentication is implemented

        if (playlistDto.getChannelId() != null) {
            Channel channel = channelDao.findById(playlistDto.getChannelId())
                    .orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + playlistDto.getChannelId()));
            updatedPlaylist.setChannel(channel);
        }

        Playlist savedPlaylist = playlistDao.save(updatedPlaylist);
        return playlistMapper.toDto(savedPlaylist);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public void deletePlaylist(UUID uuid) {
        Playlist playlist = playlistDao.findByUuid(uuid)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found with uuid: " + uuid));
        playlistDao.delete(playlist);
    }
}
