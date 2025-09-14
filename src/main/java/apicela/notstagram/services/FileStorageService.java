package apicela.notstagram.services;

import apicela.notstagram.models.dtos.FileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${upload.dir}")
    private String uploadDir;

    public FileDTO saveFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path filePath = Paths.get(uploadDir, fileName);

        // Cria diretório se não existir
        Files.createDirectories(filePath.getParent());
        // Salva fisicamente no sistema de arquivos
        Files.write(filePath, file.getBytes());

        return new FileDTO(filePath.toString(), file.getContentType());
    }
}
