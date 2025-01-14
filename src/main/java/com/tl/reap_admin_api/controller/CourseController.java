package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.dto.ChapterDto;
import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.CourseRsetisDto;
import com.tl.reap_admin_api.dto.VideoDto;
import com.tl.reap_admin_api.dto.VideoResponse;
import com.tl.reap_admin_api.exception.ChapterNotFoundException;
import com.tl.reap_admin_api.exception.CourseNotFoundException;
import com.tl.reap_admin_api.exception.KPAddPlayListToChannelException;
import com.tl.reap_admin_api.exception.KPChannleNotFoundException;
import com.tl.reap_admin_api.exception.KPPlaylistCreationException;
import com.tl.reap_admin_api.exception.KPVideoUploadException;
import com.tl.reap_admin_api.exception.VideoNotFoundException;
import com.tl.reap_admin_api.model.CourseTranslation;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.service.CourseService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.io.IOException;

@RestController
@RequestMapping("/apis/v1/courses")
public class CourseController {

    private final CourseService courseService;
    private Map<String, Integer> exceptionCodeMap;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
        exceptionCodeMap = new HashMap<>();
        int errorCode = 1001;
        exceptionCodeMap.put("CourseNotFoundException", errorCode++);
        exceptionCodeMap.put("ChapterNotFoundException", errorCode++);
        exceptionCodeMap.put("VideoNotFoundException", errorCode++);
        exceptionCodeMap.put("KPAddPlayListToChannelException", errorCode++);
        exceptionCodeMap.put("KPVideoUploadException", errorCode++);
        exceptionCodeMap.put("KPPlaylistCreationException", errorCode++);
        exceptionCodeMap.put("KPChannleNotFoundException", errorCode++);
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CourseDto courseDto) {

        try {
            CourseDto createdCourseDto = courseService.createCourse(courseDto);           
            return new ResponseEntity<>(createdCourseDto, HttpStatus.CREATED);
        }  catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch(AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                     KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                     KPChannleNotFoundException e) {

            String className = e.getClass().getSimpleName();
          
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error creating course: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{uuid}/image")
    public ResponseEntity<CourseDto> uploadCourseImage(@PathVariable UUID uuid, @RequestParam("file") MultipartFile file) {
        try {
            CourseDto updatedCourse = courseService.uploadCourseImage(uuid, file);
            return ResponseEntity.ok(updatedCourse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/batch-update")
    public ResponseEntity<String> batchUpdateCourses(@RequestParam("file") MultipartFile file) {
        try {
            courseService.processBatchUpdate(file.getInputStream());
            return ResponseEntity.ok("Batch update completed successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error processing file: " + e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/batch-update/videos")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public ResponseEntity<?> bulkUploadVideos(@RequestParam("file") MultipartFile file) {
        try {
            courseService.bulkUploadVideos(file.getInputStream());
            return ResponseEntity.ok("Bulk video upload completed successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error processing file: " + e.getMessage());
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                KPChannleNotFoundException e) {
            String className = e.getClass().getSimpleName();
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error during bulk upload: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        try {
            
            List<CourseDto> courses = courseService.getAllCourses();
            return new ResponseEntity<>(courses, HttpStatus.OK);
        } catch(AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CourseDto> getCourseByUuid(@PathVariable UUID uuid) {
        CourseDto courseDto = courseService.getCourseByUuid(uuid);
        return courseDto != null
                ? new ResponseEntity<>(courseDto, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{uuid}/rsetis")
    public ResponseEntity<CourseRsetisDto> getCourseRsetis(@PathVariable UUID uuid) {
        CourseRsetisDto response = courseService.getCourseRsetis(uuid);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{uuid}")
    public  ResponseEntity<?> updateCourse(@PathVariable UUID uuid, @RequestBody CourseDto courseDetails) {
        try {
            CourseDto updatedCourseDto = courseService.updateCourse(uuid, courseDetails);
            return new ResponseEntity<>(updatedCourseDto, HttpStatus.OK);
        } catch(AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
            KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
            KPChannleNotFoundException e) {

            String className = e.getClass().getSimpleName();

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteCourse(@PathVariable UUID uuid) {
        try {
            courseService.deleteCourse(uuid);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                KPChannleNotFoundException e) {

            String className = e.getClass().getSimpleName();

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{uuid}/translations")
    public ResponseEntity<?> addCourseTranslation(
            @PathVariable UUID uuid,
            @RequestBody CourseTranslation translation,
            @RequestParam Long languageId) {
        try {
            Language language = new Language(); // In a real application, you would fetch this from a language service
            language.setId(languageId);
            CourseDto updatedCourseDto = courseService.addCourseTranslation(uuid, translation, languageId);
            return new ResponseEntity<>(updatedCourseDto, HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                KPChannleNotFoundException e) {

            String className = e.getClass().getSimpleName();

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{uuid}/chapters")
    public ResponseEntity<?> addChapterToCourse(@PathVariable UUID uuid, 
            @RequestBody ChapterDto chapterDto) {
        try {
            CourseDto updatedCourse = courseService.addChapterToCourse(uuid, chapterDto);
            return new ResponseEntity<>(updatedCourse, HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                KPChannleNotFoundException e) {

            String className = e.getClass().getSimpleName();

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{courseUuid}/chapters/{chapterUuid}")
    public ResponseEntity<?> updateChapterInCourse(
            @PathVariable UUID courseUuid,
            @PathVariable UUID chapterUuid,
            @RequestBody ChapterDto chapterDto) {
        try {
            CourseDto updatedCourse = courseService.updateChapterInCourse(courseUuid, chapterUuid, chapterDto);
            return new ResponseEntity<>(updatedCourse, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                KPChannleNotFoundException e) {

            String className = e.getClass().getSimpleName();

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{courseUuid}/chapters/{chapterUuid}")
    public ResponseEntity<?> deleteChapterFromCourse(
            @PathVariable UUID courseUuid,
            @PathVariable UUID chapterUuid) {
        try {
            CourseDto updatedCourse = courseService.deleteChapterFromCourse(courseUuid, chapterUuid);
            return new ResponseEntity<>(updatedCourse, HttpStatus.NO_CONTENT);
        }catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                KPChannleNotFoundException e) {

            String className = e.getClass().getSimpleName();

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{courseUuid}/chapters/{chapterUuid}/videos")
    public ResponseEntity<?> addVideoToChapter(
            @PathVariable UUID courseUuid,
            @PathVariable UUID chapterUuid,
            @RequestBody VideoDto videoDto) {
        try {
            VideoResponse response = courseService.addVideoToChapter(courseUuid, chapterUuid, videoDto);
            VideoDto video = response.getVideo();
            return new ResponseEntity<>(video, response.getStatus());
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                KPChannleNotFoundException e) {

            String className = e.getClass().getSimpleName();

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            // Log the exception
            e.printStackTrace();          
            System.out.println("Error adding video to chapter: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{courseUuid}/chapters/{chapterUuid}/videos/{videoUuid}")
    public ResponseEntity<?> deleteVideoFromChapter(
            @PathVariable UUID courseUuid,
            @PathVariable UUID chapterUuid,
            @PathVariable UUID videoUuid) {
        try {
            CourseDto updatedCourse = courseService.deleteVideoFromChapter(courseUuid, chapterUuid, videoUuid);
            return new ResponseEntity<>(updatedCourse, HttpStatus.NO_CONTENT);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                KPChannleNotFoundException e) {

            String className = e.getClass().getSimpleName();

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}