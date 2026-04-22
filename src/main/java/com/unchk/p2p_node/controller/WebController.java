package com.unchk.p2p_node.controller;

import com.unchk.p2p_node.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/ui")
public class WebController {
    @Autowired
    private FileService fileService;
    @Value("${server.port}")
    private String port;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("files", fileService.listAvailableFiles()); // À ajouter dans FileService
        model.addAttribute("port", port);
        return "index";
    }

    @GetMapping("/delete/{filename}")
    public String deleteFile(@PathVariable String filename) {
        fileService.deleteFile(filename);
        return "redirect:/ui"; // Recharge la page après suppression
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                fileService.saveFile(file.getOriginalFilename(), file.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/ui"; // Redirige vers la page principale après l'envoi
    }



}

