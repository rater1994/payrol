package com.employee.system.payroll.controllers;

import com.employee.system.payroll.model.Order;
import com.employee.system.payroll.model.OrderStatus;
import com.employee.system.payroll.repository.OrderRepository;
import com.employee.system.payroll.util.OrderModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderModelAssembler orderModelAssembler;

    public OrderController(OrderRepository orderRepository, OrderModelAssembler orderModelAssembler) {
        this.orderRepository = orderRepository;
        this.orderModelAssembler = orderModelAssembler;
    }

    @GetMapping("/orders")
    public CollectionModel<EntityModel<Order>> getAllOrders() {
        List<EntityModel<Order>> orders = orderRepository.findAll().stream()
                .map(orderModelAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(orders,
                linkTo(methodOn(OrderController.class).getAllOrders()).withSelfRel());
    }

    @GetMapping("/order/{id}")
    public EntityModel<Order> getOneOrder(@PathVariable Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return orderModelAssembler.toModel(order);
    }

    @PostMapping("/orders")
    ResponseEntity<EntityModel<Order>> addOrder(@RequestBody Order order) {
        order.setStatus(OrderStatus.IN_PROGRESS);
        Order newOrder = orderRepository.save(order);

        return ResponseEntity
                .created(linkTo(methodOn(OrderController.class).getOneOrder(order.getId())).toUri())
                .body(orderModelAssembler.toModel(newOrder));

    }

    @DeleteMapping("/order/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.IN_PROGRESS) {
            order.setStatus(OrderStatus.CANCELLED);
            return ResponseEntity.ok(orderModelAssembler.toModel(orderRepository.save(order)));
        }

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(Problem.create()
                        .withTitle("Method not allowed")
                        .withDetail("You can't cancel an order that is in the " + order.getStatus() + " status"));
    }

    @PutMapping("/orders/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id) {

        Order order = orderRepository.findById(id) //
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.IN_PROGRESS) {
            order.setStatus(OrderStatus.COMPLETED);
            return ResponseEntity.ok(orderModelAssembler.toModel(orderRepository.save(order)));
        }

        return ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't complete an order that is in the " + order.getStatus() + " status"));
    }

}
