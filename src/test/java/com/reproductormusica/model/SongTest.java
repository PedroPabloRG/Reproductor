package com.reproductormusica.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Song
 */
class SongTest {
    
    private Song song;
    
    @BeforeEach
    void setUp() {
        song = new Song("1", "Test Song", "Test Artist", "/path/to/song.mp3");
    }
    
    @Test
    void testSongCreation() {
        assertNotNull(song);
        assertEquals("1", song.getId());
        assertEquals("Test Song", song.getTitle());
        assertEquals("Test Artist", song.getArtist());
        assertEquals("/path/to/song.mp3", song.getFilePath());
    }
    
    @Test
    void testSongEquality() {
        Song song2 = new Song("1", "Different Title", "Different Artist", "/different/path.mp3");
        assertEquals(song, song2, "Songs with same ID should be equal");
        
        Song song3 = new Song("2", "Test Song", "Test Artist", "/path/to/song.mp3");
        assertNotEquals(song, song3, "Songs with different ID should not be equal");
    }
    
    @Test
    void testToString() {
        String expected = "Test Artist - Test Song";
        assertEquals(expected, song.toString());
    }
    
    @Test
    void testNullValues() {
        Song emptySong = new Song();
        emptySong.setId("test");
        
        String expected = "Unknown - Unknown";
        assertEquals(expected, emptySong.toString());
    }
}
