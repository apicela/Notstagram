package apicela.notstagram.models.requests;

import org.springframework.web.multipart.MultipartFile;

public record PostDTORequest (String description, MultipartFile file) {
}
