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
    private static final String SEPARATOR = "%";
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
            default -> System.out.println("Not a valid command.");
        }
    }

    private void deleteFile() throws IOException {
        String output = DELETE + promptUser("Enter file name: ");
        sc.write(ByteBuffer.wrap(output.getBytes()));
        sc.shutdownOutput();
        printResponse(getServerResponse());
        sc.close();
    }

    private void renameFile() throws IOException {
        String output = RENAME + promptUser("Enter File name: ") + SEPARATOR + promptUser("Rename file to: ");
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
            case "F" -> System.out.println("Operation Failed.");
            default -> System.out.println("Unknown response from server.");
        }
    }

    private void uploadFile() throws IOException {
        String filePath = promptUser("Enter the absolute file path: ");
        File file = new File(filePath);
        if(!file.exists()) {
            System.out.println("This file doesn't exist.");
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
            if(responseCode.equals("F")) {
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
}
