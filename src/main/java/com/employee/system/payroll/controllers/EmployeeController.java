package com.employee.system.payroll.controllers;

import com.employee.system.payroll.model.Employee;
import com.employee.system.payroll.repository.EmployeeRepository;
import com.employee.system.payroll.util.EmployeeModelAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeModelAssembler assembler;
    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeController.class);

    public EmployeeController(EmployeeRepository employeeRepository, EmployeeModelAssembler assembler) {
        this.employeeRepository = employeeRepository;
        this.assembler = assembler;
    }

    @GetMapping("/employees")
    public CollectionModel<EntityModel<Employee>> getAllEmployees() {

        List<EntityModel<Employee>> employees = employeeRepository.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(employees,
                linkTo(methodOn(EmployeeController.class).getAllEmployees()).withSelfRel());

    }

    @PostMapping("/employee")
    ResponseEntity<?> addNewEmployee(@RequestBody Employee employee) {
        EntityModel<Employee> entityModel = assembler.toModel(employee);
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }


    @GetMapping("/employee/{id}")
    public EntityModel<Employee> getOneEmployee(@PathVariable Long id) {

        Employee employee = employeeRepository.findById(id) //
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        return assembler.toModel(employee);
    }

    @PutMapping("/employee/{id}")
    ResponseEntity<?> updateEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {
        Employee updateEmployee = employeeRepository.findById(id)
                .map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return employeeRepository.save(employee);
                })
                .orElseGet(() -> {
                    newEmployee.setId(id);
                    return employeeRepository.save(newEmployee);
                });

        EntityModel<Employee> entityModel = assembler.toModel(updateEmployee);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @DeleteMapping("/employee/{id}")
    ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}