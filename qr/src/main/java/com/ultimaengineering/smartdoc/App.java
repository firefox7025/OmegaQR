package com.ultimaengineering.smartdoc;

import com.google.zxing.WriterException;
import com.ultimaengineering.smartdoc.qr.DataUtil;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;

/**
 * Hello world!
 * http://emaraic.com/blog/yolo-custom-object-detector
 */
@Slf4j
public class App 
{
    public static void main( String[] args ) throws WriterException {
        System.out.println( "Hello World!" );
        while(true) {
            BufferedImage bufferedImage = DataUtil.generateQrCode();
            System.out.println("Another one");
        }
    }
}
