package zip;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class ZipStreamCompressionTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ZipStreamCompression toTest = new ZipStreamCompression();

    @Test
    public void testEndToEnd() throws IOException {
        List<Path> filesToCompress = new LinkedList<>();
        filesToCompress.add(createTemporaryFile("file_1.txt", "Contents of file 1"));
        filesToCompress.add(createTemporaryFile("file_2.txt", "Contents of file 2"));
        filesToCompress.add(createTemporaryFile("file_3.txt", "Contents of file 3"));

        Path result = temporaryFolder.newFile("result.zip").toPath();

        // COMPRESS
        toTest.compressFiles(filesToCompress, result);

        assertTrue(Files.exists(result));

        Path extractedDirectory = temporaryFolder.newFolder("extracted").toPath();

        // DECOMPRESS
        toTest.decompressFiles(result, extractedDirectory);

        assertTrue(Files.exists(extractedDirectory));
        assertTrue(Files.isDirectory(extractedDirectory));

        Path[] extractedFiles = Files.list(extractedDirectory).toArray(Path[]::new);

        assertEquals(filesToCompress.size(), extractedFiles.length);

        for (int i = 0; i < filesToCompress.size(); i++) {
            Path originalFile = filesToCompress.get(i);
            Path actualFile = extractedFiles[i];

            // same name
            assertEquals(originalFile.getFileName(), actualFile.getFileName());
            // same size
            assertEquals(Files.size(originalFile), Files.size(actualFile));
            // same contents
            assertArrayEquals(Files.readAllBytes(originalFile), Files.readAllBytes(actualFile));
        }
    }

    private Path createTemporaryFile(String fileName, String fileContents) throws IOException {
        Path path = temporaryFolder.newFile(fileName).toPath();
        Files.write(path, fileContents.getBytes(StandardCharsets.UTF_8));
        return path;
    }
}