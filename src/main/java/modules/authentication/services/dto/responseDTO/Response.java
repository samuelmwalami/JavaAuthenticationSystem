package modules.authentication.services.dto.responseDTO;

import java.util.ArrayList;

public class Response<T> {
    public T data;
    public ArrayList<String> errors = new ArrayList<>();
}
