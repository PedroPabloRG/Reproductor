package com.reproductormusica.view;

import com.reproductormusica.controller.MainController;
import com.reproductormusica.model.PlaybackState;
import com.reproductormusica.model.Playlist;
import com.reproductormusica.model.RepeatMode;
import com.reproductormusica.model.Song;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Ventana principal de la aplicación
 */
public class MainWindow {
    
    private final Stage primaryStage;
    private final MainController controller;
    
    // UI Components
    private Label songTitleLabel;
    private Label artistLabel;
    private Button playPauseButton;
    private Button stopButton;
    private Button previousButton;
    private Button nextButton;
    private Slider volumeSlider;
    private Slider progressSlider;
    private Label totalTimeLabel;
    private Label volumeValue;
    private ListView<Song> libraryListView;
    private ListView<Song> queueListView;
    private ListView<Playlist> playlistsListView;
    private ListView<Song> playlistContentListView;
    private TabPane tabPane;
    private Label playlistStatusMessage;
    private Button editPlaylistButton;
    private Button deletePlaylistButton;
    
    // Lyrics components
    private TextArea lyricsTextArea;
    private Label currentSongLyricsLabel;
    
    // Menu items
    private MenuItem importFiles;
    private MenuItem importFolder;
    
    public MainWindow(Stage primaryStage, MainController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
        
        setupUI();
        setupBindings();
        setupEventHandlers();
    }
    
    private void setupUI() {
        // Create main layout
        BorderPane mainLayout = new BorderPane();
        
        // Top: Menu bar
        MenuBar menuBar = createMenuBar();
        mainLayout.setTop(menuBar);
        
        // Center: Main content with tabs
        tabPane = createMainContent();
        mainLayout.setCenter(tabPane);
        
        // Bottom: Player controls
        VBox playerControls = createPlayerControls();
        mainLayout.setBottom(playerControls);
        
        // Create scene with optimized size
        Scene scene = new Scene(mainLayout, 1200, 650);
        
        // Load CSS safely
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("No se pudo cargar el archivo CSS, usando estilos por defecto");
        }
        
        primaryStage.setTitle("Reproductor de Música - Java");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(580);
        primaryStage.setMaximized(false);
        
        // Centrar la ventana en la pantalla
        primaryStage.centerOnScreen();
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File menu
        Menu fileMenu = new Menu("Archivo");
        importFiles = new MenuItem("Importar archivos...");
        importFolder = new MenuItem("Importar carpeta...");
        MenuItem exit = new MenuItem("Salir");
        
        // Setup file menu event handlers
        setupFileMenuHandlers();
        
        fileMenu.getItems().addAll(importFiles, importFolder, new SeparatorMenuItem(), exit);
        
        // Playlist menu
        Menu playlistMenu = new Menu("Lista de reproducción");
        MenuItem newPlaylist = new MenuItem("Nueva lista...");
        
        // Setup playlist menu event handlers
        newPlaylist.setOnAction(e -> showCreatePlaylistDialog());
        
        playlistMenu.getItems().add(newPlaylist);
        
        // View menu
        Menu viewMenu = new Menu("Ver");
        CheckMenuItem showQueue = new CheckMenuItem("Mostrar cola");
        showQueue.setSelected(true);
        viewMenu.getItems().add(showQueue);
        
        // Help menu
        Menu helpMenu = new Menu("Ayuda");
        MenuItem about = new MenuItem("Acerca de...");
        helpMenu.getItems().add(about);
        
        menuBar.getMenus().addAll(fileMenu, playlistMenu, viewMenu, helpMenu);
        return menuBar;
    }
    
    private TabPane createMainContent() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Library tab
        Tab libraryTab = new Tab("📚 Biblioteca Musical");
        libraryTab.setClosable(false);
        
        VBox libraryContent = new VBox(15);
        libraryContent.setPadding(new Insets(20));
        
        // Header section
        Label libraryHeader = new Label("Tu Biblioteca Musical");
        libraryHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Buscar canciones, artistas, álbumes...");
        searchField.setPrefHeight(35);
        searchField.setStyle("-fx-font-size: 14px;");
        
        // Library list
        libraryListView = new ListView<>();
        libraryListView.setCellFactory(lv -> new SongListCell());
        libraryListView.setPrefHeight(320);
        libraryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        VBox.setVgrow(libraryListView, Priority.ALWAYS);
        
        // Add context menu for right-click actions
        setupLibraryContextMenu();
        
        // Setup search functionality (after ListView is created)
        setupSearchField(searchField);
        
        // Library controls for multiple selection
        HBox libraryControls = new HBox(10);
        libraryControls.setAlignment(Pos.CENTER_LEFT);
        
        Button addToQueueButton = new Button("➕ Agregar a Cola");
        addToQueueButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Button playSelectedButton = new Button("▶ Reproducir Selección");
        playSelectedButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Button selectAllButton = new Button("☑ Seleccionar Todo");
        selectAllButton.setStyle("-fx-font-size: 12px;");
        
        Button clearSelectionButton = new Button("✖ Limpiar Selección");
        clearSelectionButton.setStyle("-fx-font-size: 12px;");
        
        Label selectionCountLabel = new Label("0 canciones seleccionadas");
        selectionCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        libraryControls.getChildren().addAll(
            addToQueueButton, playSelectedButton, 
            new Separator(), selectAllButton, clearSelectionButton,
            new Region(), selectionCountLabel
        );
        
        // Set up selection controls handlers
        setupLibrarySelectionHandlers(addToQueueButton, playSelectedButton, 
                                    selectAllButton, clearSelectionButton, selectionCountLabel);
        
        // Add sample data message
        Label emptyMessage = new Label("No hay canciones en la biblioteca.\nUse 'Archivo > Importar archivos...' para agregar música.");
        emptyMessage.setStyle("-fx-text-fill: #666; -fx-text-alignment: center;");
        
        libraryContent.getChildren().addAll(libraryHeader, searchField, libraryListView, libraryControls, emptyMessage);
        libraryTab.setContent(libraryContent);
        
        // Queue tab
        Tab queueTab = new Tab("🎵 Cola de Reproducción");
        queueTab.setClosable(false);
        
        VBox queueContent = new VBox(15);
        queueContent.setPadding(new Insets(20));
        
        // Queue header
        Label queueHeader = new Label("Cola de Reproducción");
        queueHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        queueListView = new ListView<>();
        queueListView.setCellFactory(lv -> new SongListCell());
        queueListView.setPrefHeight(320);
        queueListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        VBox.setVgrow(queueListView, Priority.ALWAYS);
        
        // Add context menu for queue
        setupQueueContextMenu();
        
        // Queue controls
        HBox queueControls = new HBox(10);
        Button clearQueueButton = new Button("🗑️ Limpiar Cola");
        Button shuffleQueueButton = new Button("🔀 Mezclar");
        Button removeFromQueueButton = new Button("➖ Eliminar Selección");
        removeFromQueueButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Label queueSelectionLabel = new Label("0 canciones seleccionadas");
        queueSelectionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        queueControls.getChildren().addAll(clearQueueButton, shuffleQueueButton, removeFromQueueButton, 
                                         new Region(), queueSelectionLabel);
        
        // Setup queue controls handlers
        setupQueueControlsHandlers(clearQueueButton, shuffleQueueButton, 
                                 removeFromQueueButton, queueSelectionLabel);
        
        // Empty queue message
        Label emptyQueueMessage = new Label("La cola de reproducción está vacía.\nAgrega canciones desde la biblioteca.");
        emptyQueueMessage.setStyle("-fx-text-fill: #666; -fx-text-alignment: center;");
        
        queueContent.getChildren().addAll(queueHeader, queueListView, queueControls, emptyQueueMessage);
        queueTab.setContent(queueContent);
        
        // Playlists tab
        Tab playlistsTab = new Tab("📝 Listas de Reproducción");
        playlistsTab.setClosable(false);
        
        // Create split pane for playlists and content
        SplitPane playlistSplitPane = new SplitPane();
        playlistSplitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        playlistSplitPane.setDividerPositions(0.4); // 40% for playlist list, 60% for content
        
        // Left side: Playlist list
        VBox playlistsListSection = new VBox(10);
        playlistsListSection.setPadding(new Insets(15));
        playlistsListSection.setPrefWidth(300);
        
        Label playlistsHeader = new Label("Mis Listas");
        playlistsHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        playlistsListView = new ListView<>();
        playlistsListView.setPrefHeight(400);
        VBox.setVgrow(playlistsListView, Priority.ALWAYS);
        
        // Custom cell factory for playlists to show more info
        playlistsListView.setCellFactory(listView -> new ListCell<Playlist>() {
            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);
                if (empty || playlist == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(2);
                    Label nameLabel = new Label(playlist.getName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    vbox.getChildren().add(nameLabel);
                    
                    Label songsLabel = new Label(playlist.getSongs().size() + " canción" + 
                        (playlist.getSongs().size() != 1 ? "es" : ""));
                    songsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
                    vbox.getChildren().add(songsLabel);
                    
                    if (playlist.getDescription() != null && !playlist.getDescription().trim().isEmpty()) {
                        Label descLabel = new Label(playlist.getDescription());
                        descLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11px;");
                        descLabel.setWrapText(true);
                        vbox.getChildren().add(descLabel);
                    }
                    
                    setGraphic(vbox);
                }
            }
        });
        
        HBox playlistControls = new HBox(10);
        Button newPlaylistButton = new Button("➕ Nueva Lista");
        editPlaylistButton = new Button("✏️ Editar");
        deletePlaylistButton = new Button("🗑️ Eliminar");
        
        newPlaylistButton.setOnAction(e -> showCreatePlaylistDialog());
        editPlaylistButton.setOnAction(e -> showEditPlaylistDialog());
        deletePlaylistButton.setOnAction(e -> showDeletePlaylistDialog());
        
        // Initially disable edit and delete buttons
        editPlaylistButton.setDisable(true);
        deletePlaylistButton.setDisable(true);
        
        playlistControls.getChildren().addAll(newPlaylistButton, editPlaylistButton, deletePlaylistButton);
        
        playlistStatusMessage = new Label("📝 Aún no tienes listas de reproducción\n✨ Crea tu primera lista para organizar tus canciones favoritas");
        playlistStatusMessage.setStyle("-fx-text-fill: #666; -fx-text-alignment: center; -fx-font-size: 12px;");
        
        playlistsListSection.getChildren().addAll(playlistsHeader, playlistsListView, playlistControls, playlistStatusMessage);
        
        // Right side: Playlist content
        VBox playlistContentSection = new VBox(10);
        playlistContentSection.setPadding(new Insets(15));
        
        Label playlistContentHeader = new Label("Contenido de la Lista");
        playlistContentHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label selectedPlaylistInfo = new Label("Selecciona una lista para ver su contenido");
        selectedPlaylistInfo.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
        
        playlistContentListView = new ListView<>();
        playlistContentListView.setCellFactory(lv -> new SongListCell());
        playlistContentListView.setPrefHeight(350);
        playlistContentListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        VBox.setVgrow(playlistContentListView, Priority.ALWAYS);
        
        // Setup context menu for playlist content
        setupPlaylistContentContextMenu();
        
        // Playlist content double-click to play from selected song
        playlistContentListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Song selectedSong = playlistContentListView.getSelectionModel().getSelectedItem();
                Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
                
                if (selectedSong != null && selectedPlaylist != null) {
                    // Play the playlist starting from the selected song
                    controller.playPlaylistFromSong(selectedSong, selectedPlaylist);
                }
            }
        });
        
        // Playlist content controls
        HBox playlistContentControls = new HBox(10);
        Button addToQueueAllButton = new Button("➕ Agregar Todo a Cola");
        addToQueueAllButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Button removeSelectedButton = new Button("➖ Eliminar Selección");
        removeSelectedButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Button moveUpButton = new Button("⬆ Mover Arriba");
        moveUpButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Button moveDownButton = new Button("⬇ Mover Abajo");
        moveDownButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Button clearPlaylistButton = new Button("🗑️ Vaciar Lista");
        clearPlaylistButton.setStyle("-fx-font-size: 12px;");
        
        Label playlistSelectionLabel = new Label("0 canciones seleccionadas");
        playlistSelectionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        playlistContentControls.getChildren().addAll(
            addToQueueAllButton, 
            new Separator(), removeSelectedButton, moveUpButton, moveDownButton,
            new Separator(), clearPlaylistButton,
            new Region(), playlistSelectionLabel
        );
        
        // Setup playlist content controls handlers
        setupPlaylistContentHandlers(addToQueueAllButton, removeSelectedButton, 
                                   moveUpButton, moveDownButton, clearPlaylistButton, playlistSelectionLabel, selectedPlaylistInfo);
        
        playlistContentSection.getChildren().addAll(
            playlistContentHeader, selectedPlaylistInfo, playlistContentListView, 
            playlistContentControls
        );
        
        // Add both sections to split pane
        playlistSplitPane.getItems().addAll(playlistsListSection, playlistContentSection);
        
        playlistsTab.setContent(playlistSplitPane);
        
        // Lyrics tab
        Tab lyricsTab = new Tab("🎤 Lyrics");
        lyricsTab.setClosable(false);
        
        VBox lyricsContent = new VBox(15);
        lyricsContent.setPadding(new Insets(20));
        
        // Lyrics header
        Label lyricsHeader = new Label("Letras de Canciones");
        lyricsHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Current song info for lyrics
        currentSongLyricsLabel = new Label("Selecciona una canción para ver sus letras");
        currentSongLyricsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-font-weight: bold;");
        
        // Lyrics text area
        lyricsTextArea = new TextArea();
        lyricsTextArea.setPromptText("🎵 Las letras de la canción aparecerán aquí...\n\n¡Disfruta de la música!");
        lyricsTextArea.setWrapText(true);
        lyricsTextArea.setEditable(false);
        lyricsTextArea.setPrefHeight(400);
        lyricsTextArea.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial'; -fx-control-inner-background: #f9f9f9;");
        VBox.setVgrow(lyricsTextArea, Priority.ALWAYS);
        
        // Lyrics controls
        HBox lyricsControls = new HBox(10);
        lyricsControls.setAlignment(Pos.CENTER_LEFT);
        
        Button searchLyricsButton = new Button("🔍 Buscar Letras");
        searchLyricsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Button editLyricsButton = new Button("✏️ Editar Letras");
        editLyricsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Button clearLyricsButton = new Button("🗑️ Limpiar");
        clearLyricsButton.setStyle("-fx-font-size: 12px;");
        
        lyricsControls.getChildren().addAll(searchLyricsButton, editLyricsButton, clearLyricsButton);
        
        // Setup lyrics controls handlers
        setupLyricsControlsHandlers(searchLyricsButton, editLyricsButton, clearLyricsButton);
        
        // Instructions label
        Label instructionsLabel = new Label("💡 Tip: Las letras se actualizarán automáticamente cuando cambies de canción.");
        instructionsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        
        lyricsContent.getChildren().addAll(lyricsHeader, currentSongLyricsLabel, lyricsTextArea, lyricsControls, instructionsLabel);
        lyricsTab.setContent(lyricsContent);
        
        tabPane.getTabs().addAll(libraryTab, queueTab, playlistsTab, lyricsTab);
        return tabPane;
    }
    
    private VBox createPlayerControls() {
        VBox playerControls = new VBox(12);
        playerControls.setPadding(new Insets(15));
        playerControls.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f8f8, #e8e8e8); -fx-border-color: #d0d0d0; -fx-border-width: 1 0 0 0;");
        
        // Song info section (optimized)
        HBox songInfo = new HBox(15);
        songInfo.setAlignment(Pos.CENTER_LEFT);
        songInfo.setPrefHeight(50);
        
        VBox songDetails = new VBox(5);
        songTitleLabel = new Label("♪ Selecciona una canción para reproducir");
        songTitleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");
        artistLabel = new Label("Usa la biblioteca para elegir tu música favorita");
        artistLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        songDetails.getChildren().addAll(songTitleLabel, artistLabel);
        
        songInfo.getChildren().add(songDetails);
        
        // Progress section (larger)
        HBox progressBox = new HBox(15);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPrefHeight(40);
        
        progressSlider = new Slider(0, 1, 0);
        progressSlider.setPrefWidth(500);
        progressSlider.setPrefHeight(20);
        totalTimeLabel = new Label("0:00");
        totalTimeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        progressBox.getChildren().addAll(progressSlider, totalTimeLabel);
        
        // Control buttons (optimized size)
        HBox controlButtons = new HBox(18);
        controlButtons.setAlignment(Pos.CENTER);
        controlButtons.setPrefHeight(60);
        
        previousButton = new Button("⏮");
        previousButton.setPrefSize(50, 50);
        previousButton.setStyle("-fx-font-size: 16px; -fx-background-radius: 25; -fx-border-radius: 25;");
        
        playPauseButton = new Button("▶");
        playPauseButton.setPrefSize(60, 60);
        playPauseButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 30; -fx-border-radius: 30;");
        
        stopButton = new Button("⏹");
        stopButton.setPrefSize(50, 50);
        stopButton.setStyle("-fx-font-size: 16px; -fx-background-radius: 25; -fx-border-radius: 25;");
        
        nextButton = new Button("⏭");
        nextButton.setPrefSize(50, 50);
        nextButton.setStyle("-fx-font-size: 16px; -fx-background-radius: 25; -fx-border-radius: 25;");
        
        
        // Add shuffle and repeat buttons
        Button shuffleButton = new Button("🔀");
        shuffleButton.setPrefSize(40, 40);
        shuffleButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 20; -fx-border-radius: 20;");
        
        Button repeatButton = new Button("🔁");
        repeatButton.setPrefSize(40, 40);
        repeatButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 20; -fx-border-radius: 20;");
        
        // Setup shuffle and repeat button handlers
        setupShuffleRepeatButtons(shuffleButton, repeatButton);
        
        controlButtons.getChildren().addAll(shuffleButton, previousButton, playPauseButton, stopButton, nextButton, repeatButton);
        
        // Volume control (improved)
        HBox volumeBox = new HBox(15);
        volumeBox.setAlignment(Pos.CENTER_RIGHT);
        volumeBox.setPrefWidth(200);
        
        Label volumeLabel = new Label("🔊");
        volumeLabel.setStyle("-fx-font-size: 16px;");
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(120);
        volumeSlider.setPrefHeight(25);
        volumeValue = new Label("50%");
        volumeValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        volumeBox.getChildren().addAll(volumeLabel, volumeSlider, volumeValue);
        
        // Combine all controls
        HBox allControls = new HBox(25);
        allControls.setAlignment(Pos.CENTER);
        allControls.setPrefHeight(65);
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        allControls.getChildren().addAll(spacer1, controlButtons, spacer2, volumeBox);
        
        playerControls.getChildren().addAll(songInfo, progressBox, allControls);
        return playerControls;
    }
    
    private void setupBindings() {
        // Bind library list
        libraryListView.setItems(controller.getLibrary());
        
        // Bind queue list
        queueListView.setItems(controller.getCurrentQueue());
        
        // Bind playlists list
        playlistsListView.setItems(controller.getPlaylists());
        
        // Bind current song info
        controller.currentSongProperty().addListener((obs, oldSong, newSong) -> {
            if (newSong != null) {
                songTitleLabel.setText(newSong.getTitle());
                artistLabel.setText(newSong.getArtist());
                // Reset time label when song changes
                updateCurrentTimeLabel(0.0);
                // Update lyrics for new song
                updateLyricsForCurrentSong(newSong);
            } else {
                songTitleLabel.setText("No hay canción seleccionada");
                artistLabel.setText("");
                totalTimeLabel.setText("0:00");
                // Clear lyrics when no song is selected
                updateLyricsForCurrentSong(null);
            }
        });
        
        // Bind playback state
        controller.playbackStateProperty().addListener((obs, oldState, newState) -> {
            if (newState == PlaybackState.PLAYING) {
                playPauseButton.setText("⏸");
            } else {
                playPauseButton.setText("▶");
            }
        });
        
        // Bind volume
        volumeSlider.valueProperty().bindBidirectional(controller.volumeProperty());
        
        // Update volume label in real time
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeValue.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
        });
        
        // Bind progress
        controller.progressProperty().addListener((obs, oldProgress, newProgress) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(newProgress.doubleValue());
            }
            
            // Update time label with current playback time
            updateCurrentTimeLabel(newProgress.doubleValue());
        });
    }
    
    private void setupEventHandlers() {
        // Play/Pause button
        playPauseButton.setOnAction(e -> {
            if (controller.playbackStateProperty().get() == PlaybackState.PLAYING) {
                controller.pause();
            } else {
                controller.play();
            }
        });
        
        // Stop button
        stopButton.setOnAction(e -> controller.stop());
        
        // Previous/Next buttons
        previousButton.setOnAction(e -> controller.previous());
        nextButton.setOnAction(e -> controller.next());
        
        // Progress slider
        progressSlider.setOnMousePressed(e -> {
            controller.seek(progressSlider.getValue());
        });
        
        progressSlider.setOnMouseDragged(e -> {
            controller.seek(progressSlider.getValue());
        });
        
        // Volume slider interactions
        volumeSlider.setOnMousePressed(e -> {
            // Immediately update volume when pressed
            controller.setVolume(volumeSlider.getValue());
        });
        
        volumeSlider.setOnMouseDragged(e -> {
            // Update volume while dragging
            controller.setVolume(volumeSlider.getValue());
        });
        
        volumeSlider.setOnMouseReleased(e -> {
            // Final update when released
            controller.setVolume(volumeSlider.getValue());
        });
        
        // Library double-click to play
        libraryListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Song selectedSong = libraryListView.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    // If multiple songs are selected, play all selected songs
                    List<Song> selectedSongs = new java.util.ArrayList<>(
                        libraryListView.getSelectionModel().getSelectedItems());
                    
                    if (selectedSongs.size() > 1) {
                        controller.playSelectedSongs(selectedSongs);
                    } else {
                        controller.playNow(selectedSong);
                    }
                }
            }
        });
        
        // Window close event
        primaryStage.setOnCloseRequest(e -> {
            controller.shutdown();
        });
        
        // Playlist status message listeners
        controller.currentPlaylistProperty().addListener((obs, oldPlaylist, newPlaylist) -> {
            updatePlaylistStatusMessage();
        });
        
        controller.getPlaylists().addListener((javafx.collections.ListChangeListener<Playlist>) change -> {
            updatePlaylistStatusMessage();
        });
        
        // Initial update
        updatePlaylistStatusMessage();
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    /**
     * Configura los botones de shuffle y repeat
     */
    private void setupShuffleRepeatButtons(Button shuffleButton, Button repeatButton) {
        // Shuffle button
        shuffleButton.setOnAction(e -> {
            controller.toggleShuffle();
            updateShuffleButtonStyle(shuffleButton);
        });
        
        // Repeat button
        repeatButton.setOnAction(e -> {
            controller.toggleRepeatMode();
            updateRepeatButtonStyle(repeatButton);
        });
        
        // Bind button styles to controller properties
        controller.shuffleProperty().addListener((obs, oldVal, newVal) -> {
            updateShuffleButtonStyle(shuffleButton);
        });
        
        controller.repeatModeProperty().addListener((obs, oldMode, newMode) -> {
            updateRepeatButtonStyle(repeatButton);
        });
        
        // Initial style update
        updateShuffleButtonStyle(shuffleButton);
        updateRepeatButtonStyle(repeatButton);
    }
    
    private void updateShuffleButtonStyle(Button shuffleButton) {
        boolean isActive = controller.shuffleProperty().get();
        String baseStyle = "-fx-font-size: 14px; -fx-background-radius: 20; -fx-border-radius: 20;";
        
        if (isActive) {
            shuffleButton.setStyle(baseStyle + " -fx-background-color: #4CAF50; -fx-text-fill: white;");
        } else {
            shuffleButton.setStyle(baseStyle + " -fx-background-color: #f0f0f0; -fx-text-fill: #333;");
        }
    }
    
    private void updateRepeatButtonStyle(Button repeatButton) {
        RepeatMode mode = controller.repeatModeProperty().get();
        String baseStyle = "-fx-font-size: 14px; -fx-background-radius: 20; -fx-border-radius: 20;";
        
        switch (mode) {
            case OFF:
                repeatButton.setText("🔁");
                repeatButton.setStyle(baseStyle + " -fx-background-color: #f0f0f0; -fx-text-fill: #333;");
                break;
            case ALL:
                repeatButton.setText("🔁");
                repeatButton.setStyle(baseStyle + " -fx-background-color: #4CAF50; -fx-text-fill: white;");
                break;
            case ONE:
                repeatButton.setText("🔂");
                repeatButton.setStyle(baseStyle + " -fx-background-color: #FF9800; -fx-text-fill: white;");
                break;
        }
    }
    
    /**
     * Configura la funcionalidad de búsqueda
     */
    private void setupSearchField(TextField searchField) {
        // Create filtered list for search results
        FilteredList<Song> filteredSongs = 
            new FilteredList<>(controller.getLibrary());
        
        // Update list view with filtered results
        libraryListView.setItems(filteredSongs);
        
        // Add listener for real-time search
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                // Show all songs when search is empty
                filteredSongs.setPredicate(null);
            } else {
                // Filter songs based on search query
                String lowercaseQuery = newValue.toLowerCase().trim();
                filteredSongs.setPredicate(song -> 
                    (song.getTitle() != null && song.getTitle().toLowerCase().contains(lowercaseQuery)) ||
                    (song.getArtist() != null && song.getArtist().toLowerCase().contains(lowercaseQuery)) ||
                    (song.getAlbum() != null && song.getAlbum().toLowerCase().contains(lowercaseQuery)) ||
                    (song.getGenre() != null && song.getGenre().toLowerCase().contains(lowercaseQuery))
                );
            }
        });
        
        // Clear search on Escape key
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                searchField.clear();
            }
        });
    }
    
    /**
     * Actualiza la etiqueta de tiempo con el tiempo actual de reproducción
     */
    private void updateCurrentTimeLabel(double progress) {
        Song currentSong = controller.currentSongProperty().get();
        if (currentSong != null && currentSong.getDuration() != null) {
            // Calculate current time based on progress (0.0 to 1.0)
            long totalSeconds = currentSong.getDuration().getSeconds();
            long currentSeconds = Math.round(totalSeconds * progress);
            
            // Format time as MM:SS
            String timeText = formatTime(currentSeconds);
            totalTimeLabel.setText(timeText);
        } else {
            // No current song or duration, show 0:00
            totalTimeLabel.setText("0:00");
        }
    }
    
    /**
     * Formatea segundos como MM:SS
     */
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
    
    /**
     * Actualiza el mensaje de estado de playlist basado en el contexto actual
     */
    private void updatePlaylistStatusMessage() {
        Playlist currentPlaylist = controller.currentPlaylistProperty().get();
        ObservableList<Playlist> playlists = controller.getPlaylists();
        
        if (playlists.isEmpty()) {
            // No hay playlists creadas
            playlistStatusMessage.setText("📝 Aún no tienes listas de reproducción\n✨ Crea tu primera lista para organizar tus canciones favoritas");
            playlistStatusMessage.setStyle("-fx-text-fill: #666; -fx-text-alignment: center; -fx-font-size: 12px;");
        } else if (currentPlaylist != null) {
            // Se está reproduciendo desde una playlist
            playlistStatusMessage.setText("🎵 Reproduciendo desde: " + currentPlaylist.getName() + "\n▶️ " + currentPlaylist.getSongs().size() + " canciones en cola");
            playlistStatusMessage.setStyle("-fx-text-fill: #4CAF50; -fx-text-alignment: center; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            // Hay playlists pero no se está reproduciendo desde ninguna
            playlistStatusMessage.setText("📋 " + playlists.size() + " lista" + (playlists.size() != 1 ? "s" : "") + " de reproducción disponible" + (playlists.size() != 1 ? "s" : "") + "\n🎵 Selecciona una para reproducir");
            playlistStatusMessage.setStyle("-fx-text-fill: #666; -fx-text-alignment: center; -fx-font-size: 12px;");
        }
    }
    
    /**
     * Refresca la interfaz de usuario relacionada con playlists después de modificaciones
     */
    private void refreshPlaylistUI(Playlist modifiedPlaylist) {
        // Refresh the playlists list view to update song counts
        playlistsListView.refresh();
        
        // If the modified playlist is currently selected, refresh its content view
        Playlist currentlySelectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
        if (currentlySelectedPlaylist != null && currentlySelectedPlaylist.getId().equals(modifiedPlaylist.getId())) {
            // Update the playlist content view
            ObservableList<Song> updatedSongs = javafx.collections.FXCollections.observableArrayList(modifiedPlaylist.getSongs());
            playlistContentListView.setItems(updatedSongs);
            
            // Trigger the selection listener to update the info label and button states
            playlistsListView.getSelectionModel().clearSelection();
            playlistsListView.getSelectionModel().select(modifiedPlaylist);
        }
        
        // Update the playlist status message
        updatePlaylistStatusMessage();
    }
    
    /**
     * Configura los atajos de teclado
     */
    private void setupKeyboardShortcuts() {
        primaryStage.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DELETE:
                    // Delete selected songs from currently focused list
                    if (libraryListView.isFocused() && !libraryListView.getSelectionModel().getSelectedItems().isEmpty()) {
                        List<Song> selectedSongs = new java.util.ArrayList<>(
                            libraryListView.getSelectionModel().getSelectedItems());
                        
                        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmAlert.setTitle("Confirmar eliminación");
                        confirmAlert.setHeaderText(null);
                        confirmAlert.setContentText("¿Está seguro de que desea eliminar " + 
                            selectedSongs.size() + " canción" + (selectedSongs.size() != 1 ? "es" : "") + 
                            " de la biblioteca?");
                        
                        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                            controller.removeSongsFromLibrary(selectedSongs);
                            showSelectionActionComplete("Eliminadas " + selectedSongs.size() + 
                                " canción" + (selectedSongs.size() != 1 ? "es" : "") + " de la biblioteca");
                        }
                    } else if (queueListView.isFocused() && !queueListView.getSelectionModel().getSelectedItems().isEmpty()) {
                        List<Song> selectedSongs = new java.util.ArrayList<>(
                            queueListView.getSelectionModel().getSelectedItems());
                        
                        controller.removeSongsFromQueue(selectedSongs);
                        showSelectionActionComplete("Eliminadas " + selectedSongs.size() + 
                            " canción" + (selectedSongs.size() != 1 ? "es" : "") + " de la cola");
                    }
                    break;
                    
                case SPACE:
                    // Play/Pause with spacebar
                    if (controller.playbackStateProperty().get() == PlaybackState.PLAYING) {
                        controller.pause();
                    } else {
                        controller.play();
                    }
                    e.consume();
                    break;
                    
                case ESCAPE:
                    // Clear selections
                    libraryListView.getSelectionModel().clearSelection();
                    queueListView.getSelectionModel().clearSelection();
                    break;
                    
                case S:
                    // Toggle shuffle with Ctrl+S
                    if (e.isControlDown()) {
                        controller.toggleShuffle();
                        e.consume();
                    }
                    break;
                    
                case R:
                    // Toggle repeat with Ctrl+R
                    if (e.isControlDown()) {
                        controller.toggleRepeatMode();
                        e.consume();
                    }
                    break;
                    
                case LEFT:
                    // Previous song with left arrow
                    controller.previous();
                    e.consume();
                    break;
                    
                case RIGHT:
                    // Next song with right arrow
                    controller.next();
                    e.consume();
                    break;
                    
                default:
                    break;
            }
        });
    }
    
    public void show() {
        primaryStage.show();
    }
    
    /**
     * Configura el menú contextual para la cola de reproducción
     */
    private void setupQueueContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem playNowItem = new MenuItem("▶ Reproducir ahora");
        MenuItem addToPlaylistItem = new MenuItem("📋 Agregar a lista de reproducción...");
        MenuItem removeFromQueueItem = new MenuItem("➖ Eliminar de cola");
        MenuItem moveUpItem = new MenuItem("⬆ Mover arriba");
        MenuItem moveDownItem = new MenuItem("⬇ Mover abajo");
        
        playNowItem.setOnAction(e -> {
            Song selectedSong = queueListView.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                controller.playNow(selectedSong);
            }
        });
        
        addToPlaylistItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                queueListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                showAddToPlaylistDialog(selectedSongs);
            }
        });
        
        removeFromQueueItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                queueListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                controller.removeSongsFromQueue(selectedSongs);
                showSelectionActionComplete("Eliminadas " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : "") + " de la cola");
            }
        });
        
        moveUpItem.setOnAction(e -> {
            // TODO: Implementar mover canción hacia arriba en la cola
            showSelectionActionComplete("Función de mover hacia arriba próximamente");
        });
        
        moveDownItem.setOnAction(e -> {
            // TODO: Implementar mover canción hacia abajo en la cola
            showSelectionActionComplete("Función de mover hacia abajo próximamente");
        });
        
        contextMenu.getItems().addAll(playNowItem, addToPlaylistItem, new SeparatorMenuItem(), 
                                    removeFromQueueItem, new SeparatorMenuItem(),
                                    moveUpItem, moveDownItem);
        
        // Show context menu only when items are selected
        queueListView.setOnContextMenuRequested(e -> {
            if (!queueListView.getSelectionModel().getSelectedItems().isEmpty()) {
                contextMenu.show(queueListView, e.getScreenX(), e.getScreenY());
            }
        });
        
        // Hide context menu when clicking elsewhere
        queueListView.setOnMousePressed(e -> {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
        });
    }
    
    /**
     * Configura los event handlers para los controles de la cola
     */
    private void setupQueueControlsHandlers(Button clearQueueButton, Button shuffleQueueButton,
                                          Button removeFromQueueButton, Label queueSelectionLabel) {
        
        // Update queue selection count label
        queueListView.getSelectionModel().getSelectedItems().addListener(
            (javafx.collections.ListChangeListener<Song>) change -> {
                int count = queueListView.getSelectionModel().getSelectedItems().size();
                queueSelectionLabel.setText(count + " canción" + (count != 1 ? "es" : "") + " seleccionada" + (count != 1 ? "s" : ""));
                
                // Enable/disable remove button based on selection
                removeFromQueueButton.setDisable(count == 0);
            });
        
        // Clear queue button
        clearQueueButton.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar limpiar cola");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("¿Está seguro de que desea limpiar toda la cola de reproducción?");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                controller.clearQueue();
                showSelectionActionComplete("Cola de reproducción limpiada");
            }
        });
        
        // Shuffle queue button
        shuffleQueueButton.setOnAction(e -> {
            controller.shuffleQueue();
            showSelectionActionComplete("Cola de reproducción mezclada");
        });
        
        // Remove from queue button
        removeFromQueueButton.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                queueListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                controller.removeSongsFromQueue(selectedSongs);
                showSelectionActionComplete("Eliminadas " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : "") + " de la cola");
            }
        });
        
        // Initially disable remove button
        removeFromQueueButton.setDisable(true);
    }
    
    /**
     * Configura los event handlers para los controles del contenido de playlist
     */
    private void setupPlaylistContentHandlers(Button addToQueueAllButton,
                                            Button removeSelectedButton, Button moveUpButton, 
                                            Button moveDownButton, Button clearPlaylistButton,
                                            Label playlistSelectionLabel, Label selectedPlaylistInfo) {
        
        // Update playlist content selection count label
        playlistContentListView.getSelectionModel().getSelectedItems().addListener(
            (javafx.collections.ListChangeListener<Song>) change -> {
                int count = playlistContentListView.getSelectionModel().getSelectedItems().size();
                playlistSelectionLabel.setText(count + " canción" + (count != 1 ? "es" : "") + " seleccionada" + (count != 1 ? "s" : ""));
                
                // Enable/disable buttons based on selection
                removeSelectedButton.setDisable(count == 0);
                moveUpButton.setDisable(count == 0);
                moveDownButton.setDisable(count == 0);
            });
        
        // Update playlist content when a playlist is selected
        playlistsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldPlaylist, newPlaylist) -> {
            if (newPlaylist != null) {
                selectedPlaylistInfo.setText("Lista: " + newPlaylist.getName() + " (" + 
                    newPlaylist.getSongs().size() + " canción" + (newPlaylist.getSongs().size() != 1 ? "es" : "") + ")");
                ObservableList<Song> playlistSongs = javafx.collections.FXCollections.observableArrayList(newPlaylist.getSongs());
                playlistContentListView.setItems(playlistSongs);
                
                // Enable/disable buttons based on playlist content
                boolean hasContent = !newPlaylist.getSongs().isEmpty();
                addToQueueAllButton.setDisable(!hasContent);
                clearPlaylistButton.setDisable(!hasContent);
                
                // Enable edit and delete buttons when a playlist is selected
                editPlaylistButton.setDisable(false);
                deletePlaylistButton.setDisable(false);
            } else {
                selectedPlaylistInfo.setText("Selecciona una lista para ver su contenido");
                playlistContentListView.getItems().clear();
                addToQueueAllButton.setDisable(true);
                clearPlaylistButton.setDisable(true);
                
                // Disable edit and delete buttons when no playlist is selected
                editPlaylistButton.setDisable(true);
                deletePlaylistButton.setDisable(true);
            }
        });
        
        // Add all to queue button
        addToQueueAllButton.setOnAction(e -> {
            Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
            if (selectedPlaylist != null && !selectedPlaylist.getSongs().isEmpty()) {
                List<Song> allSongs = new java.util.ArrayList<>(selectedPlaylist.getSongs());
                controller.addSongsToQueue(allSongs);
                showSelectionActionComplete("Agregadas " + allSongs.size() + 
                    " canciones de \"" + selectedPlaylist.getName() + "\" a la cola");
            }
        });
        
        // Remove selected songs from playlist
        removeSelectedButton.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                playlistContentListView.getSelectionModel().getSelectedItems());
            Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
            
            if (!selectedSongs.isEmpty() && selectedPlaylist != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmar eliminación");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("¿Está seguro de que desea eliminar " + 
                    selectedSongs.size() + " canción" + (selectedSongs.size() != 1 ? "es" : "") + 
                    " de la lista \"" + selectedPlaylist.getName() + "\"?");
                
                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    controller.removeSongsFromPlaylist(selectedSongs, selectedPlaylist);
                    
                    // Refresh UI to show changes immediately
                    refreshPlaylistUI(selectedPlaylist);
                    
                    showSelectionActionComplete("Eliminadas " + selectedSongs.size() + 
                        " canción" + (selectedSongs.size() != 1 ? "es" : "") + " de la lista");
                }
            }
        });
        
        // Clear playlist button
        clearPlaylistButton.setOnAction(e -> {
            Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
            if (selectedPlaylist != null && !selectedPlaylist.getSongs().isEmpty()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmar vaciar lista");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("¿Está seguro de que desea vaciar completamente la lista \"" + 
                    selectedPlaylist.getName() + "\"? Esta acción eliminará todas las canciones de la lista.");
                
                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    controller.clearPlaylist(selectedPlaylist);
                    
                    // Refresh UI to show changes immediately
                    refreshPlaylistUI(selectedPlaylist);
                    
                    showSelectionActionComplete("Lista \"" + selectedPlaylist.getName() + "\" vaciada");
                }
            }
        });
        
        // Move up button
        moveUpButton.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                playlistContentListView.getSelectionModel().getSelectedItems());
            Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
            
            if (!selectedSongs.isEmpty() && selectedPlaylist != null) {
                controller.moveSongsUp(selectedSongs, selectedPlaylist);
                
                // Refresh the playlist content view
                ObservableList<Song> updatedSongs = javafx.collections.FXCollections.observableArrayList(selectedPlaylist.getSongs());
                playlistContentListView.setItems(updatedSongs);
                
                // Restore selection
                for (Song song : selectedSongs) {
                    playlistContentListView.getSelectionModel().select(song);
                }
                
                showSelectionActionComplete("Movidas " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : "") + " hacia arriba");
            }
        });
        
        // Move down button
        moveDownButton.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                playlistContentListView.getSelectionModel().getSelectedItems());
            Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
            
            if (!selectedSongs.isEmpty() && selectedPlaylist != null) {
                controller.moveSongsDown(selectedSongs, selectedPlaylist);
                
                // Refresh the playlist content view
                ObservableList<Song> updatedSongs = javafx.collections.FXCollections.observableArrayList(selectedPlaylist.getSongs());
                playlistContentListView.setItems(updatedSongs);
                
                // Restore selection
                for (Song song : selectedSongs) {
                    playlistContentListView.getSelectionModel().select(song);
                }
                
                showSelectionActionComplete("Movidas " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : "") + " hacia abajo");
            }
        });
        
        // Initially disable action buttons
        addToQueueAllButton.setDisable(true);
        removeSelectedButton.setDisable(true);
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);
        clearPlaylistButton.setDisable(true);
    }
    
    /**
     * Configura el menú contextual para el contenido de playlists
     */
    private void setupPlaylistContentContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem playNowItem = new MenuItem("▶ Reproducir ahora");
        MenuItem addToQueueItem = new MenuItem("➕ Agregar a cola");
        MenuItem addToOtherPlaylistItem = new MenuItem("📋 Copiar a otra lista...");
        MenuItem moveUpItem = new MenuItem("⬆ Mover arriba");
        MenuItem moveDownItem = new MenuItem("⬇ Mover abajo");
        MenuItem removeFromPlaylistItem = new MenuItem("➖ Eliminar de esta lista");
        
        playNowItem.setOnAction(e -> {
            Song selectedSong = playlistContentListView.getSelectionModel().getSelectedItem();
            Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
            if (selectedSong != null && selectedPlaylist != null) {
                // Play the playlist starting from the selected song
                controller.playPlaylistFromSong(selectedSong, selectedPlaylist);
                showSelectionActionComplete("Reproduciendo desde \"" + selectedSong.getTitle() + 
                    "\" en la lista: " + selectedPlaylist.getName());
            }
        });
        
        addToQueueItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                playlistContentListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                controller.addSongsToQueue(selectedSongs);
                showSelectionActionComplete("Agregadas " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : "") + " a la cola");
            }
        });
        
        addToOtherPlaylistItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                playlistContentListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                showAddToPlaylistDialog(selectedSongs);
            }
        });
        
        moveUpItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                playlistContentListView.getSelectionModel().getSelectedItems());
            Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
            
            if (!selectedSongs.isEmpty() && selectedPlaylist != null) {
                controller.moveSongsUp(selectedSongs, selectedPlaylist);
                // Refresh playlist content
                playlistContentListView.setItems(javafx.collections.FXCollections.observableList(selectedPlaylist.getSongs()));
                // Restore selection for the moved songs
                Platform.runLater(() -> {
                    for (Song song : selectedSongs) {
                        int index = selectedPlaylist.getSongs().indexOf(song);
                        if (index >= 0) {
                            playlistContentListView.getSelectionModel().select(index);
                        }
                    }
                });
                showSelectionActionComplete("Movidas " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : "") + " hacia arriba");
            }
        });
        
        moveDownItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                playlistContentListView.getSelectionModel().getSelectedItems());
            Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
            
            if (!selectedSongs.isEmpty() && selectedPlaylist != null) {
                controller.moveSongsDown(selectedSongs, selectedPlaylist);
                // Refresh playlist content
                playlistContentListView.setItems(javafx.collections.FXCollections.observableList(selectedPlaylist.getSongs()));
                // Restore selection for the moved songs
                Platform.runLater(() -> {
                    for (Song song : selectedSongs) {
                        int index = selectedPlaylist.getSongs().indexOf(song);
                        if (index >= 0) {
                            playlistContentListView.getSelectionModel().select(index);
                        }
                    }
                });
                showSelectionActionComplete("Movidas " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : "") + " hacia abajo");
            }
        });
        
        removeFromPlaylistItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                playlistContentListView.getSelectionModel().getSelectedItems());
            Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
            
            if (!selectedSongs.isEmpty() && selectedPlaylist != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmar eliminación");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("¿Está seguro de que desea eliminar " + 
                    selectedSongs.size() + " canción" + (selectedSongs.size() != 1 ? "es" : "") + 
                    " de la lista \"" + selectedPlaylist.getName() + "\"?");
                
                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    controller.removeSongsFromPlaylist(selectedSongs, selectedPlaylist);
                    
                    // Refresh UI to show changes immediately
                    refreshPlaylistUI(selectedPlaylist);
                    
                    showSelectionActionComplete("Eliminadas " + selectedSongs.size() + 
                        " canción" + (selectedSongs.size() != 1 ? "es" : "") + " de la lista");
                }
            }
        });
        
        contextMenu.getItems().addAll(playNowItem, addToQueueItem, addToOtherPlaylistItem,
                                    new SeparatorMenuItem(), moveUpItem, moveDownItem,
                                    new SeparatorMenuItem(), removeFromPlaylistItem);
        
        // Show context menu only when items are selected
        playlistContentListView.setOnContextMenuRequested(e -> {
            if (!playlistContentListView.getSelectionModel().getSelectedItems().isEmpty()) {
                contextMenu.show(playlistContentListView, e.getScreenX(), e.getScreenY());
            }
        });
        
        // Hide context menu when clicking elsewhere
        playlistContentListView.setOnMousePressed(e -> {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
        });
    }
    
    /**
     * Configura el menú contextual para la biblioteca
     */
    private void setupLibraryContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem playNowItem = new MenuItem("▶ Reproducir ahora");
        MenuItem addToQueueItem = new MenuItem("➕ Agregar a cola");
        MenuItem addToPlaylistItem = new MenuItem("📋 Agregar a lista de reproducción...");
        MenuItem removeFromLibraryItem = new MenuItem("🗑️ Eliminar de biblioteca");
        
        playNowItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                libraryListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                controller.playSelectedSongs(selectedSongs);
            }
        });
        
        addToQueueItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                libraryListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                controller.addSongsToQueue(selectedSongs);
                showSelectionActionComplete("Agregadas " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : "") + " a la cola");
            }
        });
        
        addToPlaylistItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                libraryListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                showAddToPlaylistDialog(selectedSongs);
            }
        });
        
        removeFromLibraryItem.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                libraryListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmar eliminación");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("¿Está seguro de que desea eliminar " + 
                    selectedSongs.size() + " canción" + (selectedSongs.size() != 1 ? "es" : "") + 
                    " de la biblioteca?");
                
                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    for (Song song : selectedSongs) {
                        controller.removeSongFromLibrary(song);
                    }
                    showSelectionActionComplete("Eliminadas " + selectedSongs.size() + 
                        " canción" + (selectedSongs.size() != 1 ? "es" : "") + " de la biblioteca");
                }
            }
        });
        
        contextMenu.getItems().addAll(playNowItem, addToQueueItem, addToPlaylistItem,
                                    new SeparatorMenuItem(), removeFromLibraryItem);
        
        // Show context menu only when items are selected
        libraryListView.setOnContextMenuRequested(e -> {
            if (!libraryListView.getSelectionModel().getSelectedItems().isEmpty()) {
                contextMenu.show(libraryListView, e.getScreenX(), e.getScreenY());
            }
        });
        
        // Hide context menu when clicking elsewhere
        libraryListView.setOnMousePressed(e -> {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
        });
    }
    
    /**
     * Configura los event handlers para la selección múltiple en la biblioteca
     */
    private void setupLibrarySelectionHandlers(Button addToQueueButton, Button playSelectedButton,
                                             Button selectAllButton, Button clearSelectionButton,
                                             Label selectionCountLabel) {
        
        // Update selection count label when selection changes
        libraryListView.getSelectionModel().getSelectedItems().addListener(
            (javafx.collections.ListChangeListener<Song>) change -> {
                int count = libraryListView.getSelectionModel().getSelectedItems().size();
                selectionCountLabel.setText(count + " canción" + (count != 1 ? "es" : "") + " seleccionada" + (count != 1 ? "s" : ""));
                
                // Enable/disable buttons based on selection
                boolean hasSelection = count > 0;
                addToQueueButton.setDisable(!hasSelection);
                playSelectedButton.setDisable(!hasSelection);
                clearSelectionButton.setDisable(!hasSelection);
            });
        
        // Add to queue button
        addToQueueButton.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                libraryListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                controller.addSongsToQueue(selectedSongs);
                showSelectionActionComplete("Agregadas " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : "") + " a la cola");
            }
        });
        
        // Play selected button
        playSelectedButton.setOnAction(e -> {
            List<Song> selectedSongs = new java.util.ArrayList<>(
                libraryListView.getSelectionModel().getSelectedItems());
            if (!selectedSongs.isEmpty()) {
                controller.playSelectedSongs(selectedSongs);
                showSelectionActionComplete("Reproduciendo " + selectedSongs.size() + 
                    " canción" + (selectedSongs.size() != 1 ? "es" : ""));
            }
        });
        
        // Select all button
        selectAllButton.setOnAction(e -> {
            libraryListView.getSelectionModel().selectAll();
        });
        
        // Clear selection button
        clearSelectionButton.setOnAction(e -> {
            libraryListView.getSelectionModel().clearSelection();
        });
        
        // Initially disable action buttons
        addToQueueButton.setDisable(true);
        playSelectedButton.setDisable(true);
        clearSelectionButton.setDisable(true);
    }
    
    /**
     * Muestra un mensaje temporal de acción completada
     */
    private void showSelectionActionComplete(String message) {
        // Show message in the artist label with green color
        artistLabel.setText("✓ " + message);
        artistLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        
        // Reset after 4 seconds to give more time to see the message
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(4), ev -> {
                Song currentSong = controller.currentSongProperty().get();
                if (currentSong != null) {
                    artistLabel.setText(currentSong.getArtist());
                } else {
                    artistLabel.setText("Usa la biblioteca para elegir tu música favorita");
                }
                artistLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
            })
        );
        timeline.play();
    }
    
    /**
     * Configura los event handlers para los controles de lyrics
     */
    private void setupLyricsControlsHandlers(Button searchLyricsButton, Button editLyricsButton, Button clearLyricsButton) {
        // Search lyrics button - placeholder for future implementation
        searchLyricsButton.setOnAction(e -> {
            Song currentSong = controller.currentSongProperty().get();
            if (currentSong != null) {
                showSelectionActionComplete("Función de búsqueda de letras próximamente");
                // TODO: Implement lyrics search functionality
            } else {
                showSelectionActionComplete("Selecciona una canción primero");
            }
        });
        
        // Edit lyrics button - opens edit window
        editLyricsButton.setOnAction(e -> {
            Song currentSong = controller.currentSongProperty().get();
            if (currentSong != null) {
                openLyricsEditWindow(currentSong);
            } else {
                showSelectionActionComplete("Selecciona una canción primero");
            }
        });
        
        // Clear lyrics button
        clearLyricsButton.setOnAction(e -> {
            lyricsTextArea.clear();
            showSelectionActionComplete("Letras limpiadas");
        });
    }
    
    /**
     * Abre una ventana emergente para editar las letras de una canción
     */
    private void openLyricsEditWindow(Song song) {
        // Crear nueva ventana
        Stage editWindow = new Stage();
        editWindow.setTitle("Editar Letras - " + song.getTitle());
        editWindow.initModality(Modality.APPLICATION_MODAL);
        editWindow.initOwner(primaryStage);
        
        // Layout principal
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        
        // Header con información de la canción
        VBox header = new VBox(5);
        Label songTitle = new Label("🎵 " + song.getTitle());
        songTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label artistInfo = new Label("👤 " + song.getArtist());
        artistInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        if (song.getAlbum() != null && !song.getAlbum().trim().isEmpty()) {
            Label albumInfo = new Label("💿 " + song.getAlbum());
            albumInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
            header.getChildren().addAll(songTitle, artistInfo, albumInfo);
        } else {
            header.getChildren().addAll(songTitle, artistInfo);
        }
        
        Separator separator = new Separator();
        
        // Área de texto para editar letras
        Label instructionLabel = new Label("✏️ Edita las letras de la canción:");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextArea editTextArea = new TextArea();
        editTextArea.setPromptText("Escribe o pega aquí las letras de la canción...");
        editTextArea.setWrapText(true);
        editTextArea.setPrefHeight(400);
        editTextArea.setPrefWidth(600);
        editTextArea.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial'; -fx-control-inner-background: #ffffff;");
        
        // Cargar las letras actuales si existen
        String currentLyrics = getLyricsForSong(song);
        if (currentLyrics != null && !currentLyrics.trim().isEmpty()) {
            // Limpiar el texto de placeholder si existe
            if (!currentLyrics.contains("No hay letras disponibles")) {
                editTextArea.setText(currentLyrics);
            }
        }
        
        // Botones de acción
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelButton = new Button("❌ Cancelar");
        cancelButton.setPrefWidth(120);
        cancelButton.setPrefHeight(35);
        cancelButton.setStyle("-fx-font-size: 12px; -fx-background-color: #f44336; -fx-text-fill: white;");
        
        Button saveButton = new Button("💾 Guardar Cambios");
        saveButton.setPrefWidth(140);
        saveButton.setPrefHeight(35);
        saveButton.setStyle("-fx-font-size: 12px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        // Event handlers para los botones
        cancelButton.setOnAction(e -> {
            editWindow.close();
        });
        
        saveButton.setOnAction(e -> {
            String newLyrics = editTextArea.getText().trim();
            
            // Guardar las letras
            saveLyricsForSong(song, newLyrics);
            
            // Actualizar la vista principal de letras
            updateLyricsForCurrentSong(song);
            
            // Mostrar confirmación
            showSelectionActionComplete("Letras guardadas para: " + song.getTitle());
            
            // Cerrar ventana
            editWindow.close();
        });
        
        // Agregar todos los componentes al layout
        VBox.setVgrow(editTextArea, Priority.ALWAYS);
        mainLayout.getChildren().addAll(header, separator, instructionLabel, editTextArea, buttonBox);
        
        // Crear escena y mostrar ventana
        Scene scene = new Scene(mainLayout, 650, 550);
        editWindow.setScene(scene);
        editWindow.setResizable(true);
        editWindow.setMinWidth(500);
        editWindow.setMinHeight(400);
        
        // Centrar la ventana
        editWindow.centerOnScreen();
        
        // Focus en el área de texto
        Platform.runLater(() -> editTextArea.requestFocus());
        
        // Mostrar ventana
        editWindow.showAndWait();
    }
    
    /**
     * Actualiza las letras para la canción actual
     */
    private void updateLyricsForCurrentSong(Song song) {
        if (song != null) {
            currentSongLyricsLabel.setText("🎵 " + song.getTitle() + " - " + song.getArtist());
            
            // TODO: Load lyrics from database or file
            // For now, show placeholder text
            String lyrics = getLyricsForSong(song);
            if (lyrics != null && !lyrics.trim().isEmpty()) {
                lyricsTextArea.setText(lyrics);
            } else {
                lyricsTextArea.setText("No hay letras disponibles para esta canción.\n\n" +
                                     "Puedes:\n" +
                                     "• Usar el botón '🔍 Buscar Letras' para buscarlas automáticamente\n" +
                                     "• Usar el botón '✏️ Editar Letras' para agregar las letras manualmente");
            }
        } else {
            currentSongLyricsLabel.setText("Selecciona una canción para ver sus letras");
            lyricsTextArea.clear();
        }
    }
    
    /**
     * Obtiene las letras para una canción desde la base de datos
     */
    private String getLyricsForSong(Song song) {
        if (song != null && song.getId() != null) {
            // Primero intentar obtener desde el objeto Song en memoria
            if (song.getLyrics() != null && !song.getLyrics().trim().isEmpty()) {
                return song.getLyrics();
            }
            
            // Si no está en memoria, intentar cargar desde la base de datos
            try {
                return controller.getDatabaseManager().getSongLyrics(song.getId());
            } catch (Exception e) {
                System.err.println("Error al cargar letras desde la base de datos: " + e.getMessage());
                return null;
            }
        }
        return null;
    }
    
    /**
     * Guarda las letras para una canción específica en la base de datos
     */
    private void saveLyricsForSong(Song song, String lyrics) {
        if (song != null && song.getId() != null) {
            try {
                // Actualizar la canción en memoria
                song.setLyrics(lyrics);
                
                // Guardar en la base de datos
                controller.getDatabaseManager().updateSongLyrics(song.getId(), lyrics);
                
                System.out.println("Letras guardadas exitosamente para: " + song.getTitle() + " - " + song.getArtist());
            } catch (Exception e) {
                System.err.println("Error al guardar letras en la base de datos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Guarda las letras para la canción actual
     */
    private void saveLyricsForCurrentSong() {
        Song currentSong = controller.currentSongProperty().get();
        if (currentSong != null && lyricsTextArea.getText() != null) {
            String lyrics = lyricsTextArea.getText().trim();
            // TODO: Save lyrics to database
            // For now, just show confirmation message
            System.out.println("Saving lyrics for: " + currentSong.getTitle() + " - " + lyrics.length() + " characters");
            showSelectionActionComplete("Letras guardadas para \"" + currentSong.getTitle() + "\"");
        }
    }
    
    /**
     * Configura los event handlers para el menú de archivo
     */
    private void setupFileMenuHandlers() {
        // Import files handler
        importFiles.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivos de música");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de Audio", 
                    "*.mp3", "*.wav", "*.flac", "*.ogg", "*.m4a", "*.aac", "*.wma"),
                new FileChooser.ExtensionFilter("MP3", "*.mp3"),
                new FileChooser.ExtensionFilter("WAV", "*.wav"),
                new FileChooser.ExtensionFilter("FLAC", "*.flac"),
                new FileChooser.ExtensionFilter("OGG", "*.ogg"),
                new FileChooser.ExtensionFilter("M4A", "*.m4a"),
                new FileChooser.ExtensionFilter("AAC", "*.aac"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );
            
            // Set initial directory to Music folder if available
            String userHome = System.getProperty("user.home");
            File musicDir = new File(userHome, "Music");
            if (musicDir.exists()) {
                fileChooser.setInitialDirectory(musicDir);
            }
            
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                // Show progress dialog or status
                showImportProgress(selectedFiles.size());
                
                // Import files in background thread
                Thread importThread = new Thread(() -> {
                    try {
                        controller.importMusicFiles(selectedFiles);
                        // Update UI on JavaFX thread
                        javafx.application.Platform.runLater(() -> {
                            hideImportProgress();
                            showImportComplete(selectedFiles.size());
                        });
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> {
                            hideImportProgress();
                            showImportError(ex.getMessage());
                        });
                    }
                });
                importThread.setDaemon(true);
                importThread.start();
            }
        });
        
        // Import folder handler
        importFolder.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Seleccionar carpeta de música");
            
            // Set initial directory to Music folder if available
            String userHome = System.getProperty("user.home");
            File musicDir = new File(userHome, "Music");
            if (musicDir.exists()) {
                directoryChooser.setInitialDirectory(musicDir);
            }
            
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                // Scan directory for audio files
                List<File> audioFiles = scanDirectoryForAudioFiles(selectedDirectory);
                
                if (!audioFiles.isEmpty()) {
                    showImportProgress(audioFiles.size());
                    
                    // Import files in background thread
                    Thread importThread = new Thread(() -> {
                        try {
                            controller.importMusicFiles(audioFiles);
                            javafx.application.Platform.runLater(() -> {
                                hideImportProgress();
                                showImportComplete(audioFiles.size());
                            });
                        } catch (Exception ex) {
                            javafx.application.Platform.runLater(() -> {
                                hideImportProgress();
                                showImportError(ex.getMessage());
                            });
                        }
                    });
                    importThread.setDaemon(true);
                    importThread.start();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sin archivos de audio");
                    alert.setHeaderText(null);
                    alert.setContentText("No se encontraron archivos de audio en la carpeta seleccionada.");
                    alert.showAndWait();
                }
            }
        });
    }
    
    /**
     * Escanea un directorio en busca de archivos de audio
     */
    private List<File> scanDirectoryForAudioFiles(File directory) {
        List<File> audioFiles = new java.util.ArrayList<>();
        scanDirectoryRecursive(directory, audioFiles);
        return audioFiles;
    }
    
    /**
     * Escanea recursivamente un directorio
     */
    private void scanDirectoryRecursive(File directory, List<File> audioFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectoryRecursive(file, audioFiles);
                } else if (isAudioFile(file)) {
                    audioFiles.add(file);
                }
            }
        }
    }
    
    /**
     * Verifica si un archivo es un archivo de audio
     */
    private boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav") || 
               name.endsWith(".flac") || name.endsWith(".ogg") || 
               name.endsWith(".m4a") || name.endsWith(".aac") || 
               name.endsWith(".wma");
    }
    
    /**
     * Muestra progreso de importación
     */
    private void showImportProgress(int fileCount) {
        // TODO: Implementar dialog de progreso
        System.out.println("Importando " + fileCount + " archivos...");
    }
    
    /**
     * Oculta progreso de importación
     */
    private void hideImportProgress() {
        // TODO: Ocultar dialog de progreso
        System.out.println("Importación completada");
    }
    
    /**
     * Muestra mensaje de importación completada
     */
    private void showImportComplete(int fileCount) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Importación completada");
        alert.setHeaderText(null);
        alert.setContentText("Se importaron " + fileCount + " archivos exitosamente.");
        alert.showAndWait();
    }
    
    /**
     * Muestra mensaje de error de importación
     */
    private void showImportError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de importación");
        alert.setHeaderText(null);
        alert.setContentText("Error al importar archivos: " + errorMessage);
        alert.showAndWait();
    }
    
    /**
     * Muestra el diálogo para crear una nueva playlist
     */
    private void showCreatePlaylistDialog() {
        // Create custom dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Nueva Lista de Reproducción");
        dialog.setHeaderText("Crear una nueva lista de reproducción");
        
        // Set the button types
        ButtonType createButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Nombre de la playlist");
        nameField.setPrefWidth(300);
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Descripción (opcional)");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setWrapText(true);
        
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        
        // Enable/disable create button depending on whether a name was entered
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);
        
        // Do some validation (using the Java 8 lambda syntax)
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty());
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        javafx.application.Platform.runLater(() -> nameField.requestFocus());
        
        // Convert the result to a playlist name when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return nameField.getText().trim();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(playlistName -> {
            // Create the playlist
            String description = descriptionArea.getText().trim();
            if (description.isEmpty()) {
                description = null;
            }
            
            try {
                controller.createPlaylist(playlistName, description);
                showSelectionActionComplete("Lista de reproducción \"" + playlistName + "\" creada exitosamente");
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error al crear playlist");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("No se pudo crear la lista de reproducción: " + e.getMessage());
                errorAlert.showAndWait();
            }
        });
    }
    
    /**
     * Muestra el diálogo para editar una playlist existente
     */
    private void showEditPlaylistDialog() {
        Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
        if (selectedPlaylist == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ninguna lista seleccionada");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecciona una lista de reproducción para editar.");
            alert.showAndWait();
            return;
        }
        
        // Create custom dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Editar Lista de Reproducción");
        dialog.setHeaderText("Editar \"" + selectedPlaylist.getName() + "\"");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setText(selectedPlaylist.getName());
        nameField.setPromptText("Nombre de la playlist");
        nameField.setPrefWidth(300);
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setText(selectedPlaylist.getDescription() != null ? selectedPlaylist.getDescription() : "");
        descriptionArea.setPromptText("Descripción (opcional)");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setWrapText(true);
        
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        
        // Enable/disable save button depending on whether a name was entered
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false);
        
        // Do some validation (using the Java 8 lambda syntax)
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        javafx.application.Platform.runLater(() -> nameField.requestFocus());
        
        // Convert the result to a playlist name when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return nameField.getText().trim();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(newPlaylistName -> {
            // Update the playlist
            String newDescription = descriptionArea.getText().trim();
            if (newDescription.isEmpty()) {
                newDescription = null;
            }
            
            try {
                // Update playlist properties
                selectedPlaylist.setName(newPlaylistName);
                selectedPlaylist.setDescription(newDescription);
                selectedPlaylist.setModifiedDate(System.currentTimeMillis());
                
                // Update in database through controller
                controller.updatePlaylist(selectedPlaylist);
                
                // Refresh the playlists list view
                playlistsListView.refresh();
                
                showSelectionActionComplete("Lista de reproducción \"" + newPlaylistName + "\" actualizada exitosamente");
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error al editar playlist");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("No se pudo actualizar la lista de reproducción: " + e.getMessage());
                errorAlert.showAndWait();
            }
        });
    }
    
    /**
     * Muestra el diálogo para eliminar una playlist
     */
    private void showDeletePlaylistDialog() {
        Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
        if (selectedPlaylist == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ninguna lista seleccionada");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecciona una lista de reproducción para eliminar.");
            alert.showAndWait();
            return;
        }
        
        // Create confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar eliminación");
        confirmAlert.setHeaderText("Eliminar lista de reproducción");
        confirmAlert.setContentText("¿Está seguro de que desea eliminar la lista \"" + selectedPlaylist.getName() + "\"?\n\n" +
                                  "Esta acción no se puede deshacer. La lista contiene " + 
                                  selectedPlaylist.getSongs().size() + " canción" + 
                                  (selectedPlaylist.getSongs().size() != 1 ? "es" : "") + ".");
        
        // Add custom buttons
        ButtonType deleteButtonType = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);
        
        // Style the delete button to be red
        confirmAlert.getDialogPane().lookupButton(deleteButtonType).setStyle(
            "-fx-background-color: #f44336; -fx-text-fill: white;"
        );
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == deleteButtonType) {
            try {
                String playlistName = selectedPlaylist.getName();
                
                // Delete from controller (which will handle database deletion)
                controller.deletePlaylist(selectedPlaylist);
                
                // Clear playlist content view if this playlist was selected
                playlistContentListView.getItems().clear();
                
                showSelectionActionComplete("Lista de reproducción \"" + playlistName + "\" eliminada exitosamente");
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error al eliminar playlist");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("No se pudo eliminar la lista de reproducción: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
    
    /**
     * Muestra el diálogo para agregar canciones a una playlist existente
     */
    private void showAddToPlaylistDialog(List<Song> songs) {
        if (songs == null || songs.isEmpty()) {
            return;
        }
        
        ObservableList<Playlist> availablePlaylists = controller.getPlaylists();
        if (availablePlaylists.isEmpty()) {
            Alert noPlaylistsAlert = new Alert(Alert.AlertType.INFORMATION);
            noPlaylistsAlert.setTitle("Sin listas de reproducción");
            noPlaylistsAlert.setHeaderText(null);
            noPlaylistsAlert.setContentText("No hay listas de reproducción disponibles. Cree una nueva lista primero.");
            
            ButtonType createNewButton = new ButtonType("Crear Nueva Lista");
            ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            noPlaylistsAlert.getButtonTypes().setAll(createNewButton, cancelButton);
            
            Optional<ButtonType> result = noPlaylistsAlert.showAndWait();
            if (result.isPresent() && result.get() == createNewButton) {
                showCreatePlaylistDialog();
            }
            return;
        }
        
        // Create selection dialog
        Dialog<Playlist> dialog = new Dialog<>();
        dialog.setTitle("Agregar a Lista de Reproducción");
        dialog.setHeaderText("Seleccione la lista de reproducción donde agregar " + 
            songs.size() + " canción" + (songs.size() != 1 ? "es" : ""));
        
        // Set the button types
        ButtonType addButtonType = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create the playlist selection list
        ListView<Playlist> playlistListView = new ListView<>();
        playlistListView.setItems(availablePlaylists);
        playlistListView.setPrefHeight(200);
        playlistListView.setPrefWidth(400);
        
        // Custom cell factory to show playlist name and description
        playlistListView.setCellFactory(listView -> new ListCell<Playlist>() {
            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);
                if (empty || playlist == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(2);
                    Label nameLabel = new Label(playlist.getName());
                    nameLabel.setStyle("-fx-font-weight: bold;");
                    vbox.getChildren().add(nameLabel);
                    
                    if (playlist.getDescription() != null && !playlist.getDescription().trim().isEmpty()) {
                        Label descLabel = new Label(playlist.getDescription());
                        descLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 0.9em;");
                        vbox.getChildren().add(descLabel);
                    }
                    
                    Label songsLabel = new Label("(" + playlist.getSongs().size() + " canción" + 
                        (playlist.getSongs().size() != 1 ? "es" : "") + ")");
                    songsLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 0.8em;");
                    vbox.getChildren().add(songsLabel);
                    
                    setGraphic(vbox);
                }
            }
        });
        
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.getChildren().addAll(
            new Label("Listas de reproducción disponibles:"),
            playlistListView
        );
        
        // Enable/disable add button depending on selection
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        playlistListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            addButton.setDisable(newSelection == null);
        });
        
        dialog.getDialogPane().setContent(container);
        
        // Convert the result to the selected playlist
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return playlistListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        Optional<Playlist> result = dialog.showAndWait();
        
        result.ifPresent(selectedPlaylist -> {
            try {
                controller.addSongsToPlaylist(songs, selectedPlaylist);
                
                // Refresh UI to show changes immediately
                refreshPlaylistUI(selectedPlaylist);
                
                showSelectionActionComplete("Agregadas " + songs.size() + 
                    " canción" + (songs.size() != 1 ? "es" : "") + 
                    " a la lista \"" + selectedPlaylist.getName() + "\"");
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error al agregar canciones");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("No se pudieron agregar las canciones a la lista: " + e.getMessage());
                errorAlert.showAndWait();
            }
        });
    }
    
    /**
     * Custom list cell for displaying songs
     */
    private class SongListCell extends ListCell<Song> {
        private HBox cellContainer;
        private VBox songInfo;
        private Button deleteButton;
        
        public SongListCell() {
            super();
            createCellContent();
        }
        
        private void createCellContent() {
            cellContainer = new HBox(10);
            cellContainer.setAlignment(Pos.CENTER_LEFT);
            
            songInfo = new VBox(2);
            HBox.setHgrow(songInfo, Priority.ALWAYS);
            
            deleteButton = new Button("✖");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                                "-fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 12;");
            deleteButton.setPrefSize(24, 24);
            deleteButton.setVisible(false);
            
            cellContainer.getChildren().addAll(songInfo, deleteButton);
        }
        
        @Override
        protected void updateItem(Song song, boolean empty) {
            super.updateItem(song, empty);
            
            if (empty || song == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                // Create formatted text for song display
                songInfo.getChildren().clear();
                
                Label titleLabel = new Label(song.getTitle());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                
                String artistAlbum = song.getArtist();
                if (song.getAlbum() != null && !song.getAlbum().equals("Álbum Desconocido")) {
                    artistAlbum += " • " + song.getAlbum();
                }
                Label artistLabel = new Label(artistAlbum);
                artistLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                
                // Duration info
                String durationText = "";
                if (song.getDuration() != null && song.getDuration().getSeconds() > 0) {
                    long minutes = song.getDuration().toMinutes();
                    long seconds = song.getDuration().getSeconds() % 60;
                    durationText = String.format("%d:%02d", minutes, seconds);
                }
                
                HBox topRow = new HBox();
                topRow.getChildren().add(titleLabel);
                if (!durationText.isEmpty()) {
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    Label durationLabel = new Label(durationText);
                    durationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
                    topRow.getChildren().addAll(spacer, durationLabel);
                }
                
                songInfo.getChildren().addAll(topRow, artistLabel);
                
                // Setup delete button action
                deleteButton.setOnAction(e -> {
                    e.consume(); // Prevent selection change
                    showDeleteConfirmation(song);
                });
                
                setGraphic(cellContainer);
                setText(null);
                
                // Show delete button on hover
                setOnMouseEntered(e -> deleteButton.setVisible(true));
                setOnMouseExited(e -> deleteButton.setVisible(false));
                
                // Highlight selected items
                if (isSelected()) {
                    setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; -fx-border-width: 1px;");
                    deleteButton.setVisible(true); // Always show for selected items
                } else {
                    setStyle("");
                }
            }
        }
        
        private void showDeleteConfirmation(Song song) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar eliminación");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("¿Está seguro de que desea eliminar \"" + song.getTitle() + 
                                      "\" de la biblioteca?\n\nEsta acción no se puede deshacer.");
            
            Button deleteButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
            deleteButton.setText("Eliminar");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                // Determine if this is from library or queue
                if (getListView() == libraryListView) {
                    controller.removeSongFromLibrary(song);
                    showSelectionActionComplete("Eliminada \"" + song.getTitle() + "\" de la biblioteca");
                } else if (getListView() == queueListView) {
                    controller.removeSongFromQueue(song);
                    showSelectionActionComplete("Eliminada \"" + song.getTitle() + "\" de la cola");
                }
            }
        }
    }
}
