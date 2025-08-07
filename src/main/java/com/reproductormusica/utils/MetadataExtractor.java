package com.reproductormusica.utils;

import com.reproductormusica.model.Song;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * Utilidad para extraer metadatos de archivos de audio
 */
public class MetadataExtractor {
    
    public static Song extractMetadata(File audioFile) {
        Song song = new Song();
        song.setId(UUID.randomUUID().toString());
        song.setFilePath(audioFile.getAbsolutePath());
        
        try {
            AudioFile f = AudioFileIO.read(audioFile);
            Tag tag = f.getTag();
            
            if (tag != null) {
                song.setTitle(getOrDefault(tag.getFirst(FieldKey.TITLE), audioFile.getName()));
                song.setArtist(getOrDefault(tag.getFirst(FieldKey.ARTIST), "Unknown Artist"));
                song.setAlbum(getOrDefault(tag.getFirst(FieldKey.ALBUM), "Unknown Album"));
                song.setGenre(getOrDefault(tag.getFirst(FieldKey.GENRE), "Unknown"));
                
                // Track number
                String trackStr = tag.getFirst(FieldKey.TRACK);
                if (trackStr != null && !trackStr.isEmpty()) {
                    try {
                        // Handle formats like "1/12" or just "1"
                        String[] parts = trackStr.split("/");
                        song.setTrackNumber(Integer.parseInt(parts[0]));
                    } catch (NumberFormatException e) {
                        song.setTrackNumber(0);
                    }
                }
                
                // Year
                String yearStr = tag.getFirst(FieldKey.YEAR);
                if (yearStr != null && !yearStr.isEmpty()) {
                    try {
                        song.setYear(Integer.parseInt(yearStr));
                    } catch (NumberFormatException e) {
                        song.setYear(0);
                    }
                }
            } else {
                // No metadata available, use filename
                song.setTitle(removeExtension(audioFile.getName()));
                song.setArtist("Unknown Artist");
                song.setAlbum("Unknown Album");
            }
            
            // Duration
            if (f.getAudioHeader() != null) {
                int durationSeconds = f.getAudioHeader().getTrackLength();
                song.setDuration(Duration.ofSeconds(durationSeconds));
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting metadata from: " + audioFile.getName());
            e.printStackTrace();
            
            // Fallback to filename
            song.setTitle(removeExtension(audioFile.getName()));
            song.setArtist("Unknown Artist");
            song.setAlbum("Unknown Album");
        }
        
        return song;
    }
    
    private static String getOrDefault(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }
    
    private static String removeExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }
    
    /**
     * Verifica si un archivo es un formato de audio soportado
     */
    public static boolean isSupportedAudioFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || 
               name.endsWith(".wav") || 
               name.endsWith(".flac") || 
               name.endsWith(".ogg") || 
               name.endsWith(".m4a") || 
               name.endsWith(".aac") ||
               name.endsWith(".wma");
    }
}
