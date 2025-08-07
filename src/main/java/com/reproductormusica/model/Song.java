package com.reproductormusica.model;

import java.time.Duration;
import java.util.Objects;

/**
 * Representa una canción en el reproductor
 */
public class Song {
    private String id;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private Duration duration;
    private String filePath;
    private String albumArt;
    private int trackNumber;
    private int year;
    private String lyrics;
    
    public Song() {}
    
    public Song(String id, String title, String artist, String filePath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public Duration getDuration() { return duration; }
    public void setDuration(Duration duration) { this.duration = duration; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getAlbumArt() { return albumArt; }
    public void setAlbumArt(String albumArt) { this.albumArt = albumArt; }
    
    public int getTrackNumber() { return trackNumber; }
    public void setTrackNumber(int trackNumber) { this.trackNumber = trackNumber; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s", artist != null ? artist : "Unknown", 
                           title != null ? title : "Unknown");
    }
}
