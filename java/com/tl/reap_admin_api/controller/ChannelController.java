package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.dto.ChannelDto;
import com.tl.reap_admin_api.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/apis/v1/channels")
public class ChannelController {

    private final ChannelService channelService;

    @Autowired
    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @PostMapping
    public ResponseEntity<?> createChannel(@RequestBody ChannelDto channelDto) {
        try {
            ChannelDto createdChannel = channelService.createChannel(channelDto);
            return new ResponseEntity<>(createdChannel, HttpStatus.CREATED);
        } catch  (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getChannelByUuid(@PathVariable UUID uuid) {
        try {
            ChannelDto channel = channelService.getChannelByUuid(uuid);
            return new ResponseEntity<>(channel, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllChannels() {
        try {
            List<ChannelDto> channels = channelService.getAllChannels();
            return new ResponseEntity<>(channels, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateChannel(@PathVariable UUID uuid, @RequestBody ChannelDto channelDto) {
        try {
            ChannelDto updatedChannel = channelService.updateChannel(uuid, channelDto);
            return new ResponseEntity<>(updatedChannel, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteChannel(@PathVariable UUID uuid) {
        try {
            channelService.deleteChannel(uuid);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}