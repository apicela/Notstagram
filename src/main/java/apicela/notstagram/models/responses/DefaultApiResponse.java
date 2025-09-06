package apicela.notstagram.models.responses;

import lombok.Data;

@Data
public class DefaultApiResponse<T> {
    private String message;
    private T data;
    private int status;

    public DefaultApiResponse(String message, T data, int statusCode) {
        this.message = message;
        this.data = data;
        this.status = statusCode;
    }

    public DefaultApiResponse(String message, int statusCode) {
        this(message, null, statusCode);
    }

    @Override
    public String toString() {
        return "DefaultApiResponse{" +
                "message='" + message + '\'' +
                ", data=" + data +
                ", status=" + status +
                '}';
    }
}