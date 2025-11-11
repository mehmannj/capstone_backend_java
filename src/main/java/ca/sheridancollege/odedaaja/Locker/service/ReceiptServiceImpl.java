package ca.sheridancollege.odedaaja.Locker.service;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
@Primary
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    @Override
    public String generateReceipt(Booking booking) {
        try {
            var outDir = Paths.get("./receipts");
            Files.createDirectories(outDir);

            String fileName = "receipt_booking_" + booking.getId() + ".pdf";

            // Generate simple HTML
            String html = """
                <html>
                <body>
                    <h1>Locker Booking Receipt</h1>
                    <p><b>Booking ID:</b> %d</p>
                    <p><b>Name:</b> %s</p>
                    <p><b>Email:</b> %s</p>
                    <p><b>Locker:</b> %s (%s / %s)</p>
                    <p><b>Term:</b> %s to %s</p>
                    <p><b>Total Amount:</b> %s</p>
                    <p><b>Status:</b> %s</p>
                </body>
                </html>
            """.formatted(
                booking.getId(),
                booking.getUserName(),
                booking.getUserEmail(),
                booking.getLocker().getLockerNumber(),
                booking.getLocker().getLocation().getBuilding(),
                booking.getLocker().getLocation().getFloor(),
                booking.getStartDate().format(DateTimeFormatter.ISO_DATE),
                booking.getEndDate().format(DateTimeFormatter.ISO_DATE),
                booking.getTotalAmount(),
                booking.getStatus()
            );

            // Write temp HTML file
            File htmlTmp = File.createTempFile("receipt_", ".html");
            try (var w = new java.io.OutputStreamWriter(new FileOutputStream(htmlTmp),
                    java.nio.charset.StandardCharsets.UTF_8)) {
                w.write(html);
            }

            // Generate PDF
            var pdfFile = outDir.resolve(fileName).toFile();
            try (FileOutputStream os = new FileOutputStream(pdfFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withFile(htmlTmp);
                builder.toStream(os);
                builder.run();
            }

            if (htmlTmp.exists()) htmlTmp.delete();

            // Return web URL (Spring will map it)
            return "/receipts/" + fileName;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate receipt: " + ex.getMessage(), ex);
        }
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
