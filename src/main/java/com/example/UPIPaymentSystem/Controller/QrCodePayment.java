package com.example.UPIPaymentSystem.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QrCodePayment {
	
	@GetMapping("/QrCodePayment")
	public String QrCodeMethod()
	{
		return "Bank/QrCodePayment/scan-qr";
	}
	
	// ============================
    // POST: Accept Scanned UPI Data
    // ============================
    @PostMapping("/QrCodePayment/submit-upi")
    public String submitUpi(
            @RequestParam("upi") String upiId,
            Model model) 
    {
    	System.out.println(upiId);
    	System.out.println(upiId);
    	System.out.println(upiId);
    	System.out.println(upiId);
    	System.out.println(upiId);
        
    	// Add UPI ID to model to show in next page
        model.addAttribute("upi", upiId);
        
        // You can redirect to any confirmation/payment page
        return "redirect:/dashboard";
    }
}
