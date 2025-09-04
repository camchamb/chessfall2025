package dataaccess;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{
    private Integer status = null;

    public DataAccessException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static DataAccessException fromJson(InputStream stream) {
        var map = new Gson().fromJson(new InputStreamReader(stream), HashMap.class);
//        var status = ((Double)map.get("status")).intValue();
        String message = map.get("message").toString();
        return new DataAccessException(400, message);
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", status));
    }

    public int getStatus() {
        return status;
    }
}