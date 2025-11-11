package ca.sheridancollege.odedaaja.Locker.service;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.domain.ContractTemplate;
import ca.sheridancollege.odedaaja.Locker.repo.ContractTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

@Service
@Primary
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractTemplateRepository templateRepo;

    @Override
    public String generateContract(Booking booking) {
        try {
            ContractTemplate tpl = templateRepo.findFirstByActiveTrueOrderByVersionDesc()
                    .orElseGet(() -> ContractTemplate.builder()
                            .name("Default")
                            .version(1)
                            .htmlTemplate("<html><body><h1>Locker Contract</h1>" +
                                    "<p>Renter: ${userName} (${userEmail})</p>" +
                                    "<p>Locker: ${lockerNumber}, Location: ${building}/${floor}</p>" +
                                    "<p>Term: ${startDate} to ${endDate} (${duration})</p>" +
                                    "<p>Total: ${total}</p>" +
                                    "<p>Terms: Standard locker terms apply.</p></body></html>")
                            .build());

            // FreeMarker setup
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            Template template = new Template("contract", tpl.getHtmlTemplate(), cfg);

            Map<String, Object> model = new HashMap<>();
            model.put("userName", booking.getUserName());
            model.put("userEmail", booking.getUserEmail());
            model.put("lockerNumber", booking.getLocker().getLockerNumber());
            model.put("building", booking.getLocker().getLocation().getBuilding());
            model.put("floor", booking.getLocker().getLocation().getFloor());
            model.put("startDate", booking.getStartDate().format(DateTimeFormatter.ISO_DATE));
            model.put("endDate", booking.getEndDate().format(DateTimeFormatter.ISO_DATE));
            model.put("duration", booking.getDuration().name());
            model.put("total", booking.getTotalAmount());

            var outDir = Paths.get("./contracts");
            Files.createDirectories(outDir);

            String fileName = "contract_booking_" + booking.getId() + ".pdf";
            File htmlTmp = File.createTempFile("contract_", ".html");
            try (var w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(htmlTmp),
                    java.nio.charset.StandardCharsets.UTF_8)) {
                template.process(model, w);
            }

            var pdfFile = outDir.resolve(fileName).toFile();
            try (FileOutputStream os = new FileOutputStream(pdfFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withFile(htmlTmp);
                builder.toStream(os);
                builder.run();
            }

            if (htmlTmp.exists()) htmlTmp.delete();
            return "/contracts/" + fileName;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate PDF: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String generateContractContent(Booking booking) {
        StringBuilder contract = new StringBuilder();
        
        contract.append("========================================\n");
        contract.append("         LOCKER RENTAL AGREEMENT        \n");
        contract.append("========================================\n\n");
        
        contract.append("Contract #: ").append(booking.getId()).append("\n");
        contract.append("Date: ").append(booking.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        contract.append("PARTIES:\n");
        contract.append("Lessor: Sheridan College\n");
        contract.append("Lessee: ").append(booking.getUserName()).append(" (").append(booking.getUserEmail()).append(")\n\n");
        
        contract.append("LOCKER DETAILS:\n");
        contract.append("Locker Number: ").append(booking.getLocker().getLockerNumber()).append("\n");
        contract.append("Location: ").append(booking.getLocker().getLocation().getBuilding())
                .append(" - ").append(booking.getLocker().getLocation().getFloor()).append("\n");
        contract.append("Rental Period: ").append(booking.getStartDate()).append(" to ").append(booking.getEndDate()).append("\n");
        contract.append("Duration: ").append(booking.getDuration().getMonths()).append(" months\n\n");
        
        contract.append("TERMS AND CONDITIONS:\n");
        contract.append("1. The lessee agrees to use the locker solely for personal storage.\n");
        contract.append("2. No hazardous materials or illegal items are permitted.\n");
        contract.append("3. The lessee is responsible for the security of their belongings.\n");
        contract.append("4. The college is not liable for lost or stolen items.\n");
        contract.append("5. The locker must be vacated by the end date specified.\n");
        contract.append("6. Early termination may result in forfeiture of remaining rental fees.\n\n");
        
        contract.append("PAYMENT:\n");
        contract.append("Total Amount: $").append(booking.getTotalAmount()).append("\n");
        contract.append("Payment Status: ").append(booking.getStatus()).append("\n");
        contract.append("Payment Reference: ").append(booking.getPaymentReference()).append("\n\n");
        
        contract.append("SIGNATURES:\n");
        contract.append("Lessee: ").append(booking.getUserName()).append("\n");
        contract.append("Date: ").append(booking.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");
        
        contract.append("Sheridan College\n");
        contract.append("Date: ").append(booking.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");
        
        contract.append("This agreement is binding and enforceable by law.\n");
        contract.append("========================================\n");
        
        return contract.toString();
    }
}
