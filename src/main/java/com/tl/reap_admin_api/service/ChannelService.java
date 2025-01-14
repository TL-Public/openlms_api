package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.ChannelDao;
import com.tl.reap_admin_api.dto.ChannelDto;
import com.tl.reap_admin_api.model.Channel;
import com.tl.reap_admin_api.exception.ChannelNotFoundException;
import com.tl.reap_admin_api.mapper.ChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChannelService {

    private final ChannelDao channelDao;
    private final ChannelMapper channelMapper;

    @Autowired
    public ChannelService(ChannelDao channelDao, ChannelMapper channelMapper) {
        this.channelDao = channelDao;
        this.channelMapper = channelMapper;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public ChannelDto createChannel(ChannelDto channelDto) {
        Channel channel = channelMapper.toEntity(channelDto);
        channel.setUuid(UUID.randomUUID());
        channel.setCreatedAt(ZonedDateTime.now());
        channel.setUpdatedAt(ZonedDateTime.now());
        channel.setCreatedBy("system"); // Replace with actual user when authentication is implemented
        channel.setUpdatedBy("system"); // Replace with actual user when authentication is implemented

        Channel savedChannel = channelDao.save(channel);
        return channelMapper.toDto(savedChannel);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF', 'TRAINER', 'TRAINEE')")
    public ChannelDto getChannelByUuid(UUID uuid) {
        Channel channel = channelDao.findByUuid(uuid)
                .orElseThrow(() -> new ChannelNotFoundException("Channel not found with uuid: " + uuid));
        return channelMapper.toDto(channel);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF', 'TRAINER', 'TRAINEE')")
    public List<ChannelDto> getAllChannels() {
        return channelDao.findAll().stream()
                .map(channelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public ChannelDto updateChannel(UUID uuid, ChannelDto channelDto) {
        Channel existingChannel = channelDao.findByUuid(uuid)
                .orElseThrow(() -> new ChannelNotFoundException("Channel not found with uuid: " + uuid));

        Channel updatedChannel = channelMapper.toEntity(channelDto);
        updatedChannel.setId(existingChannel.getId());
        updatedChannel.setUuid(existingChannel.getUuid());
        updatedChannel.setCreatedAt(existingChannel.getCreatedAt());
        updatedChannel.setCreatedBy(existingChannel.getCreatedBy());
        updatedChannel.setUpdatedAt(ZonedDateTime.now());
        updatedChannel.setUpdatedBy("system"); // Replace with actual user when authentication is implemented

        Channel savedChannel = channelDao.save(updatedChannel);
        return channelMapper.toDto(savedChannel);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public void deleteChannel(UUID uuid) {
        Channel channel = channelDao.findByUuid(uuid)
                .orElseThrow(() -> new ChannelNotFoundException("Channel not found with uuid: " + uuid));
        channelDao.delete(channel);
    }
}