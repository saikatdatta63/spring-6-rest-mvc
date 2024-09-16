package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.CustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private Map<UUID, CustomerDTO> customers;

    public CustomerServiceImpl() {
        customers=new HashMap<>();

        CustomerDTO customer1 = CustomerDTO.builder()
                .id(UUID.randomUUID())
                .customerName("Saikat Datta")
                .version(1)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        CustomerDTO customer2 = CustomerDTO.builder()
                .id(UUID.randomUUID())
                .customerName("Sayantan")
                .version(1)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        CustomerDTO customer3 = CustomerDTO.builder()
                .id(UUID.randomUUID())
                .customerName("Shuvam")
                .version(1)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        customers.put(customer1.getId(), customer1);
        customers.put(customer2.getId(), customer2);
        customers.put(customer3.getId(), customer3);
    }
    @Override
    public List<CustomerDTO> listCustomers() {
        return new ArrayList<>(customers.values());
    }

    public Optional<CustomerDTO> getCustomerById(UUID id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public CustomerDTO createCustomer(CustomerDTO customer) {
        CustomerDTO savedCustomer = CustomerDTO.builder()
                .id(UUID.randomUUID())
                .customerName(customer.getCustomerName())
                .version(customer.getVersion())
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        customers.put(savedCustomer.getId(), savedCustomer);
        return savedCustomer;
    }

    @Override
    public void updateCustomer(UUID id, CustomerDTO customer) {
        CustomerDTO existing = customers.get(id);
        if(null != existing) {
            existing.setCustomerName(customer.getCustomerName());
            existing.setVersion(customer.getVersion());
            existing.setLastModifiedDate(LocalDateTime.now());
        }
        else {
            log.info("Customer with id {} not found! Creating new customer", id);
            CustomerDTO createdCustomer = createCustomer(customer);
            log.info("Customer with id {} created", createdCustomer.getId());
        }
    }

    @Override
    public void deleteCustomer(UUID id) {
        customers.remove(id);
    }

    @Override
    public void patchCustomer(UUID id, CustomerDTO customer) {

        CustomerDTO existing = customers.get(id);

        if(StringUtils.hasText(customer.getCustomerName())) {
            existing.setCustomerName(customer.getCustomerName());
        }
        if(null!= customer.getVersion()) {
            existing.setVersion(customer.getVersion());
        }

        existing.setLastModifiedDate(LocalDateTime.now());
    }
}
