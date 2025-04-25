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
- Apache POI para geração de relatórios

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
- Notificações de feedback do cliente
- Notificações de peças de reposição necessárias
- Notificações de SLA (Service Level Agreement)

### Processo Automático
- Execução a cada hora para verificar agendamentos
- Envio de lembretes 24h antes do agendamento
- Envio de lembretes 1h antes do agendamento
- Verificação de SLA não atendidos
- Atualização automática de status
- Geração de relatórios periódicos
- Notificações de agendamentos atrasados

### Emails
- Envio de emails de confirmação
- Notificações de atualização de status
- Lembretes de agendamentos
- Template personalizado para emails
- Notificações de feedback
- Alertas de SLA
- Relatórios periódicos

### Relatórios
O sistema oferece diversos relatórios em formato Excel (XLSX) que podem ser gerados através dos seguintes endpoints:

#### Relatório de Visitas
- Endpoint: `GET /api/reports/visits`
- Detalhes de todas as visitas em um período
- Informações incluídas:
  - ID da visita
  - Cliente
  - Técnico
  - Data agendada
  - Data de conclusão
  - Status
  - Equipamentos
  - Descrição do problema
  - Solução
  - Duração
  - Feedback do cliente

#### Relatório de Desempenho dos Técnicos
- Endpoint: `GET /api/reports/technicians/performance`
- Métricas de desempenho por técnico
- Informações incluídas:
  - Total de visitas
  - Visitas concluídas
  - Visitas pendentes
  - Tempo médio de atendimento
  - Satisfação média
  - Taxa de conclusão

#### Relatório de Análise de Clientes
- Endpoint: `GET /api/reports/customers/analysis`
- Métricas por cliente
- Informações incluídas:
  - Total de visitas
  - Visitas concluídas
  - Visitas pendentes
  - Tempo médio entre visitas
  - Satisfação média
  - Equipamentos atendidos

#### Relatório de Manutenção de Equipamentos
- Endpoint: `GET /api/reports/equipment/maintenance`
- Métricas por equipamento
- Informações incluídas:
  - Total de visitas
  - Visitas concluídas
  - Visitas pendentes
  - Tempo médio entre manutenções
  - Problemas mais frequentes
  - Status atual

#### Relatório de Conformidade SLA
- Endpoint: `GET /api/reports/sla/compliance`
- Análise de conformidade com SLA
- Informações incluídas:
  - Tempo de resposta
  - Tempo de resolução
  - Conformidade com SLA
  - Prioridade
  - Status
- SLAs configurados:
  - Alta Prioridade: 2 horas
  - Média Prioridade: 4 horas
  - Baixa Prioridade: 8 horas

Todos os relatórios aceitam os parâmetros:
- `startDate`: Data inicial (formato ISO 8601)
- `endDate`: Data final (formato ISO 8601)

Exemplo de uso:
```bash
curl -X GET "http://localhost:8080/api/reports/visits?startDate=2025-04-01T00:00:00&endDate=2025-05-31T23:59:59" \
-H "Authorization: Bearer seu_token_jwt" \
--output visits-report.xlsx
```

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

### Relatórios
- GET /api/reports/visits - Relatório de visitas
- GET /api/reports/technicians/performance - Relatório de desempenho dos técnicos
- GET /api/reports/customers/analysis - Relatório de análise de clientes
- GET /api/reports/equipment/maintenance - Relatório de manutenção de equipamentos
- GET /api/reports/sla/compliance - Relatório de conformidade SLA

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

## Conventional Commits

[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-yellow.svg)](https://conventionalcommits.org)

Para contribuir com o projeto, por favor siga o padrão de [conventional commits](https://medium.com/gamersclub-tech/automatizando-o-changelog-do-produto-com-conventional-commits-5f57e1b2182f).

- feature: Uma nova funcionalidade
- bugfix: A correção de um bug
- chore: Mudanças fora de /src ou /test, por exemplo
- docs: Mudanças apenas em documentação
- style: Mudanças que não afetam o significado do código (espaços em branco, formatação, etc.)
- refactor: Uma refatoração de código
- perf: Um código que melhora a performance da aplicação
- test: Adição de novos testes ou correção de testes existentes

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
