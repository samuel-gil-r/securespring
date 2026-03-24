# Taller de Arquitectura Segura — SecureSpring
# Samuel Antonio Gil Romero 

Aplicación web segura desplegada en AWS con dos servidores: Apache (frontend) y Spring Boot (backend), ambos con HTTPS.


## Arquitectura

```
Browser
  │
  │ HTTPS (Let's Encrypt)
  ▼
Apache Server (EC2)
  └── Sirve index.html (HTML + JS asíncrono)
        │
        │ HTTPS (certificado autofirmado keytool)
        ▼
      Appserver (EC2)
        └── Spring Boot :5000
              ├── POST /register  → guarda usuario con BCrypt
              ├── POST /login     → valida credenciales
              └── GET  /          → hello world
```

## Infraestructura AWS

| Instancia | Tipo | Rol |
|-----------|------|-----|
| Apache | t3.micro | Servidor web (frontend + TLS Let's Encrypt) |
| Appserver | t3.micro | Backend Spring Boot (TLS keytool) |

## Tecnologías

- **Java 21** + **Spring Boot 3.4.3**
- **Spring Security** con BCryptPasswordEncoder
- **H2** base de datos en memoria
- **Apache HTTP Server** + **Certbot / Let's Encrypt**
- **keytool** para generar el certificado del Appserver
- **AWS EC2** (Amazon Linux 2023)

## Seguridad implementada

### TLS en Apache (Let's Encrypt)
El servidor Apache usa un certificado emitido por Let's Encrypt para el dominio `tdsesamuelgil.duckdns.org`. Certbot gestiona la instalación y renovación automática.

```bash
sudo certbot --apache -d tdsesamuelgil.duckdns.org
```

### TLS en Spring Boot (keytool)
El Appserver usa un certificado autofirmado generado con keytool en formato PKCS12.

```bash
keytool -genkeypair -alias ecikeypair -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore ecikeystore.p12 -validity 3650
```

Configuración en `application.properties`:
```properties
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:keystore/ecikeystore.p12
server.ssl.key-store-password=123456
server.ssl.key-alias=ecikeypair
server.ssl.enabled=true
server.port=5000
```

### Passwords hasheados (BCrypt)
Las contraseñas nunca se almacenan en texto plano. Spring Security aplica BCrypt al registrar cada usuario:

```java
user.setPassword(passwordEncoder.encode(user.getPassword()));
```

## Endpoints del backend

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| POST | `/register` | Registra nuevo usuario | No |
| POST | `/login` | Autentica usuario | No |
| GET | `/` | Hello world | No |

### Ejemplo de uso

Registrar usuario:
```bash
curl -k -X POST https://18.206.185.198:5000/register \
  -H "Content-Type: application/json" \
  -d '{"username":"samuel","password":"1234"}'
```

Login:
```bash
curl -k -X POST https://18.206.185.198:5000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"samuel","password":"1234"}'
```

## Despliegue

### 1. Compilar el proyecto
```bash
mvn clean package -DskipTests
```

### 2. Subir el jar al Appserver
```bash
scp -i "Tdse.pem" target/securespring-1.0-SNAPSHOT.jar \
  ec2-user@<APPSERVER_IP>:/home/ec2-user/
```

### 3. Ejecutar en el Appserver
```bash
java -jar ~/securespring-1.0-SNAPSHOT.jar
```

> **Nota:** Para dejar el proceso corriendo en background usar:
> ```bash
> nohup java -jar ~/securespring-1.0-SNAPSHOT.jar > app.log 2>&1 &
> ```

### 4. Subir el frontend a Apache
```bash
scp -i "Tdse.pem" index.html ec2-user@<APACHE_IP>:/home/ec2-user/
ssh -i "Tdse.pem" ec2-user@<APACHE_IP>
sudo cp /home/ec2-user/index.html /var/www/html/index.html
```

## Nota sobre Mixed Content

El browser bloquea llamadas HTTP desde una página HTTPS. Por eso tanto Apache como Spring Boot deben servir sobre HTTPS. Al usar un certificado autofirmado en el Appserver, el usuario debe aceptar el warning de seguridad en `https://<APPSERVER_IP>:5000` antes de usar la app.

## Evidencias

### Frontend sobre HTTPS (Apache + Let's Encrypt)  

<img width="1919" height="458" alt="image" src="https://github.com/user-attachments/assets/06279e14-3ace-4b80-8cce-a74a7f8923c3" />


### Registro de usuario

<img width="1569" height="486" alt="image" src="https://github.com/user-attachments/assets/564f7504-78e6-4d98-9639-3a21f9b19961" />


### Login exitoso

<img width="1569" height="486" alt="image" src="https://github.com/user-attachments/assets/a1e33260-15e6-4a9b-b5b8-50f8786dc4ff" />


### Spring Boot corriendo con HTTPS en el Appserver

<img width="947" height="137" alt="image" src="https://github.com/user-attachments/assets/3c06b910-d6d9-4e09-a348-6b5ad5c24708" />


### Appserver respondiendo desde el browser

<img width="475" height="174" alt="image" src="https://github.com/user-attachments/assets/1eeef210-a45a-42a0-847d-76021b815b62" />

---
## Video

https://youtu.be/_ZDVknc1_O0

## Estructura del proyecto

```
securespring/
├── src/main/java/co/edu/escuelaing/securespring/
│   ├── AuthController.java      # Endpoints /register y /login
│   ├── HelloController.java     # Endpoint GET /
│   ├── SecurityConfig.java      # Configuración Spring Security + BCrypt
│   ├── User.java                # Entidad usuario
│   ├── UserRepository.java      # Repositorio JPA
│   └── Securespring.java        # Main
├── src/main/resources/
│   ├── keystore/
│   │   └── ecikeystore.p12      # Certificado TLS autofirmado
│   └── application.properties
└── pom.xml
```
