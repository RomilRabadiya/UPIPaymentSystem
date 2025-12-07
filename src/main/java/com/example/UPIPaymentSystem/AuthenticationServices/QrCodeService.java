package com.example.UPIPaymentSystem.AuthenticationServices;

import java.awt.image.BufferedImage;

//Using library:

//ZXing Library
//ZXing Library

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrCodeService {

    public byte[] generateQRCode(String data, int width, int height) throws Exception 
    {
    	//QRCodeWriter → creates the QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        //BitMatrix → holds the QR pattern in our case is data
        //qrCodeWriter.enocde method will be convert our data To QR Code
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //MatrixToImageWriter → converts QR pattern to an image (PNG)
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }
    
    
    public String decodeQRCode(File qrCodeFile) throws Exception 
    {
        BufferedImage bufferedImage = ImageIO.read(qrCodeFile);

        if (bufferedImage == null) 
        {
            throw new Exception("Could not read QR Code image");
        }

        BinaryBitmap binaryBitmap = new BinaryBitmap(
                new HybridBinarizer(
                        new BufferedImageLuminanceSource(bufferedImage)
                )
        );

        try 
        {
            Result result = new MultiFormatReader().decode(binaryBitmap);
            return result.getText();  // returns UPI string (upi://pay?... )
        } 
        catch (NotFoundException e) 
        {
            return "QR Code not found in the image";
        }
    }
}
