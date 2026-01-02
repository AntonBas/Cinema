package ua.lviv.bas.cinema.service.common;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QRCodeService {

	@Value("${app.qr.code.size:200}")
	private int qrCodeSize;

	@Value("${app.qr.code.margin:1}")
	private int qrCodeMargin;

	@Value("${app.qr.code.format:PNG}")
	private String qrCodeFormat;

	public byte[] generateQRCode(String content) {
		log.debug("Generating QR code for content: {}", content);

		try {
			Map<EncodeHintType, Object> hints = new HashMap<>();
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			hints.put(EncodeHintType.MARGIN, qrCodeMargin);

			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);

			BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, qrCodeFormat, baos);

			return baos.toByteArray();

		} catch (Exception e) {
			log.error("Failed to generate QR code: {}", e.getMessage());
			throw new RuntimeException("Failed to generate QR code", e);
		}
	}

	public byte[] generateQRCode(String content, int size) {
		log.debug("Generating QR code for content with custom size: {}", content);

		try {
			Map<EncodeHintType, Object> hints = new HashMap<>();
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			hints.put(EncodeHintType.MARGIN, qrCodeMargin);

			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

			BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, qrCodeFormat, baos);

			return baos.toByteArray();

		} catch (Exception e) {
			log.error("Failed to generate QR code with custom size: {}", e.getMessage());
			throw new RuntimeException("Failed to generate QR code", e);
		}
	}

	public String generateQRCodeBase64(String content) {
		byte[] qrCodeBytes = generateQRCode(content);
		return java.util.Base64.getEncoder().encodeToString(qrCodeBytes);
	}
}
