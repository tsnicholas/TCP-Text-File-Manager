package tcp_file_project;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ServerTCP {
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
            serverTCP.createDirectory();
            serverTCP.startService(listenChannel);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void createDirectory() {
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
            fulfillRequest(messageToRead);
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

    private void fulfillRequest(String messageToRead) throws IOException {
        ByteBuffer buffer;
        try {
            performCommand(messageToRead);
            buffer = ByteBuffer.wrap("S".getBytes());
        } catch(NullPointerException e) {
            buffer = ByteBuffer.wrap("F".getBytes());
        }
        serveChannel.write(buffer);
    }

    private void performCommand(String messageToRead) throws NullPointerException, IOException {
        char command = messageToRead.charAt(0);
        String fileName = messageToRead.substring(1);
        switch(command) {
            case 'd' -> deleteFile(fileName);
            case 'D' -> System.out.println("Feature not Available yet");
            case 'r' -> renameFile(fileName);
            case 'l' -> listFiles();
            default -> System.out.println("Not a valid command.");
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

    private void deleteFile(String fileName) throws NullPointerException {
        File file = new File(serverDirectory.getAbsolutePath() + fileName);
        if(file.exists()) {
            if (file.delete()) {
                System.out.println("Deleted the file: " + file.getName());
            } else {
                System.out.println("Failed to delete the file.");
                // While not really a NullPointerException, still need to let the client know the request failed.
                throw new NullPointerException();
            }
        } else {
            System.out.println("File doesn't exist.");
            throw new NullPointerException();
        }
    }

    private void renameFile(String fileName) throws NullPointerException {
        String[] arrOfStr = fileName.split("%",1);
        File oldName = new File(serverDirectory.getAbsolutePath() + arrOfStr[0]);
        File newName = new File(serverDirectory.getAbsolutePath() + arrOfStr[1]);
        if (oldName.renameTo(newName)) {
            System.out.println("File Rename Successful");
        } else {
            System.out.println("Rename Failed");
            // Once again, we have let the client know the operation failed.
            throw new NullPointerException();
        }
    }
}
