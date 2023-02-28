package tcp_file_project;

public interface TCP {
    // Commands
    String DOWNLOAD = "D";
    String UPLOAD = "u";
    String LIST = "l";
    String DELETE = "d";
    String RENAME = "r";
    String SUCCESS = "S";

    // Response Codes
    String FAILURE = "F";
    String SEPARATOR = "%";

    // Other constant data
    int MAX_TRANSFER_SIZE = 400;
    String NULL_BYTE = "\0";
    String NOTHING = "";
    String SLASH = "\\";
    String DEFAULT_FILE_DOES_NOT_EXIST_MSG = "This file doesn't exist.";
}