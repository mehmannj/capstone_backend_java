package ca.sheridancollege.odedaaja.Locker.service;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.domain.ContractTemplate;
import ca.sheridancollege.odedaaja.Locker.repo.ContractTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service("simpleContractService")
@RequiredArgsConstructor
public class SimpleContractServiceImpl implements ContractService {
    
    private final ContractTemplateRepository templateRepo;

    @Override
    public String generateContract(Booking booking) {
        // For now, return a mock contract URL
        // In a real implementation, this would generate a PDF contract
        return "/contracts/contract_" + booking.getId() + ".pdf";
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

