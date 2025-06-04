package gruposantoro.elyctishuella.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
    private boolean success;
    private String message;
    private Object data;
}
