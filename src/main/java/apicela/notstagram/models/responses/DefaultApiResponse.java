package apicela.notstagram.models.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultApiResponse<T> {
    private String message;
    private T data;

    public DefaultApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public DefaultApiResponse(String message) {
        this(message, null);
    }

    @Override
    public String toString() {
        return "DefaultApiResponse{" +
                "message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}