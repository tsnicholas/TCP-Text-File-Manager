package tcp_file_project;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientTCP {
    private static final String RENAME = "r";
    private static final String DELETE = "d";
    private static final String LIST = "l";
    private static final String UPLOAD = "u";
    private static final String DOWNLOAD = "D";
    private static final String SEPARATOR = "%";
    private static final String FAILURE = "F";
    private static final String GENERIC_FILE_PROMPT = "Enter file name: ";
    private static final String DEFAULT_FILE_DOES_NOT_EXIST_MSG = "This file doesn't exist.";
    private static final int MAX_TRANSFER_SIZE = 400;
    private static SocketChannel sc;

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Syntax: ClientTCP <IP Address> <Port Number>");
            return;
        }
        try {
            ClientTCP clientTCP = new ClientTCP();
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
            String command = clientTCP.getCommand();
            clientTCP.performCommand(command);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String getCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your command: ");
        return scanner.nextLine();
    }

    private void performCommand(String command) throws IOException {
        switch(command) {
            case DELETE -> deleteFile();
            case RENAME -> renameFile();
            case LIST -> listFile();
            case UPLOAD -> uploadFile();
            case DOWNLOAD -> downloadFile();
            default -> System.out.println("Not a valid command.");
        }
    }

    private void deleteFile() throws IOException {
        String output = DELETE + promptUser(GENERIC_FILE_PROMPT);
        sc.write(ByteBuffer.wrap(output.getBytes()));
        sc.shutdownOutput();
        printResponse(getServerResponse());
        sc.close();
    }

    private void renameFile() throws IOException {
        String output = RENAME + promptUser(GENERIC_FILE_PROMPT) + SEPARATOR + promptUser("Rename file to: ");
        sc.write(ByteBuffer.wrap(output.getBytes()));
        sc.shutdownOutput();
        printResponse(getServerResponse());
        sc.close();
    }

    private void listFile() throws IOException {
        sc.write(ByteBuffer.wrap(LIST.getBytes()));
        sc.shutdownOutput();
        System.out.println(getServerResponse());
        sc.close();
    }

    private String promptUser(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(prompt);
        return scanner.nextLine();
    }

    private String getServerResponse() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_TRANSFER_SIZE);
        sc.read(buffer);
        buffer.flip();
        // Replaces null characters before returning.
        return new String(buffer.array()).replace("\0", "");
    }

    private void printResponse(String responseCode) {
        switch(responseCode) {
            case "S" -> System.out.println("Operation Successful.");
            case FAILURE -> System.out.println("Operation Failed.");
            default -> System.out.println("Unknown response from server.");
        }
    }

    private void uploadFile() throws IOException {
        String filePath = promptUser("Enter the absolute file path: ");
        File file = new File(filePath);
        if(!file.exists()) {
            System.out.println(DEFAULT_FILE_DOES_NOT_EXIST_MSG);
        } else {
            writeFileToServer(file);
        }
        sc.close();
    }

    private void writeFileToServer(File file) throws IOException {
        for(int i = 0; i < file.length(); i += MAX_TRANSFER_SIZE) {
            String uploadData = UPLOAD + file.getName() + SEPARATOR + readFromFile(file, i);
            sc.write(ByteBuffer.wrap(uploadData.getBytes()));
            String responseCode = getServerResponse();
            printResponse(responseCode);
            // If for some reason the server fails during the process, terminate the loop.
            if(responseCode.equals(FAILURE)) {
                break;
            }
        }
    }

    private String readFromFile(File file, int i) throws IOException {
        char[] readData = new char[MAX_TRANSFER_SIZE];
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        System.out.println("Uploading " +
                bufferedReader.read(readData, i, i + MAX_TRANSFER_SIZE) + " characters...");
        return new String(readData);
    }

    private void downloadFile() throws IOException {
        String fileName = promptUser(GENERIC_FILE_PROMPT);
        if(fileExistsInServer(fileName)) {
            startDownload(fileName);
        } else {
            System.out.println(DEFAULT_FILE_DOES_NOT_EXIST_MSG);
        }
    }

    private boolean fileExistsInServer(String fileName) throws IOException {
        sc.write(ByteBuffer.wrap(LIST.getBytes()));
        ByteBuffer buffer = ByteBuffer.allocate(MAX_TRANSFER_SIZE);
        sc.read(buffer);
        buffer.flip();
        return new String(buffer.array()).contains(fileName);
    }

    private void startDownload(String fileName) throws IOException {
        String downloadPath = promptUser("Enter the path to save download: ");
        File file = new File(downloadPath + "\\" + fileName);
        if(file.exists()) {
            System.out.println("This file already exists.");
            throw new IOException();
        }
        initializeDownload(file);
        getFileContents(file);
    }

    private void initializeDownload(File file) throws IOException {
        if(file.createNewFile()) {
            System.out.println("Downloading " + file.getName() + "...");
        } else {
            System.out.println("Error: Couldn't create file in that directory.");
            throw new IOException();
        }
    }

    private void getFileContents(File file) throws IOException {
        while(true) {
            String serverMsg = DOWNLOAD + file.getName();
            sc.write(ByteBuffer.wrap(serverMsg.getBytes()));
            String response = getServerResponse();
            if(response.equals(SEPARATOR)) {
                break;
            } else if(response.equals(FAILURE)) {
                printResponse(response);
                throw new IOException();
            } else {
                writeToFile(file, response);
            }
        }
    }

    private void writeToFile(File file, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }
}
