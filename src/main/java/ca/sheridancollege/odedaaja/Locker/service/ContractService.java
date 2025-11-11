package ca.sheridancollege.odedaaja.Locker.service;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;

public interface ContractService {
    String generateContract(Booking booking);
    String generateContractContent(Booking booking);
}