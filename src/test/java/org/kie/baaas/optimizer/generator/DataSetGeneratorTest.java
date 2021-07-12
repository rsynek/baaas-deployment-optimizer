package org.kie.baaas.optimizer.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.baaas.optimizer.domain.Customer;
import org.kie.baaas.optimizer.domain.Service;

class DataSetGeneratorTest {

    @Test
    void mapServicesToCustomers_tooFewServices() {
        DataSetGenerator dataSetGenerator = new DataSetGenerator();

        Customer normalCustomer = new Customer(1L, false);
        Customer exclusiveCustomer = new Customer(2L, true);
        List<Customer> customers = Arrays.asList(normalCustomer, exclusiveCustomer);
        List<Service> services = Arrays.asList(new Service(), new Service());
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> dataSetGenerator.mapServicesToCustomers(services, customers))
                .withMessageContaining("There are not enough services");
    }

    @Test
    void mapServicesToCustomers() {
        DataSetGenerator dataSetGenerator = new DataSetGenerator();

        Customer normalCustomer = new Customer(1L, false);
        Customer exclusiveCustomer = new Customer(2L, true);
        List<Customer> customers = Arrays.asList(normalCustomer, exclusiveCustomer);
        List<Service> services = IntStream.range(0, 30).mapToObj(value -> new Service()).collect(Collectors.toList());
        dataSetGenerator.mapServicesToCustomers(services, customers);

        Map<Customer, List<Service>> servicesByCustomer = services.stream().collect(Collectors.groupingBy(Service::getCustomer));
        assertThat(servicesByCustomer).containsOnlyKeys(normalCustomer, exclusiveCustomer);
        List<Service> normalCustomerServices = servicesByCustomer.get(normalCustomer);
        List<Service> exclusiveCustomerServices = servicesByCustomer.get(exclusiveCustomer);
        assertThat(normalCustomerServices.size()).isBetween(2, 4);
        assertThat(exclusiveCustomerServices.size()).isBetween(20, 30);
    }
}
