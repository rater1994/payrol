package com.employee.system.payroll.mocking;

import com.employee.system.payroll.model.Employee;
import com.employee.system.payroll.model.Order;
import com.employee.system.payroll.model.OrderStatus;
import com.employee.system.payroll.repository.EmployeeRepository;
import com.employee.system.payroll.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DatabaseMocking {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMocking.class);

    @Bean
    CommandLineRunner initDatabase(EmployeeRepository employeeRepository, OrderRepository orderRepository) {
        return args -> {
            LOGGER.info("Preloading" +  employeeRepository.save(new Employee("FirstName1", "LastName1", "engineer ")));
            LOGGER.info("Preloading" + employeeRepository.save(new Employee("FirstName2","LastName2", "architect ")));

            employeeRepository.findAll().forEach(employee -> LOGGER.info("Preloaded " + employee));

            orderRepository.save(new Order("MacBook Pro", OrderStatus.COMPLETED));
            orderRepository.save(new Order("iPhone", OrderStatus.IN_PROGRESS));


        };
    }
}
