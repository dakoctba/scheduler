# Sistema de Agendamento de Visitas Técnicas

Sistema desenvolvido para gerenciar agendamentos de visitas técnicas, permitindo o controle de técnicos, clientes, equipamentos e peças de reposição.

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- PostgreSQL
- Kafka
- Docker
- Maven
- JWT para autenticação
- OpenAPI (Swagger) para documentação

## Funcionalidades

### Autenticação e Autorização
- Login com JWT
- Roles: ADMIN, TECNICO
- Endpoints protegidos por role

### Agendamentos
- CRUD completo de agendamentos
- Associação com técnicos
- Controle de status (PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED)
- Prioridades (LOW, MEDIUM, HIGH)
- Geolocalização (latitude/longitude)
- Feedback do cliente (rating e comentários)
- Equipamentos associados
- Peças de reposição necessárias
- Email do cliente para notificações

### Técnicos
- CRUD de técnicos
- Métricas de desempenho
- Histórico de agendamentos
- Avaliações recebidas

### Notificações
- Sistema de notificações via Kafka
- Notificações de criação de agendamento
- Notificações de atualização de status
- Notificações de cancelamento
- Lembretes automáticos de agendamentos

### Emails
- Envio de emails de confirmação
- Notificações de atualização de status
- Lembretes de agendamentos
- Template personalizado para emails

### Relatórios
- Métricas de desempenho dos técnicos
- Tempo médio de conclusão
- Avaliações recebidas
- Agendamentos por período

## Configuração do Ambiente

### Pré-requisitos
- Java 17
- Maven
- Docker e Docker Compose
- MailCatcher (para testes de email)

### Configuração do Banco de Dados
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/scheduler
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Configuração do Kafka
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=scheduler-group
```

### Configuração do Email (MailCatcher)
```properties
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

## Executando o Projeto

1. Clone o repositório
2. Configure as variáveis de ambiente
3. Execute o Docker Compose:
```bash
docker-compose up -d
```
4. Execute o projeto:
```bash
./mvnw spring-boot:run
```

## Endpoints da API

### Autenticação
- POST /api/auth/login - Login de usuário
- POST /api/auth/refresh - Renovação do token

### Agendamentos
- GET /api/schedulings - Lista todos os agendamentos
- GET /api/schedulings/{id} - Obtém um agendamento específico
- POST /api/schedulings - Cria um novo agendamento
- PUT /api/schedulings/{id} - Atualiza um agendamento
- DELETE /api/schedulings/{id} - Remove um agendamento
- GET /api/schedulings/upcoming - Lista agendamentos futuros
- POST /api/schedulings/{id}/feedback - Adiciona feedback do cliente

### Técnicos
- GET /api/technicians - Lista todos os técnicos
- GET /api/technicians/{id} - Obtém um técnico específico
- POST /api/technicians - Cria um novo técnico
- PUT /api/technicians/{id} - Atualiza um técnico
- DELETE /api/technicians/{id} - Remove um técnico
- GET /api/technicians/{id}/performance - Obtém métricas de desempenho

## Documentação da API

A documentação da API está disponível através do Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

## Testes

O projeto inclui testes unitários e de integração. Para executar os testes:
```bash
./mvnw test
```

## Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
