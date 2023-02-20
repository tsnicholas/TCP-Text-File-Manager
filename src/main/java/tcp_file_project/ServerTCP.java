package tcp_file_project;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerTCP {
    private SocketChannel serveChannel;

    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Usage: ServerTCP <port>");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            ServerSocketChannel listenChannel = ServerSocketChannel.open();
            listenChannel.bind(new InetSocketAddress(port));
            ServerTCP serverTCP = new ServerTCP();
            serverTCP.startService(listenChannel);
        } catch(IOException e) {
            e.printStackTrace();
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

    private void performCommand(String messageToRead) throws NullPointerException {
        char command = messageToRead.charAt(0);
        String fileName = messageToRead.substring(1);
        switch(command) {
            case 'd' -> deleteFile(fileName);
            case 'D' -> System.out.println("Feature not Available yet");
            default -> System.out.println("Not a valid command.");
        }
    }

    private void deleteFile(String fileName) throws NullPointerException {
        File file = new File(fileName);
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
}
