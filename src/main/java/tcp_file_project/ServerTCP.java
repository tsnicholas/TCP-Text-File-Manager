package tcp_file_project;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
public class ServerTCP {
    private static final String SUCCESS = "S";
    private static final String FAILURE = "F";
    private static final String SEPARATOR = "%";
    private SocketChannel serveChannel;
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
        String dir = System.getProperty("user.dir");
        serverDirectory = new File(dir + "\\ServerDirectory");
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
            ByteBuffer buffer = getRequest();
            String messageToRead = convertBytesToString(buffer);
            performCommand(messageToRead);
            serveChannel.close();
        }
    }

    private ByteBuffer getRequest() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        serveChannel.read(buffer);
        buffer.flip();
        return buffer;
    }

    private String convertBytesToString(ByteBuffer buffer) {
        byte[] bytes = buffer.array();
        String input = new String(bytes);
        // Remove null bytes in the string.
        return input.replace("\0", "");
    }

    private void performCommand(String messageToRead) throws NullPointerException, IOException {
        char command = messageToRead.charAt(0);
        String fileData = messageToRead.substring(1);
        switch(command) {
            case 'd' -> deleteFile(fileData);
            case 'r' -> renameFile(fileData);
            case 'l' -> listFiles();
            case 'u' -> retrieveUpload(fileData);
            default -> System.out.println("Not a valid command.");
        }
    }

    private void deleteFile(String fileName) throws IOException {
        File file = new File(serverDirectory.getAbsolutePath() + "\\" + fileName);
        if(file.exists()) {
            if (file.delete()) {
                System.out.println("Deleted the file: " + file.getName());
                respondToClient(SUCCESS);
            } else {
                System.out.println("Failed to delete the file.");
                respondToClient(FAILURE);
            }
        } else {
            System.out.println("File doesn't exist.");
            respondToClient(FAILURE);
        }
    }

    private void respondToClient(String operationCode) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(operationCode.getBytes());
        serveChannel.write(buffer);
    }

    private void renameFile(String fileName) throws IOException {
        String[] arrOfStr = fileName.split(SEPARATOR,2);
        File oldName = new File(serverDirectory.getAbsolutePath() + "\\" + arrOfStr[0]);
        File newName = new File(serverDirectory.getAbsolutePath() + "\\" + arrOfStr[1]);
        if (oldName.renameTo(newName)) {
            System.out.println("File Rename Successful");
            respondToClient(SUCCESS);
        } else {
            System.out.println("Rename Failed");
            // Once again, we have let the client know the operation failed.
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
                    response.append(file.getName());
                    response.append("\n");
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

    private void retrieveUpload(String fileData) throws IOException {
        String[] fileContents = fileData.split(SEPARATOR, 2);
        File file = initializeFile(fileContents[0]);
        uploadContents(file, fileContents[1]);
        respondToClient(SUCCESS);
    }

    private File initializeFile(String fileName) throws IOException {
        File file = new File(serverDirectory.getAbsolutePath() + "\\" + fileName);
        if(!file.exists()) {
            if(file.createNewFile()) {
                System.out.println("New file has been created :)");
            } else {
                System.out.println("Failed to create file. :(");
                respondToClient(FAILURE);
                throw new IOException();
            }
        }
        return file;
    }

    private void uploadContents(File file, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }
}
