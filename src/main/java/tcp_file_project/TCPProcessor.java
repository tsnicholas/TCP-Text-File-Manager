package tcp_file_project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
}
