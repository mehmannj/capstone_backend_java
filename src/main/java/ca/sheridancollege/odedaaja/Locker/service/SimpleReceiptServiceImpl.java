package ca.sheridancollege.odedaaja.Locker.service;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service("simpleReceiptService")
public class SimpleReceiptServiceImpl implements ReceiptService {

    @Override
    public String generateReceipt(Booking booking) {
        // For now, return a mock receipt URL
        // In a real implementation, this would generate a PDF receipt
        return "/receipts/receipt_" + booking.getId() + ".pdf";
    }

    @Override
    public String generateReceiptContent(Booking booking) {
        StringBuilder receipt = new StringBuilder();
        
        receipt.append("========================================\n");
        receipt.append("           LOCKER RENTAL RECEIPT        \n");
        receipt.append("========================================\n\n");
        
        receipt.append("Receipt #: ").append(booking.getId()).append("\n");
        receipt.append("Date: ").append(booking.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        receipt.append("CUSTOMER INFORMATION:\n");
        receipt.append("Name: ").append(booking.getUserName()).append("\n");
        receipt.append("Email: ").append(booking.getUserEmail()).append("\n\n");
        
        receipt.append("LOCKER DETAILS:\n");
        receipt.append("Locker: ").append(booking.getLocker().getLockerNumber()).append("\n");
        receipt.append("Location: ").append(booking.getLocker().getLocation().getBuilding())
                .append(" - ").append(booking.getLocker().getLocation().getFloor()).append("\n");
        receipt.append("Duration: ").append(booking.getDuration().getMonths()).append(" months\n");
        receipt.append("Start Date: ").append(booking.getStartDate()).append("\n");
        receipt.append("End Date: ").append(booking.getEndDate()).append("\n\n");
        
        receipt.append("PAYMENT DETAILS:\n");
        receipt.append("Base Price: $").append(booking.getPriceSubTotal()).append("\n");
        receipt.append("Tax (13%): $").append(booking.getTaxAmount()).append("\n");
        receipt.append("Processing Fee: $").append(booking.getFeesAmount()).append("\n");
        receipt.append("----------------------------------------\n");
        receipt.append("TOTAL: $").append(booking.getTotalAmount()).append("\n\n");
        
        receipt.append("Payment Status: ").append(booking.getStatus()).append("\n");
        receipt.append("Payment Reference: ").append(booking.getPaymentReference()).append("\n\n");
        
        receipt.append("Thank you for using InstiManage!\n");
        receipt.append("========================================\n");
        
        return receipt.toString();
    }
}

