package com.unchk.p2p_node.service;

import com.unchk.p2p_node.config.NodeConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    @Autowired
    private NodeConfig config;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        File directory = new File(config.getStorage());
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // ÉTAPE 1 : Sauvegarder et répliquer
    public void saveFile(String filename, byte[] data) {
        try {
            Path path = Paths.get(config.getStorage(), filename);

            if (!Files.exists(path)) {
                Files.write(path, data);
                System.out.println("✅ SUCCÈS : Fichier [" + filename + "] enregistré dans " + config.getStorage());

                // Appel de la réplication
                replicateFile(filename, data);
            } else {
                System.out.println("ℹ️ INFO : Fichier [" + filename + "] déjà présent. Fin de la propagation.");
            }
        } catch (IOException e) {
            System.err.println("❌ ERREUR : Impossible de sauvegarder " + filename);
        }
    }

    // ÉTAPE 3 : La réplication (UNE SEULE FOIS)
    private void replicateFile(String filename, byte[] data) {
        for (String peer : config.getPeers()) {
            try {
                System.out.println("📡 RÉPLICATION : Envoi vers le pair -> " + peer);
                String url = peer + "/files/" + filename;
                restTemplate.postForEntity(url, data, String.class);
            } catch (Exception e) {
                System.err.println("⚠️ ALERTE : Le pair " + peer + " est injoignable.");
            }
        }
    }

    // ÉTAPE 1 : Lire un fichier
    public byte[] getFile(String filename) {
        try {
            Path path = Paths.get(config.getStorage(), filename);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture locale");
        }
        // ÉTAPE 4 : Recherche distribuée
        return searchInPeers(filename);
    }

    // ÉTAPE 4 : Recherche distribuée
    private byte[] searchInPeers(String filename) {
        for (String peer : config.getPeers()) {
            try {
                String url = peer + "/files/" + filename;
                byte[] data = restTemplate.getForObject(url, byte[].class);
                if (data != null) {
                    System.out.println("🔍 TROUVÉ : Fichier récupéré sur le pair -> " + peer);
                    return data;
                }
            } catch (Exception e) {
                // On passe au pair suivant sans erreur bloquante
            }
        }
        return null;
    }

    public List<String> listAvailableFiles() {
        File folder = new File(config.getStorage());
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            return Arrays.stream(listOfFiles)
                    .filter(File::isFile) // On ne prend que les fichiers, pas les dossiers
                    .map(File::getName)   // On récupère uniquement les noms
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public boolean deleteFile(String filename) {
        try {
            Path path = Paths.get(config.getStorage(), filename);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("❌ ERREUR : Impossible de supprimer " + filename);
            return false;
        }
    }

}
