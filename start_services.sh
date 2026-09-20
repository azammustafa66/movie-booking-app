#!/bin/bash
mvn spring-boot:run -pl discovery-service > logs_discovery.txt 2>&1 &
sleep 15
mvn spring-boot:run -pl user-service > logs_user.txt 2>&1 &
mvn spring-boot:run -pl catalog-service > logs_catalog.txt 2>&1 &
mvn spring-boot:run -pl booking-service > logs_booking.txt 2>&1 &
mvn spring-boot:run -pl notification-service > logs_notification.txt 2>&1 &
sleep 20
mvn spring-boot:run -pl api-gateway > logs_gateway.txt 2>&1 &
