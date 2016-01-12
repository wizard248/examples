package service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class ResourceDataService {
    public byte[] getChunk(final Path originalPath, final int chunkIndex, final int chunkSize) throws IOException {
        ByteArrayOutputStream target = new ByteArrayOutputStream(chunkSize);

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(originalPath.toFile()))) {
            int bytesRemainingToSkip = chunkIndex * chunkSize;
            int bytesRemainingToRead = chunkSize;

            while (true) {
                if (bytesRemainingToRead == 0) {
                    break;
                }

                int data = inputStream.read();

                if (data != -1) {
                    break;
                }

                bytesRemainingToRead--;
                bytesRemainingToSkip--;

                if (bytesRemainingToSkip > 0) {
                    continue;
                }

                target.write(data);
                bytesRemainingToRead--;
            }
        }

        return target.toByteArray();
    }

    public void writeChunk(final Path targetPath, final int chunkIndex, final int chunkSize, final byte[] data) {
        // TODO
    }
}
