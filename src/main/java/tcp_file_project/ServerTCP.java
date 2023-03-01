package tcp_file_project;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerTCP extends TCPProcessor implements TCP {
    private static SocketChannel serveChannel;
    private File serverDirectory;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ServerTCP <port>");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            ServerSocketChannel listenChannel = ServerSocketChannel.open();
            listenChannel.bind(new InetSocketAddress(port));
            ServerTCP serverTCP = new ServerTCP();
            serverTCP.initializeDirectory();
            serverTCP.startService(listenChannel);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeDirectory() {
        serverDirectory = new File(System.getProperty("user.dir") + "\\ServerDirectory");
        if(!serverDirectory.exists()) {
            if(serverDirectory.mkdir()) {
                System.out.println("Directory successfully created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void startService(ServerSocketChannel listenChannel) throws IOException {
        while(true) {
            serveChannel = listenChannel.accept();
            String messageToRead = getMessageData(serveChannel);
            performCommand(messageToRead);
            serveChannel.close();
        }
    }

    private void performCommand(String messageToRead) throws NullPointerException, IOException {
        char command = messageToRead.charAt(0);
        String fileData = messageToRead.substring(1);
        switch(String.valueOf(command)) {
            case DELETE -> deleteFile(fileData);
            case RENAME -> renameFile(fileData);
            case LIST -> listFiles();
            case UPLOAD -> retrieveUpload(fileData);
            case DOWNLOAD -> downloadFile(fileData);
            default -> System.out.println("Not a valid command.");
        }
    }

    private void deleteFile(String fileName) throws IOException {
        File file = new File(serverDirectory.getAbsolutePath() + SLASH + fileName);
        if(file.exists()) {
            if (file.delete()) {
                System.out.println("Deleted the file: " + file.getName());
                respondToClient(SUCCESS);
            } else {
                System.out.println("Failed to delete the file.");
                respondToClient(FAILURE);
            }
        } else {
            System.out.println(DEFAULT_FILE_DOES_NOT_EXIST_MSG);
            respondToClient(FAILURE);
        }
    }

    private void respondToClient(String operationCode) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(operationCode.getBytes());
        serveChannel.write(buffer);
    }

    private void renameFile(String fileName) throws IOException {
        String[] arrOfStr = fileName.split(SEPARATOR,2);
        File oldName = new File(serverDirectory.getAbsolutePath() + SLASH + arrOfStr[0]);
        File newName = new File(serverDirectory.getAbsolutePath() + SLASH + arrOfStr[1]);
        if (oldName.renameTo(newName)) {
            System.out.println("File Rename Successful");
            respondToClient(SUCCESS);
        } else {
            System.out.println("Rename Failed");
            respondToClient(FAILURE);
        }
    }

    private void listFiles() throws IOException {
        File directory = new File(serverDirectory.getAbsolutePath());
        File[] listOfFiles = directory.listFiles();
        StringBuilder response = new StringBuilder();
        if(listOfFiles == null) {
            listResponse("");
        } else {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    response.append(file.getName()).append("\n");
                }
            }
            listResponse(response.toString());
        }
    }

    private void listResponse(String response) throws IOException {
        ByteBuffer buffer;
        buffer = ByteBuffer.wrap(response.getBytes());
        serveChannel.write(buffer);
    }

    private void retrieveUpload(String fileName) throws IOException {
        File file = initializeFile(fileName);
        receiveFile(serveChannel, file);
        respondToClient(SUCCESS);
    }

    private File initializeFile(String fileName) throws IOException {
        File file = new File(serverDirectory.getAbsolutePath() + SLASH + fileName);
        if(!file.exists()) {
            if(file.createNewFile()) {
                System.out.println("New file has been created.");
            } else {
                System.out.println("Failed to create file.");
                respondToClient(FAILURE);
                throw new IOException();
            }
        }
        return file;
    }

    private void downloadFile(String fileName) throws IOException {
        File file = new File(serverDirectory.getAbsolutePath() + SLASH + fileName);
        if (!file.exists()) {
            System.out.println(DEFAULT_FILE_DOES_NOT_EXIST_MSG);
            respondToClient(FAILURE);
        } else {
            respondToClient(transferFile(serveChannel, file));
        }
    }
}
