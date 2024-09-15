package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.model.Customer;
import guru.springframework.spring6restmvc.services.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public List<Customer> listCustomers() {
        return customerService.listCustomers();
    }

    @GetMapping(value = "{custId}")
    public Customer getCustomerById(@PathVariable("custId") UUID id) {
        return customerService.getCustomerById(id);
    }

    @PostMapping
    public ResponseEntity addCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerService.createCustomer(customer);
       /* HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/customer/" + savedCustomer.getId().toString());
       */
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{custId}")
                .buildAndExpand(savedCustomer.getId())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("{customerId}")
    public ResponseEntity updateCustomer(@PathVariable("customerId") UUID customerId, @RequestBody Customer customer) {
        customerService.updateCustomer(customerId, customer);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{customerId}")
    public ResponseEntity deleteCustomer(@PathVariable("customerId") UUID customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("{customerId}")
    public ResponseEntity patchCustomer(@PathVariable("customerId") UUID customerId, @RequestBody Customer customer) {
        customerService.patchCustomer(customerId, customer);
        return ResponseEntity.noContent().build();
    }
}
