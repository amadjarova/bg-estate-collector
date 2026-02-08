# BG Estate Collector

A Spring Boot application designed to scrape Sofia real estate listings and export them
into timestamped CSV files. This project is built on **Java 25**
for high-performance scraping.

## 🛠 Tech Stack

* **Framework:** Spring Boot 4.0.1
* **Language:** Java 25
* **Scraping:** Jsoup 1.22.1
* **CSV Processing:** Apache Commons CSV 1.14.1
* **Tooling:** Lombok 1.18.42, Maven

## 🚀 Setup & Installation

### Prerequisites
* **JDK 25** installed.
* **Maven 3.8+** installed.

### Build the Project
Compile and package the application using Maven.
```bash
mvn clean install
```

## 📖 API Reference

### Export Estates
Triggers the scraping process for real estate listings and exports the data to a local CSV file.

**Endpoint:** `GET /estates/export-csv`

| Parameter | Type | Required | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `pages` | `Integer` | No | `2` | The number of pagination pages to scrape. |

**Response:**
* **200 OK:** Returns the name of the generated file (e.g., `estates_raw_data.csv`).
* **500 Internal Server Error:** If the scraping or file writing fails.

---

## 💻 Usage Examples

You can trigger the export using a web browser, `curl`, or any HTTP client.

### 1. Default Export
Scrapes the default number of pages (2) and saves the file.

**Request:**
```http
POST http://localhost:8080/estates/export-csv


