package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.CustomerDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {

    List<CustomerDTO> listCustomers();

    Optional<CustomerDTO> getCustomerById(UUID id);

    CustomerDTO createCustomer(CustomerDTO customer);

    void updateCustomer(UUID id, CustomerDTO customer);

    void deleteCustomer(UUID id);

    void patchCustomer(UUID id, CustomerDTO customer);
}
