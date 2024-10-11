# Distributed URL Shortener Using Snowflake IDs

## Objective
Create a URL shortening service using Snowflake IDs with base conversion to produce 7-8 character identifiers. The system should have at least two web servers behind a load balancer to ensure scalability and high availability.

## Key Requirements

1. **Snowflake ID Generation**
   - Generate unique Snowflake IDs and convert them to a higher base (e.g., Base62) for 7-8 character shortened URLs.

2. **URL Shortening**
   - Users can submit long URLs, which are shortened using the Snowflake ID. When accessing the short URL, the system retrieves and redirects to the original URL.

3. **Load Balancer**
   - Set up a load balancer to distribute traffic across two or more web servers, ensuring the system continues functioning if one server goes down.

4. **Web Servers**
   - Deploy at least two web servers that independently generate Snowflake IDs and handle URL shortening and redirection requests.

5. **Database**
   - Store the mappings of shortened URLs to original URLs in a shared database (e.g., PostgreSQL), accessible by all web servers.

## Technologies Used
- Java / Spring Boot
- PostgreSQL
- Load Balancer (e.g., NGINX)
- Snowflake ID Generator

## Getting Started
1. Clone the repository.
2. Set up the PostgreSQL database and configure the connection.
3. Deploy web servers.
4. Configure the load balancer.
5. Start the application and test the URL shortening functionality.

## Contribution
Feel free to contribute by creating issues or submitting pull requests!
