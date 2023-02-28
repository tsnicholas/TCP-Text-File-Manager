package tcp_file_project;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class TCPProcessor implements TCP {
    public String getMessageData(SocketChannel sc) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_TRANSFER_SIZE);
        sc.read(buffer);
        buffer.flip();
        return new String(buffer.array()).replace("\0", "");
    }

    public String readFromFile(File file, int i) throws IOException {
        char[] readData = new char[MAX_TRANSFER_SIZE];
        BufferedReader reader = new BufferedReader(new FileReader(file));
        System.out.println("Uploading " +
                reader.read(readData, i, i + MAX_TRANSFER_SIZE) + " characters...");
        return new String(readData);
    }

    public void writeToFile(File file, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }
}
