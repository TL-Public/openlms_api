package com.tl.reap_admin_api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.tl.reap_admin_api.dao.ChannelDao;
import com.tl.reap_admin_api.dao.ChapterDao;
import com.tl.reap_admin_api.dao.CourseDao;
import com.tl.reap_admin_api.dao.PlaylistDao;
import com.tl.reap_admin_api.model.Chapter;
import com.tl.reap_admin_api.model.ChapterTranslation;
import com.tl.reap_admin_api.model.ChapterVideo;
import com.tl.reap_admin_api.dto.ChapterDto;
import com.tl.reap_admin_api.dto.ChapterTranslationDto;
import com.tl.reap_admin_api.dto.ChapterVideoDto;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.CourseRsetisDto;
import com.tl.reap_admin_api.dto.CourseTranslationDto;
import com.tl.reap_admin_api.dto.VideoDto;
import com.tl.reap_admin_api.dto.VideoResponse;
import com.tl.reap_admin_api.model.CourseTranslation;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.model.Playlist;
import com.tl.reap_admin_api.model.RsetiCourse;
import com.tl.reap_admin_api.dao.RsetiCourseDao;
import com.tl.reap_admin_api.model.User;
import com.tl.reap_admin_api.model.Video;
import com.tl.reap_admin_api.model.Channel;
import com.tl.reap_admin_api.exception.ChapterNotFoundException;
import com.tl.reap_admin_api.exception.CourseNotFoundException;
import com.tl.reap_admin_api.exception.DuplicateCourseCodeException;
import com.tl.reap_admin_api.exception.LanguageNotFoundException;
import com.tl.reap_admin_api.exception.InvalidCourseDataException;
import com.tl.reap_admin_api.exception.VideoNotFoundException;
import com.tl.reap_admin_api.mapper.CourseMapper;
import com.tl.reap_admin_api.mapper.ChapterMapper;
import com.tl.reap_admin_api.dao.VideoDao;
import com.tl.reap_admin_api.mapper.VideoMapper;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.tl.reap_admin_api.exception.KPAddPlayListToChannelException;
import com.tl.reap_admin_api.exception.KPVideoUploadException;
import com.tl.reap_admin_api.exception.KPPlaylistCreationException;
import com.tl.reap_admin_api.exception.KPChannleNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Optional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
@Transactional
public class CourseService {

    private final CourseDao courseDao;
    private final CourseMapper courseMapper;
    private final LanguageService languageService;
    private final ChapterMapper chapterMapper;
    private final ChapterDao chapterDao;
    private final ChannelDao channelDao;
    private final PlaylistDao playlistDao;
    private final VideoDao videoDao;
    private final VideoMapper videoMapper;
    private final KPointService kPointService;
    private final UserService userService;   
    private RsetiCourseDao rsetiCourseDao; 
    private final AmazonS3 amazonS3Client;

    @Value("${aws.s3.crsimg.bucket-name}")
    private String crsImgBucketName;

    @Value("${aws.s3.crsimg.bucket-url}")
    private String crsImgBucketUrl;
    
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
       
    
        @Autowired
        public CourseService(CourseDao courseDao, ChapterDao chapterDao, VideoDao videoDao, 
                             ChannelDao channelDao, PlaylistDao playlistDao, 
                             CourseMapper courseMapper, VideoMapper videoMapper, LanguageService languageService,
                             ChapterMapper chapterMapper, KPointService kPointService, UserService userService, AmazonS3 amazonS3Client, RsetiCourseDao rsetiCourseDao) {   
            this.courseDao = courseDao;
            this.chapterDao = chapterDao;
            this.videoDao = videoDao;
            this.channelDao = channelDao;
            this.playlistDao = playlistDao;
            this.courseMapper = courseMapper;
            this.videoMapper = videoMapper;
            this.languageService = languageService;
            this.chapterMapper = chapterMapper;
            this.kPointService = kPointService;  
            this.userService = userService;     
            this.amazonS3Client = amazonS3Client;
            this.rsetiCourseDao = rsetiCourseDao;
        }
    
    
        private void validateCourseData(CourseDto courseDto) {
            if (courseDto.getCourseCode() == null || courseDto.getCourseCode().trim().isEmpty()) {
                throw new InvalidCourseDataException("Course code cannot be null or empty");
            }
            if (courseDto.getTranslations() == null || courseDto.getTranslations().isEmpty()) {
                throw new InvalidCourseDataException("At least one translation must be provided");
            }
            for (CourseTranslationDto translation : courseDto.getTranslations()) {
                if (translation.getTitle() == null || translation.getTitle().trim().isEmpty()) {
                    throw new InvalidCourseDataException("Course translation title cannot be null or empty");
                }
                if (translation.getLanguageCode() == null) {
                    throw new InvalidCourseDataException("Language code must be provided for each translation");
                }
            }
            // Add more validations as needed
        }
    
        @Transactional
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
        public CourseDto createCourse(CourseDto courseDto) {
            
            try {
                validateCourseData(courseDto);
                Course course = courseMapper.toEntity(courseDto);
                course.setUuid(UUID.randomUUID());
                course.setCreatedAt(ZonedDateTime.now());
                course.setUpdatedAt(ZonedDateTime.now());
                User currentUser = userService.getCurrentUser();
                course.setCreatedBy(currentUser.getUsername());
                course.setUpdatedBy(currentUser.getUsername());
    
                //Check whether a course aleady exists for the course code
                
                Optional<Course> existingCourse = courseDao.findByCourseCode(courseDto.getCourseCode());
                if(existingCourse.isPresent()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Course with course code " +courseDto.getCourseCode() +" already exists" );
                }
                
                // Save the course first
                Course savedCourse = courseDao.save(course);
                Channel channel = getOrCreateChannel(course);
    
                for (CourseTranslation translation : savedCourse.getTranslations()) {
                    if(translation.getAboutVideoUrl() == null || translation.getAboutVideoUrl().isEmpty()) {
                        continue;
                    }
                    VideoDto videoDto = new VideoDto();
                    videoDto.setUrl(translation.getAboutVideoUrl());
                    videoDto.setLanguageCode(translation.getLanguage().getCode());
                    videoDto = addVideoToCourse(savedCourse, channel, videoDto);
                    translation.setAboutVideoExtid(videoDto.getExtId());
                }
    
                if(savedCourse.getChapters() != null && !savedCourse.getChapters().isEmpty()) {
                    savedCourse.getChapters().forEach(chapter -> {
                        chapter.setUuid(UUID.randomUUID());
                        chapter.setUpdatedAt(ZonedDateTime.now());
                        chapter.setUpdatedBy(userService.getCurrentUser().getUsername());
                    });  
                }
    
                // Save the course again to update with new information
                savedCourse = courseDao.save(savedCourse);
            
                return courseMapper.toDto(savedCourse);
            } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | 
                     KPAddPlayListToChannelException | KPVideoUploadException | KPPlaylistCreationException | 
                     KPChannleNotFoundException |ResponseStatusException e) {
                throw e;
            }
            catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                throw new DuplicateCourseCodeException("createCourse::Course with code " + courseDto.getCourseCode() + " already exists");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error creating course", e);
            }
        }
    
        @Transactional
        public CourseDto uploadCourseImage(UUID courseUuid, MultipartFile file) throws IOException {
            try {
                Course course = courseDao.findByUuid(courseUuid)
                        .orElseThrow(() -> new RuntimeException("Course not found with UUID: " + courseUuid));
                course.setUpdatedAt(ZonedDateTime.now());
                User currentUser = userService.getCurrentUser();
                course.setUpdatedBy(currentUser.getUsername());
                
    
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String key = "courses/" + course.getCourseCode() + ".png";
    
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
    
                amazonS3Client.putObject(new PutObjectRequest(crsImgBucketName, key, file.getInputStream(), metadata));
    
                String imageUrl = amazonS3Client.getUrl(crsImgBucketName, key).toString();
                course.setImageUrl(imageUrl);
    
                Course updatedCourse = courseDao.save(course);
                return courseMapper.toDto(updatedCourse);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error uploading course image", e);
            }
        }
    
        private String getFileExtension(String filename) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
    
        @Transactional(readOnly = true)
        public List<CourseDto> getAllCourses() {
            try {
                List<Course> courses = courseDao.findAll();
                return courseMapper.toDtoList(courses);            
            } catch (Exception e) {
                throw new RuntimeException("Error retrieving all courses", e);
            }
        }
    
        @Transactional(readOnly = true)
        public CourseDto getCourseByUuid(UUID uuid) {
            try {
                Course course = courseDao.findByUuid(uuid)
                        .orElseThrow(() -> new CourseNotFoundException("Course not found with UUID: " + uuid));
                return courseMapper.toDto(course);
            } catch (CourseNotFoundException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error retrieving course by UUID: " + uuid, e);
            }
        }
    
        @Transactional(readOnly = true)
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
        public CourseDto getCourseById(Long id) {
            try {
                Course course = courseDao.findById(id)
                        .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));
                return courseMapper.toDto(course);
            } catch (CourseNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Error retrieving course by id: " + id, e);
            }
        }
    
        @Transactional(readOnly = true)
        public CourseRsetisDto getCourseRsetis(UUID courseUuid) {
            Course course = courseDao.findByUuid(courseUuid)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with UUID: " + courseUuid));

            List<RsetiCourse> rsetiCourses = rsetiCourseDao.findByCourse(course);

            List<CourseRsetisDto.RsetiCourseInfo> rsetiCourseInfos = rsetiCourses.stream()
                    .map(rc -> new CourseRsetisDto.RsetiCourseInfo(rc.getRseti().getUuid(), rc.getStartDate()))
                    .collect(Collectors.toList());

            return new CourseRsetisDto(rsetiCourseInfos);
        }
    
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public CourseDto updateCourse(UUID uuid, CourseDto courseDto) {
        try {
            Course existingCourse = courseDao.findByUuid(uuid)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with UUID: " + uuid));
            existingCourse.setUpdatedAt(ZonedDateTime.now());
            User currentUser = userService.getCurrentUser();
            existingCourse.setUpdatedBy(currentUser.getUsername());

             // Update basic course information
             updateBasicCourseInfo(existingCourse, courseDto);
           
            if (courseDto.getTranslations() != null) {
               updateCourseTranslations(existingCourse, courseDto);              
            }
           
            if(courseDto.getChapters() != null) {
                updateCourseChapters(existingCourse, courseDto);               
            }
            Course updatedCourse = courseDao.save(existingCourse);
            return courseMapper.toDto(updatedCourse);
        } catch (CourseNotFoundException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCourseCodeException("Course with code " + courseDto.getCourseCode() + " already exists");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating course with UUID: " + uuid, e);
        }
    }

    private void updateBasicCourseInfo(Course existingCourse, CourseDto courseDto) {
        if (courseDto.getCourseCode() != null) {
            existingCourse.setCourseCode(courseDto.getCourseCode());
        }
        if (courseDto.getDuration() != null) {
            existingCourse.setDuration(courseDto.getDuration());
        }
        if (courseDto.getImageUrl() != null) {
            existingCourse.setImageUrl(courseDto.getImageUrl());
        }
        if (courseDto.getCategory() != null) {
            existingCourse.setCategory(courseDto.getCategory());
        }
        if (courseDto.getStatus() != null) {
            existingCourse.setStatus(courseDto.getStatus());
        }
        
        if (courseDto.getDisplayCourseCode() != null) {
            existingCourse.setDisplayCourseCode(courseDto.getDisplayCourseCode());
        }
        existingCourse.setUpdatedAt(ZonedDateTime.now());
        User currentUser = userService.getCurrentUser();
        existingCourse.setUpdatedBy(currentUser.getUsername());
    }

    private Set<CourseTranslation> updateCourseTranslations(Course course, CourseDto courseDto) {
        Set<CourseTranslation> updatedTranslations = new HashSet<>();

        courseDto.getTranslations().forEach(translationDto -> {
            CourseTranslation translation = course.getTranslations().stream()
                    .filter(t -> t.getId().equals(translationDto.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        CourseTranslation newTranslation = new CourseTranslation();
                        newTranslation.setCourse(course);                        
                        return newTranslation;
                    });
            updateCourseTranslationBasicData(course, translation, translationDto);
            updatedTranslations.add(translation);
        });
        
        // Update existing translations and add new ones
        updatedTranslations.forEach(updatedTranslation -> {
            // Try to find a matching translation in the existing course translations by language code
            course.getTranslations().stream()
                .filter(existingTranslation -> existingTranslation.getLanguageCode() != null && 
                    updatedTranslation.getLanguageCode() != null && 
                    existingTranslation.getLanguageCode().equals(updatedTranslation.getLanguageCode()))
                .findFirst()
                .ifPresentOrElse(existingTranslation -> {
                    course.getTranslations();
                    updateCourseTranslationBasicData(course, existingTranslation, updatedTranslation);                     
                },
                () -> {
                    // Handle the case where no matching updatedTranslation is found
                    // Example: Log a warning or handle the scenario based on your requirements
                   
                        // Logic to add a new translation based on updatedTranslation
                        CourseTranslation newCourseTranslation = new CourseTranslation();
                        newCourseTranslation.setCourse(course);                         // Set the relationship back to the course
                        updateCourseTranslationBasicData(course, newCourseTranslation, updatedTranslation);  
                        // Add this new translation to the course
                        course.getTranslations().add(newCourseTranslation);
                    });
            }); 
       
        return  course.getTranslations();
    }

    private void updateCourseTranslationBasicData(Course course, CourseTranslation translation, CourseTranslationDto translationDto ) {
        if (translationDto.getTitle() != null) {
            translation.setTitle(translationDto.getTitle());
        }
        if (translationDto.getDescription() != null) {
            translation.setDescription(translationDto.getDescription());
        }

        if (translationDto.getAboutVideoUrl() != null 
        && translationDto.getAboutVideoUrl().isEmpty()) {
            translation.setAboutVideoExtid("");
            translation.setAboutVideoUrl("");
        } else if (translationDto.getAboutVideoUrl() != null 
            && !translationDto.getAboutVideoUrl().isEmpty()
            && !translationDto.getAboutVideoUrl().equals(translation.getAboutVideoUrl())) {

                VideoDto videoDto = new VideoDto();
                videoDto.setUrl(translationDto.getAboutVideoUrl());
                videoDto.setLanguageCode(translationDto.getLanguageCode());
                videoDto = addVideoToCourse(course, course.getChannel(), videoDto);
                translation.setAboutVideoExtid(videoDto.getExtId());
                translation.setAboutVideoUrl(translationDto.getAboutVideoUrl());
        }

        if (translationDto.getAboutVideoExtid() != null) {
            translation.setAboutVideoExtid(translationDto.getAboutVideoExtid());
        }
        if (translationDto.getLanguageCode() != null) {
            translation.setLanguage(languageService.getLanguageByCode(translationDto.getLanguageCode()));
        }
    }

    private void updateCourseTranslationBasicData(Course course, CourseTranslation translation, CourseTranslation updatedTranslation ) {
       
        if (updatedTranslation.getTitle() != null) {
            translation.setTitle(updatedTranslation.getTitle());
        }
        if (updatedTranslation.getDescription() != null) {
            translation.setDescription(updatedTranslation.getDescription());
        }
        
        if (updatedTranslation.getAboutVideoUrl() != null 
        && updatedTranslation.getAboutVideoUrl().isEmpty()) {
            translation.setAboutVideoExtid("");
            translation.setAboutVideoUrl("");
        } else if (updatedTranslation.getAboutVideoUrl() != null 
                && !updatedTranslation.getAboutVideoUrl().isEmpty()
                && !updatedTranslation.getAboutVideoUrl().equals(translation.getAboutVideoUrl())) {
            VideoDto videoDto = new VideoDto();
            videoDto.setUrl(translation.getAboutVideoUrl());
            videoDto.setLanguageCode(translation.getLanguage().getCode());
            videoDto = addVideoToCourse(course, course.getChannel(), videoDto);
            translation.setAboutVideoExtid(videoDto.getExtId());
            translation.setAboutVideoUrl(updatedTranslation.getAboutVideoUrl());            
        }

        if (updatedTranslation.getAboutVideoExtid() != null) {
            translation.setAboutVideoExtid(updatedTranslation.getAboutVideoExtid());
        }
        if (updatedTranslation.getLanguageCode() != null) {
            translation.setLanguage(languageService.getLanguageByCode(updatedTranslation.getLanguageCode()));
        }
    }

     private void updateCourseChapters(Course course, CourseDto courseDto) {
        Map<UUID, Chapter> existingChapters = course.getChapters().stream()
        .collect(Collectors.toMap(Chapter::getUuid, Function.identity()));
      //  Set<Chapter> updatedChapters = new HashSet<>();

        for (ChapterDto chapterDto : courseDto.getChapters()) {
            Chapter chapter = existingChapters.get(chapterDto.getUuid());
            if (chapter == null) {
                // This is a new chapter
                chapter = new Chapter();
                chapter.setUuid(UUID.randomUUID());
                chapter.setCourse(course);
                chapter.setUpdatedAt(ZonedDateTime.now());
                chapter.setUpdatedBy(userService.getCurrentUser().getUsername());
            }
            updateChapter(chapter, chapterDto);
        //    updatedChapters.add(chapter);
        }

        // Remove chapters that are no longer in the DTO
     //   course.getChapters().removeIf(chapter -> !updatedChapters.contains(chapter));

        // Add all updated and new chapters
      //  course.getChapters().addAll(updatedChapters);
    }

    private void updateChapter(Chapter chapter, ChapterDto chapterDto) {
        if(chapterDto.getOrderNumber() != null)
        {
            chapter.setOrderNumber(chapterDto.getOrderNumber());
        }
        
        chapter.setUpdatedAt(ZonedDateTime.now());
        chapter.setUpdatedBy(userService.getCurrentUser().getUsername());

        // Update chapter translations
        if (chapterDto.getTranslations() != null && !chapterDto.getTranslations().isEmpty()) {
            updateChapterTranslations(chapter, chapterDto.getTranslations());
        }
        // Update videos if necessary
        if (chapterDto.getVideos() != null && !chapterDto.getVideos().isEmpty()) {
            updateVideosInChapter(chapter, chapterDto.getVideos());
        }

        chapterDao.save(chapter);
    }

    private void updateChapterTranslations(Chapter chapter, List<ChapterTranslationDto> translationDtos) {
        Map<Long, ChapterTranslation> existingTranslations = chapter.getTranslations().stream()
        .collect(Collectors.toMap(t -> t.getId(), Function.identity()));

        Set<ChapterTranslation> updatedTranslations = new HashSet<>();

        for (ChapterTranslationDto translationDto : translationDtos) {
            ChapterTranslation translation = existingTranslations.get(translationDto.getId());
            if (translation == null) {
                translation = new ChapterTranslation();
                translation.setChapter(chapter);                
            }
            translation.setTitle(translationDto.getTitle());
            translation.setDescription(translationDto.getDescription());
            translation.setLanguage(languageService.getLanguageByCode(translationDto.getLanguageCode()));
            updatedTranslations.add(translation);
        }

        // Update existing translations and add new ones
        updatedTranslations.forEach(updatedTranslation -> {
            // Try to find a matching translation in the existing course translations by language code
            chapter.getTranslations().stream()
                .filter(existingTranslation -> existingTranslation.getLanguage().getCode().equals(updatedTranslation.getLanguage().getCode()))
                .findFirst()
                .ifPresentOrElse(
                    existingTranslation -> {
                        // If a match is found, update the existing translation with new data
                        updateChapterTranslationProperties(existingTranslation, updatedTranslation);
                    },
                    () -> {
                        // No match found, create and add a new translation
                        ChapterTranslation newChapterTranslation = new ChapterTranslation();
                       
                        newChapterTranslation.setChapter(chapter);
                        updateChapterTranslationProperties(newChapterTranslation, updatedTranslation);
                        // Add the new translation to the course
                        chapter.getTranslations().add(newChapterTranslation);
                    }
                );
        });
        
    }
    
    private void updateChapterTranslationProperties(ChapterTranslation existingTranslation, ChapterTranslation updatedTranslation) {
        // Add logic to update chapter translation properties
        if (updatedTranslation.getTitle() != null) {
            existingTranslation.setTitle(updatedTranslation.getTitle());
        }
        if (updatedTranslation.getDescription() != null) {
            existingTranslation.setDescription(updatedTranslation.getDescription());
        }
        if (updatedTranslation.getLanguageCode() != null) {
            existingTranslation.setLanguage(languageService.getLanguageByCode(updatedTranslation.getLanguageCode()));
        }
    }  
      
    private void updateVideosInChapter(Chapter chapter, Set<VideoDto> videoDtos) {
        Map<UUID, Video> existingVideos = chapter.getChapterVideos().stream()
        .collect(Collectors.toMap(chapterVideo -> chapterVideo.getVideo().getUuid(), chapterVideo -> chapterVideo.getVideo()));

        videoDtos.forEach(videoDto -> {
            //System.out.println("\n\nupdateChapterVideos video uuid - " + videoDto.getUuid() );
            Video video = existingVideos.get(videoDto.getUuid());
            if (video != null) {
                // Update the order number of the chapter video if it exists
                //System.out.println("\n\nupdateChapterVideos video uuid - " + video.getUuid() + " - " +video.getId());

                chapter.getChapterVideos().stream()
                        .filter(chapterVideo -> chapterVideo.getVideo().getUuid().equals(video.getUuid()))
                        .findFirst()
                        .ifPresent(chapterVideo -> {
                            chapterVideo.setOrderNumber(videoDto.getOrderNumber());
                        });
            }
        });

    }

    private void updateChapterVideos(Chapter chapter, Set<ChapterVideoDto> chapterVideoDtos) {
        Map<UUID, Video> existingVideos = chapter.getChapterVideos().stream()
                .collect(Collectors.toMap(chapterVideo -> chapterVideo.getVideo().getUuid(), chapterVideo -> chapterVideo.getVideo()));

                
      //  Set<Video> updatedVideos = new HashSet<>();
        chapterVideoDtos.forEach(chapterVideoDto -> {
            //System.out.println("\n\nupdateChapterVideos video uuid - " + chapterVideoDto.getVideo().getUuid() );
            Video video = existingVideos.get(chapterVideoDto.getVideo().getUuid());
            if (video != null) {
                // Update the order number of the chapter video if it exists
                //System.out.println("\n\nupdateChapterVideos video uuid - " + video.getUuid() + " - " +video.getId());

                chapter.getChapterVideos().stream()
                        .filter(chapterVideo -> chapterVideo.getVideo().getUuid().equals(video.getUuid()))
                        .findFirst()
                        .ifPresent(chapterVideo -> {                          
                            chapterVideo.setOrderNumber(chapterVideoDto.getOrderNumber());
                        });
            }
        });

        for (ChapterVideoDto chapterVideoDto : chapterVideoDtos) {
           Video video = existingVideos.get(chapterVideoDto.getVideo().getUuid());
            if (video == null) {
                // This is a new video
                //System.out.println("Ignored:- New Video with url - " + chapterVideoDto.getVideo().getUrl() +" is being added to chapter");
            }
            
            updateVideo(video, chapterVideoDto.getVideo());
        //    updatedVideos.add(video);
        }

        
    }

    private void updateVideo(Video video, VideoDto videoDto) {
        if(videoDto.getName() != null)
        {
            video.setName(videoDto.getName());
        }
        
        if(videoDto.getDescription() != null)
        {
            video.setDescription(videoDto.getDescription());
        }
        
        if(videoDto.getThumbnail() != null)
        {
            video.setThumbnail(videoDto.getThumbnail());
        }
       
        if(videoDto.getDuration() != null)
        {
            video.setDuration(videoDto.getDuration());
        }
       
        video.setUpdatedAt(ZonedDateTime.now());
        video.setUpdatedBy(userService.getCurrentUser().getUsername());

        if (videoDto.getLanguageCode() != null) {
            Language language = languageService.getLanguageByCode(videoDto.getLanguageCode());
            if (language == null) {
                throw new LanguageNotFoundException("Language not found with code: " + videoDto.getLanguageCode());
            }
            video.setLanguage(language);
        }

        videoDao.save(video);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public void deleteCourse(UUID uuid) {
        try {
            Course course = courseDao.findByUuid(uuid)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with UUID: " + uuid));
            course.setUpdatedAt(ZonedDateTime.now());
            User currentUser = userService.getCurrentUser();
            course.setUpdatedBy(currentUser.getUsername());
            
            // Delete associated KPoint playlists
            deleteKPointPlaylists(course);
            
            // Course existence is already checked, so we can proceed with deletion
            courseDao.deleteById(course.getId());
            
            logger.info("Course deleted successfully. UUID: {}", uuid);
        } catch (CourseNotFoundException e) {
            logger.error("Course not found during deletion. UUID: {}", uuid);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting course. UUID: {}", uuid, e);
            throw new RuntimeException("Error deleting course with UUID: " + uuid, e);
        }
    }
    
    private void deleteKPointPlaylists(Course course) {
        String channelDisplayName = course.getCourseCode();
        try {
            kPointService.deleteAllPlaylistsForChannel(channelDisplayName);
            logger.info("Successfully deleted all KPoint playlists for course: {}", course.getCourseCode());
        } catch (Exception e) {
            logger.error("Failed to delete KPoint playlists for course: {}. Error: {}", course.getCourseCode(), e.getMessage());
            
        }
    }


    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public CourseDto addCourseTranslation(UUID uuid, CourseTranslation translation, Long languageId) {
        try {
            Course course = courseDao.findByUuid(uuid)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with UUID: " + uuid));
            course.setUpdatedAt(ZonedDateTime.now());
            User currentUser = userService.getCurrentUser();
            course.setUpdatedBy(currentUser.getUsername());

            Language language = languageService.getLanguageById(languageId);
            if (language == null) {
                throw new LanguageNotFoundException("Language not found with id: " + languageId);
            }

            boolean translationExists = course.getTranslations().stream()
                    .anyMatch(t -> t.getLanguage().getId().equals(languageId));

            if (translationExists) {
                throw new DuplicateCourseCodeException("Translation for language " + languageId + " already exists for this course");
            }

            translation.setCourse(course);
            translation.setLanguage(language);
            course.addTranslation(translation);

            Course updatedCourse = courseDao.save(course);
            return courseMapper.toDto(updatedCourse);
        } catch (CourseNotFoundException | LanguageNotFoundException | DuplicateCourseCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error adding course translation for UUID: " + uuid, e);
        }
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByCategory(Long categoryId) {
        try {
            return courseDao.findByCategoryId(categoryId).stream()
                    .map(courseMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving courses by category id: " + categoryId, e);
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public CourseDto addChapterToCourse(UUID courseUuid, ChapterDto chapterDto) {
        try {
            Course course = courseDao.findByUuid(courseUuid)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with uuid: " + courseUuid));

            User user = userService.getCurrentUser();
            Chapter chapter = chapterMapper.toEntity(chapterDto);
            chapter.setUuid(UUID.randomUUID());
            
            chapter.setCreatedBy(user.getUsername());
            chapter.setCreatedAt(ZonedDateTime.now());
            chapter.setUpdatedAt(ZonedDateTime.now());
            chapter.setUpdatedBy(user.getUsername());
            chapter.setCourse(course);

            course.addChapter(chapter);
            Course updatedCourse = courseDao.save(course);
            return courseMapper.toDto(updatedCourse);
        } catch (CourseNotFoundException e) {
            throw e;
        }catch(AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding chapter to course with UUID: " + courseUuid, e);
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public CourseDto updateChapterInCourse(UUID courseUuid, UUID chapterUuid, ChapterDto chapterDto) {
        try {
            Course course = courseDao.findByUuid(courseUuid)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with uuid: " + courseUuid));
            course.setUpdatedAt(ZonedDateTime.now());
            User currentUser = userService.getCurrentUser();
            course.setUpdatedBy(currentUser.getUsername());

            Chapter chapterToUpdate = course.getChapters().stream()
            .filter(chapter -> chapter.getUuid().equals(chapterUuid))
            .findFirst()
            .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with UUID: " + chapterUuid));

            updateChapter(chapterToUpdate, chapterDto);

            Course savedCourse = courseDao.save(course);
            return courseMapper.toDto(savedCourse);
        } catch (CourseNotFoundException | ChapterNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating chapter in course. CourseUUID: " + courseUuid + ", ChapterUUID: " + chapterUuid, e);
        }
    }   

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public CourseDto deleteChapterFromCourse(UUID courseUuid, UUID chapterUuid) {
        try {
            Course course = courseDao.findByUuid(courseUuid)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with uuid: " + courseUuid));
            course.setUpdatedAt(ZonedDateTime.now());
            User currentUser = userService.getCurrentUser();
            course.setUpdatedBy(currentUser.getUsername());

            Chapter chapterToRemove = course.getChapters().stream()
                    .filter(ch -> ch.getUuid().equals(chapterUuid))
                    .findFirst()
                    .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + chapterUuid));

            course.removeChapter(chapterToRemove);
            Course updatedCourse = courseDao.save(course);
            return courseMapper.toDto(updatedCourse);
        } catch (CourseNotFoundException | ChapterNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting chapter from course. CourseUUID: " + courseUuid + ", ChapterUUID: " + chapterUuid, e);
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public VideoResponse addVideoToChapter(Course course, Chapter chapter, VideoDto videoDto) {
        try {           

            if (!chapter.getCourse().getId().equals(course.getId())) {
                throw new IllegalArgumentException("Chapter does not belong to the specified course");
            }

            JsonNode videoNode = kPointService.checkAndUploadVideo(videoDto.getUrl(),course.getCourseCode(),videoDto.getLanguageCode());
            if(videoNode == null)
            {
                throw new VideoNotFoundException("Video cannot be added to KPoint: " + videoDto.getUrl());
            }
            try {
                // Check if a video with the same URL already exists
                Optional<Video> existingVideo = videoDao.findByUrl(videoDto.getUrl());
                Video video;
                User user = userService.getCurrentUser();
                int videoCreationStatus = 0;
                int oldChapterSize = 0;
                if (existingVideo.isPresent()) {
                    video = existingVideo.get();
                     boolean isInSameChapter = video.getChapterVideos().stream()
                    .anyMatch(cv -> cv.getChapter().getId().equals(chapter.getId()));

                    if (isInSameChapter && isVideoUnchanged(video, videoNode, videoDto.getOrderNumber())) {
                        videoCreationStatus = 1;
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate video in the same chapter");
                    }

                    oldChapterSize = video.getChapterVideos().size();
                    videoMapper.updateEntityFromDto(video, videoDto);
                    updateVideoProperties(video, videoNode);
                    videoCreationStatus = 2;
                    System.out.println("\n\nVideo already exists in the database\n\n");     
                } else {
                    video = videoMapper.toEntity(videoDto);
                    video.setUuid(UUID.randomUUID());
                    video.setCreatedAt(ZonedDateTime.now());
                    video.setCreatedBy(user.getUsername());
                    video.setUpdatedAt(ZonedDateTime.now());
                    video.setUpdatedBy(user.getUsername());
                    updateVideoProperties(video, videoNode);
                    videoCreationStatus = 3;
                  
                }
               
                // Check if the video is already associated with the chapter
                Video finalVideo = video; // Create a final reference to video

                // Check if the video is already associated with the chapter
                ChapterVideo chapterVideo = video.getChapterVideos().stream()
                .filter(cv -> cv.getVideo().getId().equals(finalVideo.getId()) && cv.getChapter().getId().equals(chapter.getId()))
                .findFirst()
                .orElseGet(() -> {
                    System.out.println("\n\nVideoNOT ASSOCIATED WITH THE CHAPTER " + chapter.getUuid() +" -- " + finalVideo.getUuid());  
                    ChapterVideo newChapterVideo = new ChapterVideo(chapter, finalVideo, videoDto.getOrderNumber());
                    chapter.addChapterVideo(newChapterVideo);  
                    finalVideo.addChapterVideo(newChapterVideo);  
                    return newChapterVideo;
                });

                
                // Update the order number
                chapterVideo.setOrderNumber(videoDto.getOrderNumber());

                video = videoDao.save(finalVideo);
                chapterDao.save(chapter);
           
                // Add video to the course's channel playlist
                Channel channel = getOrCreateChannel(course);
                course.setChannel(channel);
                Playlist playlist = getOrCreatePlaylist(channel, course.getCourseCode(), video.getLanguage().getCode());             
                addVideoToPlaylist(playlist, video);
                
                  // Fetch the updated video with all associations
                video = videoDao.findById(video.getId())
                .orElseThrow(() -> new VideoNotFoundException("Video not found after saving"));

                VideoDto newVideoDto = videoMapper.toDto(video);
                newVideoDto.setOrderNumber(videoDto.getOrderNumber());
               
                if((videoCreationStatus == 2) && (oldChapterSize == video.getChapterVideos().size())) { //Exisiting videoupdated
                    return new VideoResponse(HttpStatus.OK, newVideoDto, "Video updated successfully");
                }
                
                return new VideoResponse(HttpStatus.CREATED, newVideoDto, "New video added successfully");
            } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error adding video to chapter. Course: " + course.getCourseCode() + ", ChapterUUID: " + chapter.getUuid(), e);
            }
        } catch(IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Chapter does not belong to the specified course");
        } catch(KPAddPlayListToChannelException | KPVideoUploadException |  KPPlaylistCreationException | KPChannleNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Error adding video to chapter. Course: " + course.getCourseCode() + ", ChapterUUID: " + chapter.getUuid(), e);
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Outer exception Error adding video to chapter. Course: " + course.getCourseCode() + ", ChapterUUID: " + chapter.getUuid(), e);
        }
    }
    
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public VideoResponse addVideoToChapter(UUID courseUuid, UUID chapterUuid, VideoDto videoDto) {
        try {
            Course course = courseDao.findByUuid(courseUuid)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with uuid: " + courseUuid));
            course.setUpdatedAt(ZonedDateTime.now());
            User currentUser = userService.getCurrentUser();
            course.setUpdatedBy(currentUser.getUsername());

            Chapter chapter = chapterDao.findByUuid(chapterUuid)
                    .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + chapterUuid));

            if (!chapter.getCourse().getId().equals(course.getId())) {
                throw new IllegalArgumentException("Chapter does not belong to the specified course");
            }

            JsonNode videoNode = kPointService.checkAndUploadVideo(videoDto.getUrl(),course.getCourseCode(),videoDto.getLanguageCode());
            if(videoNode == null)
            {
                throw new VideoNotFoundException("Video cannot be added to KPoint: " + videoDto.getUrl());
            }
            try {
                // Check if a video with the same URL already exists
                Optional<Video> existingVideo = videoDao.findByUrl(videoDto.getUrl());
                Video video;
                User user = userService.getCurrentUser();
                int videoCreationStatus = 0;
                int oldChapterSize = 0;
                if (existingVideo.isPresent()) {
                    video = existingVideo.get();
                     boolean isInSameChapter = video.getChapterVideos().stream()
                    .anyMatch(cv -> cv.getChapter().getId().equals(chapter.getId()));

                    if (isInSameChapter && isVideoUnchanged(video, videoNode, videoDto.getOrderNumber())) {
                        videoCreationStatus = 1;
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate video in the same chapter");
                    }

                    oldChapterSize = video.getChapterVideos().size();
                    videoMapper.updateEntityFromDto(video, videoDto);
                    updateVideoProperties(video, videoNode);
                    videoCreationStatus = 2;
                    System.out.println("\n\nVideo already exists in the database\n\n");     
                } else {
                    video = videoMapper.toEntity(videoDto);
                    video.setUuid(UUID.randomUUID());
                    video.setCreatedAt(ZonedDateTime.now());
                    video.setCreatedBy(user.getUsername());
                    video.setUpdatedAt(ZonedDateTime.now());
                    video.setUpdatedBy(user.getUsername());
                    updateVideoProperties(video, videoNode);
                    videoCreationStatus = 3;
                  
                }
               
                // Check if the video is already associated with the chapter
                Video finalVideo = video; // Create a final reference to video

                // Check if the video is already associated with the chapter
                ChapterVideo chapterVideo = video.getChapterVideos().stream()
                .filter(cv -> cv.getVideo().getId().equals(finalVideo.getId()) && cv.getChapter().getId().equals(chapter.getId()))
                .findFirst()
                .orElseGet(() -> {
                    System.out.println("\n\nVideoNOT ASSOCIATED WITH THE CHAPTER " + chapter.getUuid() +" -- " + finalVideo.getUuid());  
                    ChapterVideo newChapterVideo = new ChapterVideo(chapter, finalVideo, videoDto.getOrderNumber());
                    chapter.addChapterVideo(newChapterVideo);  
                    finalVideo.addChapterVideo(newChapterVideo);  
                    return newChapterVideo;
                });

                
                // Update the order number
                chapterVideo.setOrderNumber(videoDto.getOrderNumber());

                video = videoDao.save(finalVideo);
                chapterDao.save(chapter);
           
                // Add video to the course's channel playlist
                Channel channel = getOrCreateChannel(course);
                Playlist playlist = getOrCreatePlaylist(channel, course.getCourseCode(), video.getLanguage().getCode());             
                addVideoToPlaylist(playlist, video);
                
                  // Fetch the updated video with all associations
                video = videoDao.findById(video.getId())
                .orElseThrow(() -> new VideoNotFoundException("Video not found after saving"));

                VideoDto newVideoDto = videoMapper.toDto(video);
                newVideoDto.setOrderNumber(videoDto.getOrderNumber());
               
                if((videoCreationStatus == 2) && (oldChapterSize == video.getChapterVideos().size())) { //Exisiting videoupdated
                    return new VideoResponse(HttpStatus.OK, newVideoDto, "Video updated successfully");
                }
                
                return new VideoResponse(HttpStatus.CREATED, newVideoDto, "New video added successfully");
            } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error adding video to chapter. CourseUUID: " + courseUuid + ", ChapterUUID: " + chapterUuid, e);
            }
        } catch(IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Chapter does not belong to the specified course");
        } catch(KPAddPlayListToChannelException | KPVideoUploadException |  KPPlaylistCreationException | KPChannleNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Error adding video to chapter. CourseUUID: " + courseUuid + ", ChapterUUID: " + chapterUuid, e);
        } catch (CourseNotFoundException | ChapterNotFoundException | VideoNotFoundException | ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding video to chapter. CourseUUID: " + courseUuid + ", ChapterUUID: " + chapterUuid, e);
        }
    }

    private boolean isVideoUnchanged(Video video, JsonNode videoNode, Integer orderNumber) {
        return video.getName().equals(videoNode.get("displayname").asText()) &&
               video.getDescription().equals(videoNode.get("description").asText()) &&
               video.getThumbnail().equals(videoNode.get("images").get("thumb").asText()) &&
               video.getDuration().equals(videoNode.get("published_duration").asInt()) &&
               video.getExtId().equals(videoNode.get("id").asText()) &&
               video.getStatus().equals(videoNode.get("status").asText()) &&
               video.getChapterVideos().stream()
                       .anyMatch(cv -> cv.getVideo().getId().equals(video.getId()) && cv.getOrderNumber().equals(orderNumber));
    }

    private void updateVideoProperties(Video video, JsonNode videoNode) {
        video.setName(videoNode.get("displayname").asText());
        video.setDescription(videoNode.get("description").asText());
        video.setThumbnail(videoNode.get("images").get("thumb").asText());
        video.setDuration(videoNode.get("published_duration").asInt());
        video.setExtId(videoNode.get("id").asText());
        video.setStatus(videoNode.get("status").asInt());
        video.setUpdatedAt(ZonedDateTime.now());
        video.setUpdatedBy(userService.getCurrentUser().getUsername());
    }
    
    private Channel getOrCreateChannel(Course course) {
        Channel channel = course.getChannel();
        if (channel == null) {
            channel = new Channel();
            channel.setName(course.getCourseCode());
            channel.setCourse(course);
            channel.setCreatedAt(ZonedDateTime.now());
            channel.setUpdatedAt(ZonedDateTime.now());
            channel.setCreatedBy(userService.getCurrentUser().getUsername());
            channel.setUpdatedBy(userService.getCurrentUser().getUsername());
            channel = channelDao.save(channel);
        }

        return channel;
    }
    
    private Playlist getOrCreatePlaylist(Channel channel, String courseCode, String languageCode) {
        return channel.getPlaylists().stream()
                .filter(p -> p.getName().equals("PL_" + courseCode + "_" + languageCode))
                .findFirst()
                .orElseGet(() -> {
                    Playlist newPlaylist = new Playlist();
                    newPlaylist.setName("PL_" + courseCode + "_" + languageCode);
                    newPlaylist.setChannel(channel);
                    newPlaylist.setCreatedAt(ZonedDateTime.now());
                    newPlaylist.setUpdatedAt(ZonedDateTime.now());
                    newPlaylist.setCreatedBy(userService.getCurrentUser().getUsername());
                    newPlaylist.setUpdatedBy(userService.getCurrentUser().getUsername());
                    return playlistDao.save(newPlaylist);
                });
    }
    
    private void addVideoToPlaylist(Playlist playlist, Video video) {
        if (!playlist.getVideos().contains(video)) {
            playlist.addVideo(video);
            playlistDao.save(playlist);
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public VideoDto addVideoToCourse(Course course, Channel channel, VideoDto videoDto) {
        

        //System.out.println("\n\nvideoDto.getUrl() - " + videoDto.getUrl() + " - " + videoDto.getLanguageCode());
        JsonNode videoNode = kPointService.checkAndUploadVideo(videoDto.getUrl(),course.getCourseCode(),videoDto.getLanguageCode());
        if(videoNode == null)
        {        
            throw new VideoNotFoundException("Video not found with url in video service: " + videoDto.getUrl());
        }
        try {
            // Check if a video with the same URL already exists
            Optional<Video> existingVideo = videoDao.findByUrl(videoDto.getUrl());
            Video video;
            User user = userService.getCurrentUser();
            if (existingVideo.isPresent()) {
                video = existingVideo.get();     
            } else {
                video = videoMapper.toEntity(videoDto);
                video.setUuid(UUID.randomUUID());
                video.setCreatedBy(user.getUsername());
                video.setCreatedAt(ZonedDateTime.now());
            }

            video.setName(videoNode.get("displayname").asText());
            video.setDescription(videoNode.get("description").asText());
            video.setThumbnail(videoNode.get("images").get("thumb").asText());
            video.setDuration(videoNode.get("published_duration").asInt());
            video.setExtId(videoNode.get("id").asText());
            video.setStatus(videoNode.get("status").asInt());
          
            video.setUpdatedAt(ZonedDateTime.now());
            video.setUpdatedBy(user.getUsername()); // Replace with actual user when authentication is implemented
            video = videoDao.save(video);

            Video finalVideo = video; // Create a final reference to video
            String finalVideoLanguageCode = video.getLanguage().getCode();
            course.getTranslations().stream()
            .filter(translation -> translation.getLanguageCode().equals(finalVideoLanguageCode))
            .findFirst()
            .ifPresentOrElse(translation -> {                
                //System.out.println("Video already exists in the course");
                translation.setAboutVideoExtid(finalVideo.getExtId());
                translation.setAboutVideoUrl(finalVideo.getUrl());
            },
            () -> {
                //System.out.println("Translation doesnot exists in the course");               
            });


            
            Playlist playlist = getOrCreatePlaylist(channel, course.getCourseCode(), video.getLanguage().getCode());             
            addVideoToPlaylist(playlist, video);
           

            return videoMapper.toDto(video);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding video to chapter. Course: " + course.getCourseCode(), e);
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public CourseDto deleteVideoFromChapter(UUID courseUuid, UUID chapterUuid, UUID videoUuid) {
        Course course = courseDao.findByUuid(courseUuid)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with uuid: " + courseUuid));
        course.setUpdatedAt(ZonedDateTime.now());
        User currentUser = userService.getCurrentUser();
        course.setUpdatedBy(currentUser.getUsername());

        Chapter chapter = course.getChapters().stream()
                .filter(ch -> ch.getUuid().equals(chapterUuid))
                .findFirst()
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + chapterUuid));

        Video video = videoDao.findByUuid(videoUuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + videoUuid));

        chapter.getChapterVideos().stream()
            .filter(chVideo -> chVideo.getVideo().getUuid().equals(video.getUuid()))
            .findFirst()
            .ifPresentOrElse(translation -> {                
                //System.out.println("Video exists in the course ready to be removed");               
            },
            () -> {
                throw new VideoNotFoundException("Video not found in the specified chapter");               
            });
       

        chapter.removeVideo(video);
        chapterDao.save(chapter);
        return courseMapper.toDto(course);
    }
    
    @Transactional
    public void processBatchUpdate(InputStream inputStream) throws IOException {
        
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, Language> languageMap = languageService.getAllLanguages().stream()
                    .collect(Collectors.toMap(Language::getCode, Function.identity()));
           
            // Create a map of courses from the Excel sheet
            Map<String, Course> courseMap = createCourseMap(sheet, languageMap);

            // Create a map of existing courses from the database
            Map<String, Course> existingCoursesMap = courseDao.findAll().stream()
                    .collect(Collectors.toMap(Course::getCourseCode, Function.identity()));
            
            // Update existing courses and add new ones
            insUpdCourses(courseMap, existingCoursesMap);
            
        } catch (IOException e) {
            throw new RuntimeException("Error processing the Excel file for course batch update" +e.getMessage() , e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error closing the workbook", e);
                }
            }
        }
    }

    private Map<String, Course>  createCourseMap(Sheet sheet,  Map<String, Language> languageMap ) {
        Map<String, Course> courseMap = new HashMap<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header row

            String edpId = getCellValueAsString(row.getCell(1));
            String courseCode = getCellValueAsString(row.getCell(2));
            String displayCourseCode = getCellValueAsString(row.getCell(3));
            String courseName = getCellValueAsString(row.getCell(4));
            String description = getCellValueAsString(row.getCell(5));
            String aboutVideoUrl = getCellValueAsString(row.getCell(6));
            Integer duration = (int) row.getCell(7).getNumericCellValue();
            String languageCode = getCellValueAsString(row.getCell(8));

            Course course = courseMap.computeIfAbsent(courseCode, k -> {
                Course newCourse = new Course();
                newCourse.setUuid(UUID.randomUUID());
                newCourse.setCourseCode(courseCode);
                newCourse.setDisplayCourseCode(displayCourseCode);
                newCourse.setDuration(duration);
                newCourse.setCategory(Integer.parseInt(edpId));
                newCourse.setImageUrl(crsImgBucketUrl + "/" +courseCode + ".png");
                newCourse.setStatus(1);
                return newCourse;
            });

            Language language = languageMap.get(languageCode);
            if (language == null) {
                throw new RuntimeException("Language not found: " + languageCode);
            }

            CourseTranslation translation = new CourseTranslation();
            translation.setCourse(course);
            translation.setLanguage(language);
            translation.setTitle(courseName);
            translation.setDescription(description);
            translation.setAboutVideoUrl(aboutVideoUrl);
            if(aboutVideoUrl != null && aboutVideoUrl.length() > 3) {
                JsonNode videoNode = kPointService.checkAndUploadVideo(aboutVideoUrl,course.getCourseCode(),languageCode);
                if(videoNode != null)
                {
                    //System.out.println("\n\nVideo uploaded to Kpoint successfully " + videoNode.get("id").asText());
                    translation.setAboutVideoExtid(videoNode.get("id").asText());
                }
            } else {
                translation.setAboutVideoExtid(null);
            }
            
            course.getTranslations().add(translation);
        }

        return courseMap;
    }

    private void insUpdCourses( Map<String, Course> courseMap,  Map<String, Course> existingCoursesMap ) {
        for (Course course : courseMap.values()) {
            if (existingCoursesMap.containsKey(course.getCourseCode())) {
                Course existingCourse = existingCoursesMap.get(course.getCourseCode());
                existingCourse.setDisplayCourseCode(course.getDisplayCourseCode());
                existingCourse.setCategory(course.getCategory());                    
                existingCourse.setDuration(course.getDuration());
                existingCourse.setStatus(1);
                existingCourse.setUpdatedAt(ZonedDateTime.now());
                Map<String, CourseTranslation> existingTranslationMap = existingCourse.getTranslations().stream()
                        .collect(Collectors.toMap(t -> t.getLanguage().getCode(), t -> t));

                for (CourseTranslation translation : course.getTranslations()) {
                    String languageCode = translation.getLanguage().getCode();
                    if (existingTranslationMap.containsKey(languageCode)) {
                        CourseTranslation existingTranslation = existingTranslationMap.get(languageCode);
                        existingTranslation.setTitle(translation.getTitle());
                        existingTranslation.setDescription(translation.getDescription());
                        existingTranslation.setAboutVideoUrl(translation.getAboutVideoUrl());
                        existingTranslation.setAboutVideoExtid(translation.getAboutVideoExtid());
                    } else {
                        existingCourse.getTranslations().add(translation);
                    }
                }
                courseDao.save(existingCourse);
            } else {
                courseDao.save(course);
            }
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public void bulkUploadVideos(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Course> courseCache = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            
            // Skip header row
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header row
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    processVideoRow(row, courseCache, errors);
                } catch (Exception e) {
                    errors.put("Row " + (row.getRowNum() + 1), e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                throw new RuntimeException("Errors occurred during bulk upload: " + errors);
            }
        }
    }

    private void processVideoRow(Row row, Map<String, Course> courseCache, Map<String, String> errors) {
        String courseCode = getCellValueAsString(row.getCell(0));
        String chapterName = getCellValueAsString(row.getCell(1));
        String youtubeUrl = getCellValueAsString(row.getCell(2));
        Integer orderNo = (int) row.getCell(3).getNumericCellValue();
        String languageCode = getCellValueAsString(row.getCell(4));

        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        System.out.println("Started videp row processing - " + courseCode + " -- " + chapterName + "  " + youtubeUrl);
    
        if (courseCode.isEmpty() || youtubeUrl.isEmpty() || languageCode.isEmpty()) {
            return; // Skip empty rows
        }
    
        // Get or fetch course
        Course course = courseCache.computeIfAbsent(courseCode, code -> 
            courseDao.findByCourseCode(code)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with code: " + code))
        );
        
        if (chapterName.isEmpty()) {
            // Add video to CourseTranslation
            updateCourseTranslationWithVideo(course, youtubeUrl, languageCode);
        } else {
        // Find or create chapter
            Chapter chapter = findOrCreateChapter(course, chapterName, "en");
        
            // Create video DTO
            VideoDto videoDto = new VideoDto();
            videoDto.setUrl(youtubeUrl);
            videoDto.setLanguageCode(languageCode);
            videoDto.setOrderNumber(orderNo);
        
            // Upload video and add to chapter
            VideoResponse response = addVideoToChapter(course, chapter, videoDto);
            if (response.getStatus() != HttpStatus.CREATED && response.getStatus() != HttpStatus.OK) {
                errors.put("Row " + (row.getRowNum() + 1), "Failed to upload video: " + response.getVideo().getUrl());
            } else {
                System.out.println("Row " + (row.getRowNum() + 1) + "Sucessfully uploaded video: " + response.getVideo().getUrl());
                System.out.println("Message  " + response.getMessage());
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
            }
        }

       
    }

    /**
     * Process batch update for chapters with multi-language translations
     * @param file Excel file with chapter data
     * @return Map containing statistics of the operation
     * @throws IOException If file processing fails
     */
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public Map<String, Object> processChapterBatchUpdate(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalRows", 0);
        stats.put("processed", 0);
        stats.put("skipped", 0);
        stats.put("chaptersCreated", 0);
        stats.put("translationsAdded", 0);
        stats.put("errors", 0);
        List<String> errorMessages = new ArrayList<>();

        // Prefetch all course codes and their corresponding courses in a single query
        Map<String, Course> courseCache = new HashMap<>();
        
        // Prefetch all language data in a single query
        Map<String, Language> languageCache = languageService.getAllLanguages()
            .stream()
            .collect(Collectors.toMap(Language::getCode, language -> language));
        
        // Get current user once to avoid multiple service calls
        User currentUser = userService.getCurrentUser();
        ZonedDateTime now = ZonedDateTime.now();
        
        // Collect all changes to be made
        List<Chapter> chaptersToSave = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row if it exists
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                // Validate headers if needed
                String courseCodeHeader = getCellValueAsString(headerRow.getCell(0));
                if (!"Course Code".equalsIgnoreCase(courseCodeHeader)) {
                    throw new RuntimeException("Invalid file format. Expected 'Course Code' in first column.");
                }
            }

            // First, collect all course codes from the file
            Set<String> allCourseCodes = new HashSet<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                String courseCode = getCellValueAsString(row.getCell(0));
                if (!courseCode.isEmpty()) {
                    allCourseCodes.add(courseCode);
                }
            }
            
            // Batch load all needed courses in a single operation
            for (String code : allCourseCodes) {
                Optional<Course> course = courseDao.findByCourseCode(code);
                if (course.isPresent()) {
                    courseCache.put(code, course.get());
                }
            }

            // Process each row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                stats.put("totalRows", stats.get("totalRows") + 1);
                
                try {
                    String courseCode = getCellValueAsString(row.getCell(0));
                    String chapterNameEn = getCellValueAsString(row.getCell(1));
                    String chapterNameHi = getCellValueAsString(row.getCell(2));
                    
                    // Skip rows with empty values
                    if (courseCode.isEmpty() || (chapterNameEn.isEmpty() && chapterNameHi.isEmpty())) {
                        stats.put("skipped", stats.get("skipped") + 1);
                        continue;
                    }
                    
                    // Get course from cache
                    Course course = courseCache.get(courseCode);
                    if (course == null) {
                        throw new CourseNotFoundException("Course not found with code: " + courseCode);
                    }
                    
                    // Process the chapter data
                    Chapter chapterToSave = processChapterTranslations(
                        course, 
                        chapterNameEn, 
                        chapterNameHi, 
                        stats, 
                        languageCache, 
                        currentUser, 
                        now
                    );
                    
                    if (chapterToSave != null) {
                        chaptersToSave.add(chapterToSave);
                    }
                    
                    stats.put("processed", stats.get("processed") + 1);
                    
                } catch (Exception e) {
                    stats.put("errors", stats.get("errors") + 1);
                    errorMessages.add("Row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    logger.error("Error processing row " + (row.getRowNum() + 1), e);
                }
            }
        }
        
        // Batch save all chapters at once to minimize database operations
        if (!chaptersToSave.isEmpty()) {
            for (Chapter chapter : chaptersToSave) {
                chapterDao.save(chapter);
            }
        }
        
        result.put("statistics", stats);
        if (!errorMessages.isEmpty()) {
            result.put("errors", errorMessages);
        }
        
        return result;
    }

    private Chapter processChapterTranslations(
        Course course, 
        String chapterNameEn, 
        String chapterNameHi, 
        Map<String, Integer> stats,
        Map<String, Language> languageCache,
        User currentUser,
        ZonedDateTime now) {
        
        // Find chapter by English or Hindi name
        Optional<Chapter> existingChapter = Optional.empty();
        
        if (!chapterNameEn.isEmpty()) {
            existingChapter = findChapterByTranslation(course, chapterNameEn, "en");
        }
        
        if (existingChapter.isEmpty() && !chapterNameHi.isEmpty()) {
            existingChapter = findChapterByTranslation(course, chapterNameHi, "hi");
        }
        
        if (existingChapter.isPresent()) {
            // Update existing chapter with missing translations
            Chapter chapter = existingChapter.get();
            boolean translationsAdded = false;
            
            if (!chapterNameEn.isEmpty() && !hasTranslation(chapter, "en")) {
                addTranslation(chapter, chapterNameEn, "en", languageCache);
                translationsAdded = true;
            }
            
            if (!chapterNameHi.isEmpty() && !hasTranslation(chapter, "hi")) {
                addTranslation(chapter, chapterNameHi, "hi", languageCache);
                translationsAdded = true;
            }
            
            if (translationsAdded) {
                chapter.setUpdatedAt(now);
                chapter.setUpdatedBy(currentUser.getUsername());
                stats.put("translationsAdded", stats.get("translationsAdded") + 1);
                return chapter;
            }
            
            return null;
        } else if (!chapterNameEn.isEmpty() || !chapterNameHi.isEmpty()) {
            // Create new chapter with available translations
            Chapter newChapter = new Chapter();
            newChapter.setUuid(UUID.randomUUID());
            newChapter.setCourse(course);
            newChapter.setOrderNumber(getNextChapterOrderNumber(course));
            newChapter.setCreatedAt(now);
            newChapter.setUpdatedAt(now);
            newChapter.setCreatedBy(currentUser.getUsername());
            newChapter.setUpdatedBy(currentUser.getUsername());
            newChapter.setStatus(1); // Active status
            
            if (!chapterNameEn.isEmpty()) {
                addTranslation(newChapter, chapterNameEn, "en", languageCache);
            }
            
            if (!chapterNameHi.isEmpty()) {
                addTranslation(newChapter, chapterNameHi, "hi", languageCache);
            }
            
            course.addChapter(newChapter);
            stats.put("chaptersCreated", stats.get("chaptersCreated") + 1);
            return newChapter;
        }
        
        return null;
    }

    private Optional<Chapter> findChapterByTranslation(Course course, String chapterName, String languageCode) {
        return course.getChapters().stream()
            .filter(chapter -> chapter.getTranslations().stream()
                .anyMatch(translation -> 
                    translation.getLanguage().getCode().equals(languageCode) && 
                    translation.getTitle().equals(chapterName)))
            .findFirst();
    }

    private boolean hasTranslation(Chapter chapter, String languageCode) {
        return chapter.getTranslations().stream()
            .anyMatch(translation -> translation.getLanguage().getCode().equals(languageCode));
    }

    private void addTranslation(Chapter chapter, String title, String languageCode, Map<String, Language> languageCache) {
        ChapterTranslation translation = new ChapterTranslation();
        translation.setChapter(chapter);
        translation.setLanguage(languageCache.get(languageCode));
        translation.setTitle(title);
        translation.setDescription(title); // Using title as description by default
        chapter.addTranslation(translation);
    }

    private Integer getNextChapterOrderNumber(Course course) {
        return course.getChapters().stream()
            .map(Chapter::getOrderNumber)
            .max(Integer::compareTo)
            .orElse(0) + 1;
    }

    private void updateCourseTranslationWithVideo(Course course, String youtubeUrl, String languageCode) {
        CourseTranslation translation = course.getTranslations().stream()
            .filter(t -> t.getLanguage().getCode().equals("en"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Course translation not found for language: " + languageCode));
    
        JsonNode videoNode = kPointService.checkAndUploadVideo(youtubeUrl, course.getCourseCode(), languageCode);
        if (videoNode == null) {
            throw new VideoNotFoundException("Video cannot be added to KPoint: " + youtubeUrl);
        }
    
        translation.setAboutVideoUrl(youtubeUrl);
        translation.setAboutVideoExtid(videoNode.get("id").asText());
    
        // Save the updated course
        courseDao.save(course);
    }
    
    private Chapter findOrCreateChapter(Course course, String chapterName, String languageCode) {
        Optional<Chapter> chapterOpt = course.getChapters().stream()
            .filter(ch -> ch.getTranslations().stream()
                .anyMatch(tr -> tr.getTitle().equals(chapterName) && 
                            tr.getLanguage().getCode().equals(languageCode)))
            .findFirst();

        if (chapterOpt.isPresent()) {
            return chapterOpt.get();
        }

        // Create new chapter
        ChapterDto newChapterDto = new ChapterDto();
        List<ChapterTranslationDto> translations = new ArrayList<>();
        
        ChapterTranslationDto translation = new ChapterTranslationDto();
        translation.setTitle(chapterName);
        translation.setDescription(chapterName); // Using name as description
        translation.setLanguageCode(languageCode);
        translations.add(translation);
        
        newChapterDto.setTranslations(translations);
        newChapterDto.setOrderNumber(getNextChapterOrderNumber(course));
        newChapterDto.setStatus(1); // Active status
        
        // Use existing method to add chapter
        CourseDto updatedCourseDto = addChapterToCourse(course.getUuid(), newChapterDto);
        
        // Refresh course to get the new chapter
        course = courseDao.findByCourseCode(course.getCourseCode()).orElseThrow();
        
        // Find the newly created chapter
        return course.getChapters().stream()
            .filter(ch -> ch.getTranslations().stream()
                .anyMatch(tr -> tr.getTitle().equals(chapterName) && 
                            tr.getLanguage().getCode().equals(languageCode)))
            .findFirst()
            .orElseThrow(() -> new ChapterNotFoundException("Newly created chapter not found"));
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }  
}