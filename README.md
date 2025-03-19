# JWTAUTH

A Spring Boot application using PostgreSQL with a secure two-token authentication system.

## Technologies Used
- **Spring Boot 3.4.3** - Backend framework
- **PostgreSQL** - Database
- **Spring Security** - Authentication & Authorization
- **JWT (JSON Web Token)** - Access & Refresh token system
- **Java 23** - Programming language

## Security Implementation
This project employs a **two-token system** for authentication:

1. **Access Token** - Short-lived token for authentication.
2. **Refresh Token** - Longer-lived token used to obtain new access tokens.

### Token Storage & Security Measures
- **HttpOnly Cookies**: Tokens are stored in HttpOnly cookies to prevent access from JavaScript.
- **Secure Flag**: Cookies are only transmitted over HTTPS (off by default).
- **SameSite=Strict**: Prevents CSRF by restricting cookie transmission to same-origin requests.
- **Token Expiry**: Access tokens have a short expiration time, while refresh tokens last longer.

Tokens in this project are fully stateless and not stored on the backend.

## API Endpoints
- `POST /login` - Authenticate and receive tokens.
- `GET /refresh` - Obtain a new access token using the refresh token.
- `POST /logout` - Invalidate tokens and clear cookies.

## Notes
- Ensure you are running the application over HTTPS in production.
- Environment variables should be used to store database credentials.
- The secure flag for cookies is off by default.

## License
This project is licensed under [MIT License](LICENSE).

