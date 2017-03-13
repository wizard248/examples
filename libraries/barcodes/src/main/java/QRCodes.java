import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class QRCodes {
    public static RenderedImage renderBitMatrix(final BitMatrix bitMatrix) {
        final BufferedImage bufferedImage = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), IndexColorModel.BITMASK);
        final Graphics graphics = bufferedImage.getGraphics();

        for (int x = 0; x < bitMatrix.getWidth(); x++) {
            for (int y = 0; y < bitMatrix.getHeight(); y++) {
                graphics.setColor(bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                graphics.fillRect(x, y, 1, 1);
            }
        }

        return bufferedImage;
    }

    public static BitMatrix stringToQrCodeBitMatrix(final String contents, final int bitmapSize) throws WriterException {
        final Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        // set character set to UTF-8
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        // set margin to zero to not waste space
        hints.put(EncodeHintType.MARGIN, 0);
        // set error correction level to low
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        final Writer writer = new MultiFormatWriter();
        return writer.encode(contents, BarcodeFormat.QR_CODE, bitmapSize, bitmapSize, hints);
    }

    public static String readTextFromQrCode(final BufferedImage qrCodeImage) throws FormatException, ChecksumException, NotFoundException {
        final Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);

        final LuminanceSource luminanceSource = new BufferedImageLuminanceSource(qrCodeImage);
        final HybridBinarizer binarizer = new HybridBinarizer(luminanceSource);
        final BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
        final Reader reader = new MultiFormatReader();
        final Result result = reader.decode(binaryBitmap, hints);
        return result.getText();
    }
}