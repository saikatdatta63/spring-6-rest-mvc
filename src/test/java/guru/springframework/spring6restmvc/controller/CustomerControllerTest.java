package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.Customer;
import guru.springframework.spring6restmvc.services.CustomerService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    ArgumentCaptor<UUID> uuidCaptor;

    @Captor
    ArgumentCaptor<Customer> customerCaptor;

    @Test
    void listCustomers() throws Exception {
        Customer cust1 = Customer.builder()
                .customerName("John Doe")
                .build();
        Customer cust2 = Customer.builder()
                .customerName("Saikat")
                .build();
        given(customerService.listCustomers()).willReturn(List.of(cust1, cust2));
        mockMvc.perform(get("/api/v1/customer").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(2)));
    }

    @Test
    void getCustomerById() throws Exception {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(id)
                .customerName("Test")
                .build();
        given(customerService.getCustomerById(id)).willReturn(customer);
        mockMvc.perform(get("/api/v1/customer/" + id).accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.customerName", is("Test")));
    }

    @Test
    void createCustomer() throws Exception {
        Customer cust = Customer.builder()
                .customerName("Test")
                .version(1)
                .build();

        given(customerService.createCustomer(cust)).willReturn(cust);
        mockMvc.perform(post("/api/v1/customer")
                        .content(objectMapper.writeValueAsString(cust))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @SneakyThrows
    @Test
    void updateCustomer() {
        UUID id = UUID.randomUUID();
        Customer cust = Customer.builder()
                .customerName("Updated")
                .version(1)
                .build();

        mockMvc.perform(put("/api/v1/customer/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cust)))
                .andExpect(status().isNoContent());
        verify(customerService).updateCustomer(uuidCaptor.capture(), any(Customer.class));
        assertThat(uuidCaptor.getValue()).isEqualTo(id);
    }

    @Test
    void deleteCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/customer/" + id)).andExpect(status().isNoContent());
        verify(customerService).deleteCustomer(uuidCaptor.capture());
        assertThat(uuidCaptor.getValue()).isEqualTo(id);
    }

    @Test
    void patchCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        Map<String, Object> custMap = new HashMap<>();
        custMap.put("customerName", "New name");

        mockMvc.perform(patch("/api/v1/customer/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(custMap)))
                .andExpect(status().isNoContent());
        verify(customerService).patchCustomer(uuidCaptor.capture(), customerCaptor.capture());
        assertThat(uuidCaptor.getValue()).isEqualTo(id);
        assertThat(customerCaptor.getValue().getCustomerName()).isEqualTo(custMap.get("customerName"));
    }
}