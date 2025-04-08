package com.tl.reap_admin_api.service; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tl.reap_admin_api.exception.KPAddPlayListToChannelException;
import com.tl.reap_admin_api.exception.KPChannleNotFoundException;
import com.tl.reap_admin_api.exception.KPPlaylistCreationException;
import com.tl.reap_admin_api.exception.KPVideoUploadException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class KPointService implements ThirdPartyVideoService {
    private static final Logger logger = LoggerFactory.getLogger(KPointService.class);

    @Value("${kpoint.baseUrl}")
	private String baseUrl;
	
	@Value("${kpoint.playlist.url}")
	private String playlistUrl;
	
	@Value("${kpoint.channel.url}")
	private String channelUrl;
	
	@Value("${kpoint.video.url}")
	private String videoUrl;

    private final RestTemplate restTemplate;
    private final AuthTokenManager authTokenManager;
    private String errorUrl = "";

    public KPointService( RestTemplate restTemplate, AuthTokenManager authTokenManager) {
        this.restTemplate = restTemplate;   
        this.authTokenManager = authTokenManager;
    }

    public String getAuthtoken(String email, String displayName) {
        try {
            return authTokenManager.getAuthToken(email, displayName);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public JsonNode getChannel(String channelDisplayName)  {
        
        // Implement the logic to check if the channel exists
        String apiUrl = "";
        try {
            apiUrl = channelUrl+"?scope=all&first=0&max=10&shallow_search=true&qtext=\""+channelDisplayName+"\"&xt="+authTokenManager.getAuthToken();
            errorUrl = apiUrl ;
            logger.debug("\n\n ******************************************************");
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            logger.debug("Get Channel: Response Status - {} - url - {}", response.getStatusCode(), apiUrl);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode listNode = rootNode.get("list");
               
                return listNode.get(0) ;
            }

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            // Log the exception
            e.printStackTrace();
            // You might want to throw a custom exception or handle it differently
            return null;
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();	         
        } 

        return null;
    }

    @Override
    public JsonNode getPlaylist(String playlistDisplayName) {
        // Implement the logic to check if the playlist exists
        String apiUrl = "";
        try {
            apiUrl = playlistUrl+"?scope=all&first=0&max=10&shallow_search=true&qtext=\""+playlistDisplayName+"\"&xt="+authTokenManager.getAuthToken();
            errorUrl = apiUrl ;
           
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            logger.debug("Get Playlist: - {} - url - {}", response.getStatusCode(), apiUrl);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode listNode = rootNode.get("list");
                if(listNode.size() > 0)
                {
                    return listNode.get(0);
                }
            }

            //System.out.println("\n\ngetPlaylist response - " + response.getStatusCode());
        }  catch (InvalidKeyException | NoSuchAlgorithmException e) {
            // Log the exception
            e.printStackTrace();
            // You might want to throw a custom exception or handle it differently
            return null;
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();	         
        } 

        return null;
    }

    @Override
    public boolean checkPlaylistExistsInChannel(String playlistName, String channelName) {
        // Implement the logic to check if the playlist exists in the channel
        String url = "";
	    try {
	    	String token = authTokenManager.getAuthToken();
	    	url = String.format("%s/%s/content?qtext=%s&type=%s&first=0&max=1&xt=%s",
	    			channelUrl, channelName, playlistName, "playlists", token);

            errorUrl = url ;
            //System.out.println("\n\ncheckPlaylistExistsInChannel -- " + url);           
	        HttpHeaders headers = new HttpHeaders();
	        HttpEntity<String> entity = new HttpEntity<>(headers);
	        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
	        
            logger.debug("Check Playlist exists in channel: - {} - url - {}", response.getStatusCode(), url);
	        if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                JsonNode node = objectMapper.readTree(response.getBody());
                //System.out.println("\n\ncheckPlaylistExistsInChannel NODE -- " + node.toString() );
                
                JsonNode listNode = node.get("list");

                //System.out.println("\n\ncheckPlaylistExistsInChannel listNode size-- " + listNode.size() );
                if(listNode.size() > 0)
                {
                    JsonNode plJsonNode = listNode.get(0);
                    //System.out.println("\n\ncheckPlaylistExistsInChannel JsonNode -- " + plJsonNode );
                    return plJsonNode != null;
                }
	        }

            //System.out.println("\n\ncheckPlaylistExistsInChannel Response -- " + response.getStatusCode() );
	    }  catch (InvalidKeyException | NoSuchAlgorithmException e) {
            // Log the exception
            e.printStackTrace();
            // You might want to throw a custom exception or handle it differently
           
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();	         
        } 

        return false;
    }

    @Override
    public JsonNode createPlaylist(String playlistDisplayName) {
        JsonNode plnode = null;
        String url = "";
        // Implement the logic to create a playlist
        try {
	    	String token = authTokenManager.getAuthToken();
	    	url = String.format("%s?displayname=%s&visibility=%s&xt=%s",
		            playlistUrl, playlistDisplayName, "PUBLIC", token);
		   
            errorUrl = url ;
	        HttpHeaders headers = new HttpHeaders();
	        HttpEntity<String> entity = new HttpEntity<>(headers);
	        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            logger.debug("Create Playlist: - {} - url - {}", response.getStatusCode(), url);
	        
	        if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                plnode = objectMapper.readTree(response.getBody());	  
                return plnode;
	        }
	    }  catch (InvalidKeyException | NoSuchAlgorithmException e) {
            // Log the exception
            e.printStackTrace();
            // You might want to throw a custom exception or handle it differently
            
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();	         
        } 

        return null;
    }

    public JsonNode getAllAllPlaylists(int first) {
        JsonNode playlistsNode = null;
        String url = "";
        try {
            String token = authTokenManager.getAuthToken();
            url = String.format("%s?scope=all&xt=%s&first=%d&max=10", playlistUrl, token, first);
            errorUrl = url;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            logger.debug("Get All Playlists: - {} - url - {}", response.getStatusCode(), url);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                playlistsNode = objectMapper.readTree(response.getBody());
                return playlistsNode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deleteAllPlaylists() {
        int first = 0;
        int total = 0;
        do {
            JsonNode playListNodeList =  getAllAllPlaylists(first);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
            total = (playListNodeList.get("totalcount") ==  null)? 0 : playListNodeList.get("totalcount").asInt();
            System.out.println("**********total*******  " + total );

            JsonNode listNode = playListNodeList.get("list");
            for (JsonNode node : listNode) {
                try {
                    if (node.has("name")) {
                        deletePlaylist(node.get("name").asText());
                    }
                } catch (Exception e) {
                    logger.error("Error deleting playlist: {}", e.getMessage());
                }
            }

        } while (total > 0);
    }

    public JsonNode deletePlaylist(String name) {
        try {
            
            String token = authTokenManager.getAuthToken();
            String url = String.format("%s/%s?xt=%s", playlistUrl, name, token);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            logger.error("Delete Playlist: - {}", url);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

            logger.error("Delete Playlist: - {} - url - {}", response.getStatusCode(), url);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                return objectMapper.readTree(response.getBody());
            } else {
                logger.error("Failed to delete playlist: {}", name);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error deleting playlist: {}", e.getMessage());
            return null;
        }
    } 

    public JsonNode getAllVideos(int first) {
        JsonNode playlistsNode = null;
        String url = "";
        try {
            String token = authTokenManager.getAuthToken();
            url = String.format("%s?scope=recent&xt=%s&first=%d&max=10", videoUrl, token, first);
            errorUrl = url;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            logger.debug("Get All Videos: - {} - url - {}", response.getStatusCode(), url);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                playlistsNode = objectMapper.readTree(response.getBody());
                return playlistsNode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deleteAllVideos() {
        int first = 0;
        int total = 0;
        do {
            JsonNode playListNodeList =  getAllVideos(first);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
            total = (playListNodeList.get("totalcount") ==  null)? 0 : playListNodeList.get("totalcount").asInt();
            System.out.println("**********total*******  " + total );

            JsonNode listNode = playListNodeList.get("list");
            for (JsonNode node : listNode) {
                try {
                    if (node.has("name")) {
                        deleteVideo(node.get("name").asText());
                    }
                } catch (Exception e) {
                    logger.error("Error Video playlist: {}", e.getMessage());
                }
            }

        } while (total > 0);
    }

    public JsonNode deleteVideo(String name) {
        try {
            
            String token = authTokenManager.getAuthToken();
            String url = String.format("%s/%s?xt=%s", videoUrl, name, token);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            logger.error("Delete Video: - {}", url);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

            logger.error("Delete Video: - {} - url - {}", response.getStatusCode(), url);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                return objectMapper.readTree(response.getBody());
            } else {
                logger.error("Failed to delete Video: {}", name);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error deleting Video: {}", e.getMessage());
            return null;
        }
    } 

    @Override
    public JsonNode addPlayListToChannel(int playlistid, String channelName) {
         // Implement the logic to add a playlist to a channel
        JsonNode respNode = null;
        String url = "";
        try {
            String token = authTokenManager.getAuthToken();
            url = String.format("%s/%s/content/%d?type=%s&xt=%s",
                    channelUrl, channelName, playlistid, "PLAYLIST", token);
            errorUrl = url ;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            
            logger.debug("Add Playlist to channel: - {} - url - {}", response.getStatusCode(), url);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                respNode = objectMapper.readTree(response.getBody());	    
                return respNode;   
            }

            System.out.println("\n\n addPlayListToChannel responsecode - " + response.getStatusCode());
        } catch (Exception e) {
            //e.printStackTrace();
            logger.debug("addPlayListToChannel error : "+e.getMessage());
        } 

        return null;
    }

    public boolean checkVideoExistsInPlaylist(String playlistName, String videoName, int first, int max) {
        // Implement the logic to check if the video exists in the playlist
        JsonNode plvNode = null;
        String url = "";
         try {
        	String token = authTokenManager.getAuthToken();
        	url = String.format("%s/%s/videos?xt=%s&first=%d&max=%d", playlistUrl, playlistName, token, first, max);
            errorUrl = url ;

            logger.debug("Check video exists in Playlist: -  url - {}",  url);

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers); 
            //System.out.println("checkVideoExistsInPlaylist:url: "+url +" playlistName: "+playlistName+" videoName: "+videoName);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
           
            logger.debug("Response to Check video exists in Playlist: - {} ", response.getStatusCode());
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JsonNode respNode = objectMapper.readTree(response.getBody());
                int total = (respNode.get("totalcount") == null) ? 0 : respNode.get("totalcount").asInt();
    
                JsonNode listNode = objectMapper.readTree(response.getBody()).get("list");
                for (JsonNode node : listNode) {
                     if (node.has("name") && node.get("name").asText().equals(videoName)) {
                         plvNode = node;
                        break;
                    }
               }
                if(plvNode != null)                {
                   
                    return true;
                }
                else if((first+max) < total)
                {
                    first = first+max;
                    return checkVideoExistsInPlaylist(playlistName, videoName, first, max);
                }
                else
                {
                    logger.debug("Video {} not found in play list : - {}", videoName, playlistName);
                    return false;
                }
               
            } 
        } catch (Exception e) {
          
           // e.printStackTrace();
           logger.debug("Error in check Video Exists In Playlist: "+playlistName+" videoName: "+videoName +" -- " + e.getMessage());
           return false;
        } 

        return true;
    }

    @Override
    public boolean checkVideoExistsInPlaylist(String playlistName, String videoName) {
        // Implement the logic to check if the video exists in the playlist
        return checkVideoExistsInPlaylist(playlistName, videoName, 0, 25);    
    }   
    
    private void pause(int millisesc) {
        try {
            Thread.sleep(millisesc); // 1 second delay
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for 1 second delay", e);
        }
    }

    @Override   
    public JsonNode checkAndUploadVideo(String ytUrl, String channelDisplayName, String language_code) {
        // Implement the logic to check and upload a video
        try {
            JsonNode channelNode = this.getChannel(channelDisplayName);

            //Todo return appropriate error code
            if(channelNode == null)
            {
                throw new KPChannleNotFoundException("Channel - " + channelDisplayName + " not found" );
            }
            
            String channelname = channelNode.get("name").asText();

            String playlistDisplayName = "PL_" + channelDisplayName+"_"+language_code;
            
            pause(1000);
            JsonNode playlistNode = this.getPlaylist(playlistDisplayName);
           
            String playlistname = "";
            JsonNode videoNode = null;
            if(playlistNode == null)
            {
                pause(1000);
                playlistNode = this.createPlaylist(playlistDisplayName);
                if(playlistNode == null) 
                {
                    throw new KPPlaylistCreationException("Play List" + playlistDisplayName + " not created" );
                } 

                playlistname = playlistNode.get("name").asText();

                pause(1000);
                videoNode = uploadVideo(ytUrl, playlistname);
                
                pause(1000);
                playlistNode = this.addPlayListToChannel(playlistNode.get("id").asInt(), channelname);
                    
                //Todo return appropriate error code
                if(playlistNode == null) 
                {
                    throw new KPAddPlayListToChannelException("Play List - " + playlistname + " not added to channel - " + channelname);
                } 
            }
            else
            {
               
                playlistname = playlistNode.get("name").asText();
                videoNode = uploadVideo(ytUrl, playlistname);
                if(!this.checkPlaylistExistsInChannel(playlistname, channelname))
                {
                    playlistNode = this.addPlayListToChannel(playlistNode.get("id").asInt(), channelname);
                    
                    //Todo return appropriate error code
                    if(playlistNode == null) 
                    {
                        throw new KPAddPlayListToChannelException("Play List - " + playlistname + " not added to channel - " + channelname );
                    } 
                }                 
            } 
          
            return videoNode;
        } 
        catch(KPAddPlayListToChannelException | KPVideoUploadException |  KPPlaylistCreationException | KPChannleNotFoundException e)
        {
           throw e;
        } catch (Exception e) {
           // e.printStackTrace();
           logger.debug("Error in checkAndUploadVideo: "+ytUrl);
            return null;
        } 
    }   
    
    private JsonNode uploadVideo(String ytUrl, String playlistname) {
            JsonNode videoNode = this.getVideo(ytUrl);
            String videoname = "";

            if(videoNode == null)
            {
                videoNode = this.uploadVideo(ytUrl);
                if(videoNode == null)
                {
                    throw new KPVideoUploadException("Video uploaded to KPoint failed for url: "+ytUrl);
                }
                
                videoname = videoNode.get("data").asText();               
            }
            else {
                videoname = videoNode.get("id").asText();
            }
            
            if(!this.checkVideoExistsInPlaylist(playlistname, videoname))
            {
                pause(1000);
                videoNode = this.addVideoToPlaylist(playlistname, videoname);
            }

            videoNode = getVideoDetails(videoname);
            return videoNode;
    }
    @Override
    public JsonNode getVideo(String ytUrl) {     
        
        String url = "";
		try {
			String token = authTokenManager.getAuthToken();
            String ytid = getytIdFromUrl(ytUrl);
            url = String.format("%s?scope=recent&facet.properties.youtube_id=%s&xt=%s",
            videoUrl, ytid, token);
    
            errorUrl = url ;

            HttpHeaders headers = new HttpHeaders();
	        HttpEntity<String> entity = new HttpEntity<>(headers);
	        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
	        
             
            logger.debug("Get video by YT Url: - {} - url - {}", response.getStatusCode(), url);
        
	        if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                JsonNode respNode = objectMapper.readTree(response.getBody());	  
                if(respNode != null && respNode.has("list") && respNode.get("list").size() > 0)
                {
                    return respNode.get("list").get(0);
                }
	        }

        } catch (Exception e) {
            e.printStackTrace();
        } 

        return null;
    }

    public JsonNode getVideoDetails(String videoName) {
        JsonNode respNode = null;
        String apiUrl = "";
        try {
            String token = authTokenManager.getAuthToken();
            apiUrl = String.format("%s/%s?&xt=%s", videoUrl, videoName, token);
            errorUrl = apiUrl ;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
           
            logger.debug("getVideoDetails: - {} - url - {}", response.getStatusCode(), apiUrl);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                respNode = objectMapper.readTree(response.getBody());	    
                return respNode;   
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.debug("getVideoDetails error : "+e.getMessage());
        } 

        return null;
    }

    @Override
    public JsonNode uploadVideo(String ytUrl) {
        // Implement the logic to upload a video
        JsonNode vNode = null;
        String url = "";
		try {
			String token = authTokenManager.getAuthToken();
			url = String.format("%s%s?xt=%s", videoUrl, "/import", token);
            errorUrl = url ;

          	HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			//Create request body
			JSONObject requestBody = new JSONObject();
	        requestBody.put("video_link", ytUrl);
	        
			HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);		
           
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            logger.debug("Upload video by YT Url: - {} - url - {}", response.getStatusCode(), url);
			if (response.getStatusCode() == HttpStatus.OK) {
				
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				vNode = objectMapper.readTree(response.getBody());	     
				return vNode;
			} 

             logger.debug("response to Upload video by YT Url"+ response.getStatusCode());

		} catch (Exception e) {
           // e.printStackTrace();
           logger.debug("uploadVideo error: " + e.getMessage());
		} 
		
        return null;
    }

    @Override
    public JsonNode addVideoToPlaylist(String playlistName, String videoName) {
        // Implement the logic to add a video to a playlist
        String url = "";
        try {
	    	String token = authTokenManager.getAuthToken();
	    	url = String.format("%s/%s/videos/%s?xt=%s", playlistUrl, playlistName, videoName, token);
            errorUrl = url ;
            logger.debug("Add Video to Playlist: - url - {}",  url);		

	        HttpHeaders headers = new HttpHeaders();	        
	        HttpEntity<String> entity = new HttpEntity<>(null, headers); 
	        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            
            logger.debug("Response to Add Video to Playlist: - {} ", response.getStatusCode());		
	        if (response.getStatusCode() == HttpStatus.OK) {
	            ObjectMapper objectMapper = new ObjectMapper();
	            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	            return objectMapper.readTree(response.getBody());
	        }
             System.out.println(" \n\n response.getStatusCode() addVideoToPlaylist " + response.getStatusCode());

	    } catch (Exception e) {
	       // e.printStackTrace();
           logger.debug("Error in addVideoToPlaylist: "+playlistName+" videoName: "+videoName +" -- " + e.getMessage());
	    }
        return null;
    }
    
    //helper function to get ytId from url
    private String getytIdFromUrl(String url) {
        String youtube_id="";
        URI uri;
       
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        String path = uri.getPath();
        String[] pathSegments = path.split("/");
        
        if(pathSegments[pathSegments.length -1].equals("watch")){
            String query = uri.getQuery();
            String[] pairs = query.split("&");
            String[] keyValue = pairs[0].split("=");
            youtube_id = keyValue[1];
        }
        else
        {
            youtube_id = pathSegments[pathSegments.length -1];
        }
        return youtube_id;
    }

    @Service
    private static class AuthTokenManager {       
        
        @Value("${kpoint.client.id}")
        private String clientId;

        @Value("${kpoint.client.secret}")
        private String clientSecret;

        @Value("${kpoint.client.displayname}")
        private String userName;
        
        @Value("${kpoint.client.email}")
        private String userEmail;

        public String getAuthToken() throws InvalidKeyException, NoSuchAlgorithmException {
            long challenge = System.currentTimeMillis() / 1000;
            String data = clientId + ":" + userEmail + ":" + userName + ":" + challenge;

            String xauthToken = stringToBase64HMACMD5(data);
            String xtEncode = "client_id=" + clientId + "&user_email=" + userEmail + "&user_name=" + userName + "&challenge="
                    + challenge + "&xauth_token=" + xauthToken;

            String xt = Base64.encodeBase64URLSafeString(xtEncode.getBytes());
            return xt;
        }

        public String getAuthToken(String email, String displayName) throws InvalidKeyException, NoSuchAlgorithmException {
            long challenge = System.currentTimeMillis() / 1000;
            String data = clientId + ":" + email + ":" + displayName + ":" + challenge;

            String xauthToken = stringToBase64HMACMD5(data);
            String xtEncode = "client_id=" + clientId + "&user_email=" + email + "&user_name=" + displayName + "&challenge="
                    + challenge + "&xauth_token=" + xauthToken;

            String xt = Base64.encodeBase64URLSafeString(xtEncode.getBytes());
            return xt;
        }

        private String stringToBase64HMACMD5(String message) throws NoSuchAlgorithmException, InvalidKeyException {
            SecretKeySpec keySpec = new SecretKeySpec(clientSecret.getBytes(), "HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(message.getBytes());
            return Base64.encodeBase64URLSafeString(rawHmac);
        }
    }
    
    
    public void deleteAllVideos(String languageCode) {
        int first = 0;
        int max = 100;
        boolean hasMore = true;

        while (hasMore) {
            JsonNode videosNode = getAllVideos(first, max, languageCode);
            if (videosNode == null || !videosNode.has("list") || videosNode.get("list").size() == 0) {
                hasMore = false;
                continue;
            }

            JsonNode listNode = videosNode.get("list");
            int itemsProcessed = 0;
            for (JsonNode node : listNode) {
                try {
                    if (node.has("name")) {
                        deleteVideo(node.get("name").asText());
                        itemsProcessed++;
                    }
                } catch (Exception e) {
                    logger.error("Error deleting video: {}", e.getMessage());
                }
            }

            first += itemsProcessed; // Increment by actual number of items processed
            
            // Check if there are more items and if totalcount is available
            hasMore = videosNode.has("totalcount") && first < videosNode.get("totalcount").asInt();
            
            // Safeguard in case totalcount is missing
            if (!videosNode.has("totalcount") && itemsProcessed == max) {
                hasMore = true; // Assume there might be more if we processed max items
            }
        }
    }

    private JsonNode getAllVideos(int first, int max, String languageCode) {
        String url = "";
        try {
            String token = authTokenManager.getAuthToken();
            url = String.format("%s?scope=recent&xt=%s&first=%d&max=%d&facet.language=%s", videoUrl, token, first, max, languageCode);
            errorUrl = url;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            logger.debug("Get All Videos: - {} - url - {}", response.getStatusCode(), url);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            logger.error("Error getting videos: {}", e.getMessage());
        }

        return null;
    }

    public void deleteAllPlaylistsForChannel(String channelDisplayName) {
        try {
            JsonNode channelNode = this.getChannel(channelDisplayName);
            if (channelNode == null) {
                throw new KPChannleNotFoundException("Channel - " + channelDisplayName + " not found");
            }

            String channelName = channelNode.get("name").asText();
            int first = 0;
            int max = 100;
            boolean hasMore = true;

            while (hasMore) {
                JsonNode playlistsNode = getPlaylistsForChannel(channelName, first, max);
                if (playlistsNode == null || !playlistsNode.has("list") || playlistsNode.get("list").size() == 0) {
                    hasMore = false;
                    continue;
                }

                JsonNode listNode = playlistsNode.get("list");
                for (JsonNode node : listNode) {
                    try {
                        if (node.has("name")) {
                            deletePlaylist(node.get("name").asText());
                        }
                    } catch (Exception e) {
                        logger.error("Error deleting playlist: {}", e.getMessage());
                    }
                }

                first += max;
                hasMore = playlistsNode.has("totalcount") && first < playlistsNode.get("totalcount").asInt();
            }

            logger.info("Finished deleting all playlists for channel: {}", channelDisplayName);
        } catch (KPChannleNotFoundException e) {
            logger.error("Channel not found: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting playlists for channel: {}", e.getMessage());
        }
    }

    private JsonNode getPlaylistsForChannel(String channelName, int first, int max) {
        String url = "";
        try {
            String token = authTokenManager.getAuthToken();
            url = String.format("%s/%s/content?type=playlists&xt=%s&first=%d&max=%d", 
                                channelUrl, channelName, token, first, max);
            errorUrl = url;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            logger.debug("Get Playlists for Channel: - {} - url - {}", response.getStatusCode(), url);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            logger.error("Error getting playlists for channel: {}", e.getMessage());
        }

        return null;
    }

}
