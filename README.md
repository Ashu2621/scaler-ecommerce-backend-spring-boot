# Scaler E-Commerce Backend (Spring Boot)

## Project Overview
This project is a backend service for an e-commerce platform developed using Spring Boot.  
It provides RESTful APIs to manage products, categories, and payments following clean backend architecture.

The project demonstrates:
- REST API design using Spring Boot
- Layered architecture (Controller, Service, Repository)
- Integration with external APIs
- Payment gateway abstraction
- Global exception handling
- Use of DTOs for clean API contracts

---

## Tech Stack
- Java 17  
- Spring Boot  
- Spring Web (REST APIs)  
- Spring Data JPA  
- Hibernate  
- MySQL  
- Stripe Payment Gateway  
- FakeStore API  
- Maven  

---

## Project Structure
Controller
Service
Repository
Model
DTO
Exception
Config

yaml
Copy code

---

## API Endpoints

### Product APIs
- GET /products – Get all products  
- GET /products/{id} – Get product by ID  
- POST /products – Create product  
- PUT /products/{id} – Update product  
- DELETE /products/{id} – Delete product  

### Category APIs
- GET /categories – Get all categories  
- GET /categories/{id} – Get category by ID  
- POST /categories – Create category  
- DELETE /categories/{id} – Delete category  

### Payment API
- POST /payment – Initiate payment  

---

## How to Run the Project

### Prerequisites
- Java 17+
- Maven
- MySQL
- IntelliJ IDEA / VS Code

### Steps
1. Clone the repository
git clone https://github.com/Ashu2621/scaler-ecommerce-backend-spring-boot.git

2. Navigate to project folder
cd scaler-ecommerce-backend-spring-boot


3. Build the project
mvn clean install


4. Run the application
mvn spring-boot:run


OR run `BackendProjectApplication.java` from IDE.

---

## Author
Ashutosh Sharma  
Backend Developer | Java | Spring Boot | REST APIs | DSA  

GitHub: https://github.com/Ashu2621  
LeetCode: https://leetcode.com/u/as5768202/
