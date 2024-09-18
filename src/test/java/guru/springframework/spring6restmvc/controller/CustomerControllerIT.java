package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.exception.NotFoundException;
import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class CustomerControllerIT {

    @Autowired
    private CustomerController customerController;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerMapper customerMapper;

    @Test
    void testListCustomers() {
        List<CustomerDTO> customerDTOS = customerController.listCustomers();
        assertThat(customerDTOS).size().isEqualTo(3);
    }

    @Test
    void testGetById() {
        Customer customer = customerRepository.findAll().getFirst();
        CustomerDTO customerDTO = customerController.getCustomerById(customer.getId());
        assertThat(customerDTO).isNotNull();
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(NotFoundException.class, () -> customerController.getCustomerById(UUID.randomUUID()));
    }

    @Test
    @Transactional
    @Rollback
    void testEmptyList() {
        customerRepository.deleteAll();
        List<CustomerDTO> customerDTOS = customerController.listCustomers();
        assertThat(customerDTOS).isEmpty();
    }

    @Transactional
    @Rollback
    @Test
    void testCreateCustomer() {
        CustomerDTO cust = CustomerDTO.builder().customerName("Saikat").createdDate(LocalDateTime.now()).build();
        ResponseEntity responseEntity = customerController.addCustomer(cust);
        URI location = responseEntity.getHeaders().getLocation();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(location).isNotNull();
        String[] locationUUID = location.getPath().split("/");
        UUID uuid = UUID.fromString(locationUUID[locationUUID.length - 1]);
        Optional<Customer> custOptional = customerRepository.findById(uuid);
        assertThat(custOptional).isPresent();
        assertThat(custOptional.get().getCustomerName()).isEqualTo(cust.getCustomerName());
    }

    @Transactional
    @Rollback
    @Test
    void testUpdateCustomer() {
        Customer customer = customerRepository.findAll().getFirst();
        CustomerDTO customerDTO = customerMapper.customerToCustomerDTO(customer);
        customerDTO.setId(null);
        customerDTO.setVersion(null);
        customerDTO.setCustomerName("Updated Name");
        ResponseEntity responseEntity = customerController.updateCustomer(customer.getId(), customerDTO);
        Customer updatedCustomer = customerRepository.findById(customer.getId()).get();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(updatedCustomer.getCustomerName()).isEqualTo(customerDTO.getCustomerName());
    }

    @Test
    void testUpdateCustomerNotFound() {
        assertThrows(NotFoundException.class, () -> customerController.updateCustomer(UUID.randomUUID(), CustomerDTO.builder().build()));
    }

    @Test
    @Transactional
    @Rollback
    void testDeleteCustomer() {
        UUID id = customerRepository.findAll().getFirst().getId();
        ResponseEntity responseEntity = customerController.deleteCustomer(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(customerRepository.findById(id)).isNotPresent();
    }

    @Test
    void testDeleteCustomerNotFound() {
        assertThrows(NotFoundException.class, () -> customerController.deleteCustomer(UUID.randomUUID()));
    }

    @Test
    @Transactional
    @Rollback
    void testPatchCustomer() {
        Customer customer = customerRepository.findAll().getFirst();
        CustomerDTO customerDTO = customerMapper.customerToCustomerDTO(customer);
        customerDTO.setCustomerName("Patched Name");
        ResponseEntity responseEntity =
                customerController.patchCustomer(customer.getId(), customerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        Customer patchedCustomer = customerRepository.findById(customer.getId()).get();
        assertThat(patchedCustomer.getCustomerName()).isEqualTo(customerDTO.getCustomerName());
    }

    @Test
    void testPatchNotFound() {
        assertThrows(NotFoundException.class,
                () -> customerController.patchCustomer(UUID.randomUUID(), CustomerDTO.builder().build()));
    }
}
