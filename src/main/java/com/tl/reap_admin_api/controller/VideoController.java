package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.dto.VideoDto;
import com.tl.reap_admin_api.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@RestController
@RequestMapping("/apis/v1/videos")
public class VideoController {

    private final VideoService videoService;

    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    public ResponseEntity<?> getAllVideos(
        @RequestParam(required = false) String courseName,
        @RequestParam(required = false) String courseCode,
        @RequestParam(required = false) String videoTitle,
        @RequestParam(required = false) String courseUuid,
        Pageable pageable) {
        try {

            UUID crsUUID = (courseUuid == null) ? null : UUID.fromString(courseUuid);
            Page<VideoDto> videos = videoService.getAllVideos(courseName, courseCode, videoTitle, crsUUID, pageable);
            return ResponseEntity.ok(videos);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{uuid}")
    public ResponseEntity<?> getVideoByUuid(@PathVariable UUID uuid) {
        try {
            VideoDto video = videoService.getVideoByUuid(uuid);
            return ResponseEntity.ok(video);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<?> createVideo(@RequestBody VideoDto videoDto) {
        try {
            VideoDto createdVideo = videoService.createVideo(videoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVideo);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateVideo(@PathVariable UUID uuid, @RequestBody VideoDto videoDto) {
        try {
            VideoDto updatedVideo = videoService.updateVideo(uuid, videoDto);
            return ResponseEntity.ok(updatedVideo);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteVideo(@PathVariable UUID uuid) {
        try {
            videoService.deleteVideo(uuid);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/kpoint/playlists")
    public ResponseEntity<?> deleteAllKPointPlaylist() {
        try {
            videoService.deleteAllKPointPlaylist();
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/kpoint/videos")
    public ResponseEntity<?> deleteAllKPointVideos() {
        try {
            videoService.deleteAllKPointVideos();
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}