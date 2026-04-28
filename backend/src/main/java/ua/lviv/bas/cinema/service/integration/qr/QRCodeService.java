package ua.lviv.bas.cinema.service.integration.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.lviv.bas.cinema.exception.domain.technical.QRCodeGenerationException;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class QRCodeService {

    @Value("${app.qr.code.margin:1}")
    private int qrCodeMargin;

    @Value("${app.qr.code.format:PNG}")
    private String qrCodeFormat;

    public byte[] generateQRCode(String content, int size) {
        log.debug("Generating QR code for content: {}", content);

        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, qrCodeMargin);

            var qrCodeWriter = new QRCodeWriter();
            var bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            var bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            var baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, qrCodeFormat, baos);

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate QR code: {}", e.getMessage());
            throw new QRCodeGenerationException("Failed to generate QR code", e);
        }
    }
}