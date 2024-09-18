package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.exception.NotFoundException;
import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class CustomerServiceJPA implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerDTO> listCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::customerToCustomerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CustomerDTO> getCustomerById(UUID id) {
       return customerRepository.findById(id).map(customerMapper::customerToCustomerDTO);
    }

    @Override
    public CustomerDTO createCustomer(CustomerDTO customer) {
        Customer savedCust = customerRepository.save(customerMapper.customerDtoToCustomer(customer));
        return customerMapper.customerToCustomerDTO(savedCust);
    }

    @Override
    public void updateCustomer(UUID id, CustomerDTO customer) {
        customerRepository.findById(id).ifPresentOrElse(foundCust -> {
            foundCust.setCustomerName(customer.getCustomerName());
            foundCust.setLastModifiedDate(LocalDateTime.now());
            customerRepository.save(foundCust);
        }, () -> {
            throw new NotFoundException("Customer not found");
        });
    }

    @Override
    public void deleteCustomer(UUID id) {
        customerRepository.findById(id).ifPresentOrElse(customerRepository::delete, () -> {
            throw new NotFoundException("Customer not found");
        });
    }

    @Override
    public void patchCustomer(UUID id, CustomerDTO customer) {
        customerRepository.findById(id).ifPresentOrElse(foundCust -> {
            if(StringUtils.hasText(customer.getCustomerName())) {
                foundCust.setCustomerName(customer.getCustomerName());
            }
            foundCust.setLastModifiedDate(LocalDateTime.now());
            customerRepository.save(foundCust);
        }, () -> {
            throw new NotFoundException("Customer not found");
        });
    }
}
