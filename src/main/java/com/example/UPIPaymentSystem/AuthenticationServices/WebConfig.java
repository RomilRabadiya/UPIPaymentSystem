package com.example.UPIPaymentSystem.AuthenticationServices;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;


//Browser cannot directly read your local file system.
//So Spring provides a public URL to access files.


//This WebConfig class customizes how Spring Boot serves static files.
//It mainly does two jobs:

//1. Serve Static Images from static/images/ Folder
//Spring will fetch it from:
//src/main/resources/static/images/logo.png

//2. Serve QR Code Images Stored Outside the Project

///uploads/QRCodes/
//Since this folder is not inside resources/static, Spring cannot access it automatically.
//So you convert that folder to file:/// URL:

//Then you register a resource handler:
//
//registry.addResourceHandler("/uploads/QRCodes/**", "/qr/**")
//        .addResourceLocations(fileUrl);
//Meaning:
//
//URL: http://localhost:8080/uploads/QRCodes/1234_qr.png
//
//Actual file: project-root/uploads/QRCodes/1234_qr.png



@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	
        //Allowed To Browser use static/images file url
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        //Extract QRCode file from "/uploads/QRCodes"
        String qrCodeDirPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "QRCodes";
        File qrCodeDir = new File(qrCodeDirPath);
        if (!qrCodeDir.exists()) qrCodeDir.mkdirs();
        

//        Purpose:
//        	Convert a local folder path into a file URL so Spring can serve external files (like dynamically generated QR codes) via HTTP.
//
//        How it works:
//
//        	qrCodeDir.getAbsolutePath() → Gets the full path on disk.
//
//        	.replace("\\", "/") → Converts Windows backslashes to forward slashes for URL compatibility.
//
//        	"file:///" + ... + "/" → Converts the path into a file:// URL, which Spring can use in addResourceLocations().
        
        // Convert to file URL
        String fileUrl = "file:///" + qrCodeDir.getAbsolutePath().replace("\\", "/") + "/";
        
        
        
        // Register handlers
        
//        addResourceHandler → Maps URL patterns to actual files.
//        Defines URL patterns that users can access in their browser.
//        Example:
//        http://localhost:8080/uploads/QRCodes/9726623330_qr.png
        
//        addResourceLocations(fileUrl) → Tells Spring where to find the files on disk.
        
//        fileUrl points to a folder outside the project resources, e.g., uploads/QRCodes/ in your project root.
//        This tells Spring: “If someone requests a file matching the above URL patterns, 
//        serve it from this external folder.”
        registry.addResourceHandler("/uploads/QRCodes/**", "/qr/**")
                .addResourceLocations(fileUrl);
    }
}
