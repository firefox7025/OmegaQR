package com.ultimaengineering.smartdoc.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

import java.awt.image.BufferedImage;
import java.util.Random;


@UtilityClass
public class DataUtil {

    private final Random rand = new Random();
    private final static int MAXSIZE = 750;
    private final static int MINSIZE = 50;
    private final QRCodeWriter qrCodeWriter = new QRCodeWriter();
    

    public BufferedImage generateQrCode() throws WriterException {
        int qrCodeSize = rand.nextInt((MAXSIZE - MINSIZE) + 1) + MINSIZE;
        BitMatrix bitMatrix = qrCodeWriter.encode(randomText(), BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }


    public String randomText() {
        int length = rand.nextInt((50 - 10) + 1) + 10;
        boolean useLetters = true;
        boolean useNumbers = true;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }
}
