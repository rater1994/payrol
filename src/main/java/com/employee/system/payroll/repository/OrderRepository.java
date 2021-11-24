package com.employee.system.payroll.repository;

import com.employee.system.payroll.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
