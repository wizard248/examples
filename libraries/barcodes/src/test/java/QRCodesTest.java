import com.google.zxing.common.BitMatrix;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;

import static org.junit.Assert.assertEquals;

public class QRCodesTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testQrCodeGenerator() throws Exception {
        final String expected = "Hello world!";
        final File tmpFile = temporaryFolder.newFile();

        final BitMatrix bitMatrix = QRCodes.stringToQrCodeBitMatrix(expected, 100);
        final RenderedImage bitMatrixImage = QRCodes.renderBitMatrix(bitMatrix);
        ImageIO.write(bitMatrixImage, "png", tmpFile);
        final BufferedImage imageReadFromFile = ImageIO.read(tmpFile);
        final String actual = QRCodes.readTextFromQrCode(imageReadFromFile);

        assertEquals(expected, actual);
    }
}