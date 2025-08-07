package com.reproductormusica.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa una lista de reproducción
 */
public class Playlist {
    private String id;
    private String name;
    private String description;
    private List<Song> songs;
    private long createdDate;
    private long modifiedDate;
    
    public Playlist() {
        this.id = UUID.randomUUID().toString();
        this.songs = new ArrayList<>();
        this.createdDate = System.currentTimeMillis();
        this.modifiedDate = this.createdDate;
    }
    
    public Playlist(String name) {
        this();
        this.name = name;
    }
    
    public void addSong(Song song) {
        if (song != null && !songs.contains(song)) {
            songs.add(song);
            updateModifiedDate();
        }
    }
    
    public void removeSong(Song song) {
        if (songs.remove(song)) {
            updateModifiedDate();
        }
    }
    
    public void removeSong(int index) {
        if (index >= 0 && index < songs.size()) {
            songs.remove(index);
            updateModifiedDate();
        }
    }
    
    public void clearSongs() {
        if (!songs.isEmpty()) {
            songs.clear();
            updateModifiedDate();
        }
    }
    
    public void moveSong(int fromIndex, int toIndex) {
        if (fromIndex >= 0 && fromIndex < songs.size() && 
            toIndex >= 0 && toIndex < songs.size()) {
            Song song = songs.remove(fromIndex);
            songs.add(toIndex, song);
            updateModifiedDate();
        }
    }
    
    private void updateModifiedDate() {
        this.modifiedDate = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        updateModifiedDate();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description; 
        updateModifiedDate();
    }
    
    public List<Song> getSongs() { return new ArrayList<>(songs); }
    public void setSongs(List<Song> songs) { 
        this.songs = new ArrayList<>(songs); 
        updateModifiedDate();
    }
    
    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    
    public long getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(long modifiedDate) { this.modifiedDate = modifiedDate; }
    
    public int size() {
        return songs.size();
    }
    
    public boolean isEmpty() {
        return songs.isEmpty();
    }
    
    @Override
    public String toString() {
        return name + " (" + songs.size() + " songs)";
    }
}
