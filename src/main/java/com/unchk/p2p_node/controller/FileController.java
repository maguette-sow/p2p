package com.unchk.p2p_node.controller;
import com.unchk.p2p_node.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * UPLOAD : Reçoit un fichier et le stocke.
     * Cette méthode est appelée par l'utilisateur OU par un autre nœud (réplication).
     */
    @PostMapping("/{filename}")
    public ResponseEntity<String> upload(
            @PathVariable String filename,
            @RequestBody byte[] data) {

        if (data == null || data.length == 0) {
            return ResponseEntity.badRequest().body("Le contenu du fichier est vide.");
        }

        fileService.saveFile(filename, data);
        return ResponseEntity.ok("Fichier " + filename + " enregistré et répliqué avec succès.");
    }

    /**
     * DOWNLOAD : Récupère un fichier.
     * Si le fichier n'est pas local, le service ira le chercher chez les pairs.
     */
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename) {
        byte[] data = fileService.getFile(filename);

        if (data != null) {
            // On définit le type de contenu comme flux d'octets pour le téléchargement
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } else {
            // ÉTAPE 4 & 5 : Si même après recherche chez les pairs on n'a rien
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
