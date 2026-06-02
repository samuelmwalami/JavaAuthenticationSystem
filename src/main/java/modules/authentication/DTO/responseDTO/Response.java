package modules.authentication.DTO.responseDTO;

import java.util.ArrayList;

public class Response<T> {
    public T data;
    public ArrayList<String> errors = new ArrayList<>();
}
