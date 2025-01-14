package com.tl.reap_admin_api.service;

import com.fasterxml.jackson.databind.JsonNode;


public interface ThirdPartyVideoService {
    public JsonNode getChannel(String channelDisplayName);
    public JsonNode getPlaylist(String playlistDisplayName);
    public boolean checkPlaylistExistsInChannel(String playlistDisplayName, String channelName);
    public JsonNode createPlaylist(String playlistDisplayName);
    public JsonNode addPlayListToChannel(int playlistId, String channelName);
    public JsonNode checkAndUploadVideo(String ytUrl, String playlistDisplayName, String channelDisplayName);
    public boolean checkVideoExistsInPlaylist(String playlistName, String videoName);
    public JsonNode uploadVideo(String url);
    public JsonNode addVideoToPlaylist(String playlistName, String videoName);
    public JsonNode getVideo(String url);
}
