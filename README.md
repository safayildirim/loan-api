# Loan API Documentation

## Overview

The Loan API provides endpoints for managing customer loans, including creating loans, paying installments, and viewing
loan and installment details. The API implements role-based access control to differentiate between ADMIN users and
CUSTOMER users.

## Features

* Create Customers: Creates a new customer account with the provided details.
* Create Loans: Customers can create new loans with specific amounts, rates, and installments.
* View Loans: Retrieve loan details for a specific customer.
* View Installments: Retrieve installment details for a specific customer.
* Pay Installments: Process payments for loan installments, applying discounts for early payments and penalties
  for late
  payments.
* Role-Based Access:
    - ADMIN: Can manage loans for all customers.
    - CUSTOMER: Can manage loans for their own account.

## Authorization

All endpoints enforces role-based access control:

- **ADMIN** users can manage loans for all customers.
- **CUSTOMER** users can manage loans for their own account.

Authentication and authorization are implemented using Spring Security.

## Endpoints

### Create a New Customer

POST  ```/customers```

#### Description:

Creates a new customer account with the provided details.

#### Request

#### Request Body:

The request body should contain a JSON object with the following fields:

| Parameter   | Type       | Description                                 |
|:------------|------------|:--------------------------------------------|
| name        | **String** | The first name of the customer.             |
| surname     | **String** | The surname of the customer.                |
| username    | **String** | A unique username for the customer.         |
| password    | **String** | A secure password for the customer.         |
| role        | **String** | The role of the customer. [CUSTOMER, ADMIN] |
| creditLimit | **Double** | The initial credit limit for the customer.  |

#### Headers

- Authorization: Required. Use Basic Authentication with valid credentials.

#### Responses:

| Status          | Description                                                |
|:----------------|:-----------------------------------------------------------|
| 200 OK          | Returns the details of the newly created customer.         |
| 400 Bad Request | Bad Request. Validation errors occurred in the input data. |

### Create Loan

POST  ```/customers/{customer_id}/loans```

#### Description:

Creates a loan for a specific customer.

#### Request

#### Path Parameters:

| Parameter   | Type     | Description                                 |
|:------------|----------|:--------------------------------------------|
| customer_id | **Long** | The ID of the customer requesting the loan. |

#### Request Body:

The request body should contain a JSON object with the following fields:

| Parameter            | Type        | Description                                           |
|:---------------------|-------------|:------------------------------------------------------|
| amount               | **Double**  | The requested loan amount.                            |
| rate                 | **Double**  | The interest rate for the loan. [0.1-0.5]             |
| numberOfInstallments | **Integer** | The number of installments for repayment. [6,9,12,24] |

#### Headers

- Authorization: Required. Use Basic Authentication with valid credentials.

#### Responses:

| Status          | Description                                                |
|:----------------|:-----------------------------------------------------------|
| 200 OK          | Returns the created loan details.                          |
| 400 Bad Request | Bad Request. Validation errors occurred in the input data. |
| 403 Forbidden   | Access denied if the user is not authorized.               |
| 404 Not Found   | Customer not found.                                        |

### List Installments

GET ```/customers/{customer_id}/loans/{loan_id}/installments```

#### Description:

Fetches a list of all installments for the loan specified by its ID.

#### Request

#### Path Parameters:

| Parameter   | Type     | Description                                              |
|:------------|----------|:---------------------------------------------------------|
| customer_id | **Long** | ID of the customer.                                      |
| loan_id     | **Long** | The ID of the loan for which installments are retrieved. |

#### Headers

- Authorization: Required. Use Basic Authentication with valid credentials.

#### Responses:

| Status        | Description                                  |
|:--------------|:---------------------------------------------|
| 200 OK        | Returns a list of installments.              |
| 403 Forbidden | Access denied if the user is not authorized. |

### List Loans

GET ```/customers/{customer_id}/loans```

#### Description:

Retrieves a list of loans for a specific customer.

#### Request

#### Path Parameters:

| Parameter   | Type     |     Description     |
|:------------|----------|:-------------------:|
| customer_id | **Long** | ID of the customer. |

#### Headers

- Authorization: Required. Use Basic Authentication with valid credentials.

#### Responses:

| Status        | Description                                  |
|:--------------|:---------------------------------------------|
| 200 OK        | Returns a list of loans.                     |
| 403 Forbidden | Access denied if the user is not authorized. |

### Pay Installment

POST ```/customers/{customer_id}/loans/{loan_id}/payment```

#### Description:

Allows a customer to pay one or more unpaid installments of a loan. The payment amount is applied to installments in
due-date order.

#### Request

#### Path Parameters:

| Parameter   | Type     |          Description           |
|:------------|----------|:------------------------------:|
| customer_id | **Long** |      ID of the customer.       |
| loan_id     | **Long** | The ID of the loan to be paid. |

#### Request Body:

The request body should contain a JSON object with the following fields:

| Parameter | Type       | Description                     |
|:----------|------------|:--------------------------------|
| amount    | **Double** | The amount of money to be paid. |

#### Headers

- Authorization: Required. Use Basic Authentication with valid credentials.

#### Responses:

| Status          | Description                                                                                                                        |
|:----------------|:-----------------------------------------------------------------------------------------------------------------------------------|
| 200 OK          | Returns a summary of the payment details, including the installments paid, the remaining installments, and the total amount spent. |
| 400 Bad Request | Bad Request. Validation errors occurred in the input data.                                                                         |
| 403 Forbidden   | Access denied if the user is not authorized.                                                                                       |
| 404 Not Found   | Loan or customer not found.                                                                                                        |

## Prerequisites

- Java 11+
- Maven
- Spring Boot

## Running the Application

1. Clone the repository.
2. Build the project:
    ```bash
    mvn clean install
    ```
3. Run the application:
    ```bash
    mvn spring-boot:run
    ```

## Testing

Run the unit tests:

```bash
mvn test
```