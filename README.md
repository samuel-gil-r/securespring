# SecureSpring - Secure Web Application

**Materia:** TDSE - Taller de Arquitecturas de Software Empresarial  
**Autor:** Samuel Gil  


## Descripción
Aplicación web segura construida con Apache y Spring Boot, desplegada en AWS EC2 con cifrado TLS.

## Arquitectura
- **Servidor 1 (Apache):** Sirve un cliente HTML+JS asíncrono sobre HTTPS usando certificados Let's Encrypt.
- **Servidor 2 (Spring Boot):** API REST backend con HTTPS usando un keystore PKCS12 generado con keytool.

## Características de Seguridad
- TLS/HTTPS en Apache con certificado Let's Encrypt
- TLS/HTTPS en Spring Boot con certificado autofirmado (keytool)
- Spring Security con hashing de contraseñas BCrypt
- Configuración CORS

## Prerrequisitos
- Java 21
- Maven
- AWS EC2 (Amazon Linux 2023)
- Apache HTTPD
- Certbot (Let's Encrypt)

## Instalación y Despliegue

### 1. Clonar el repositorio
```bash
git clone https://github.com/samuel-gil-r/securespring.git
cd securespring
```

### 2. Generar el keystore
```bash
keytool -genkeypair -alias ecikeypair -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore src/main/resources/keystore/ecikeystore.p12 \
  -validity 3650
```
> Nota: usar `localhost` como nombre del certificado cuando se solicite.

### 3. Compilar el proyecto
```bash
mvn clean package -DskipTests
```

### 4. Desplegar en EC2
```bash
scp -i "your-key.pem" target/securespring-1.0-SNAPSHOT.jar ec2-user@YOUR_EC2_IP:~
ssh -i "your-key.pem" ec2-user@YOUR_EC2_IP
java -jar securespring-1.0-SNAPSHOT.jar &
```

### 5. Configurar Apache
```bash
sudo dnf install httpd -y
sudo systemctl start httpd
sudo systemctl enable httpd
sudo certbot --apache -d yourdomain.duckdns.org
```

### 6. Desplegar el cliente
```bash
sudo nano /var/www/html/index.html
```

## Configuración
`src/main/resources/application.properties`:
```properties
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:keystore/ecikeystore.p12
server.ssl.key-store-password=123456
server.ssl.key-alias=ecikeypair
server.ssl.enabled=true
server.port=5000
```

## Pruebas

### Cliente Apache
Acceder a `https://tdsesamuelgil.duckdns.org`

### API Spring Boot
Acceder a `https://tdsesamuelgil.duckdns.org:5000/`

### Registro de usuario
```bash
curl -X POST https://tdsesamuelgil.duckdns.org:5000/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"1234"}'
```

### Login
```bash
curl -X POST https://tdsesamuelgil.duckdns.org:5000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"1234"}'
```

## Screenshots

### Certificado Apache HTTPS (Let's Encrypt)  

<img width="1233" height="709" alt="image" src="https://github.com/user-attachments/assets/af8c1800-7d97-4d06-b278-93f999d3ebbd" />

### Cliente funcionando - Call Spring API
<img width="510" height="201" alt="image" src="https://github.com/user-attachments/assets/ef4a2e23-19f7-44ba-be5e-092495e481bd" />

### Spring Boot ejecutándose en EC2

<img width="1093" height="729" alt="image" src="https://github.com/user-attachments/assets/e4a23769-2f4a-4a62-bae5-a7a7d9224ad1" />


