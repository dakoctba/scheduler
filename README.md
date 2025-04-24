# Scheduler - Sistema de Agendamento

## 📋 Descrição
Scheduler é um sistema de agendamento desenvolvido com Spring Boot, oferecendo uma API RESTful para gerenciamento de agendamentos e tarefas.

## 🚀 Tecnologias Utilizadas
- Java 17
- Spring Boot 3.4.4
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT para autenticação
- OpenAPI (Swagger) para documentação
- Maven para gerenciamento de dependências

## 🛠️ Pré-requisitos
- Java 17 ou superior
- Maven
- PostgreSQL
- Docker (opcional)

## 🔧 Configuração do Ambiente

### 1. Clone o repositório
```bash
git clone [URL_DO_REPOSITÓRIO]
cd scheduler
```

### 2. Configuração do Banco de Dados
O projeto utiliza PostgreSQL como banco de dados. Você pode configurar as credenciais no arquivo `application.properties` ou usar o Docker Compose fornecido.

### 3. Usando Docker Compose
```bash
docker-compose up -d
```

### 4. Compilando o Projeto
```bash
mvn clean install
```

### 5. Executando a Aplicação
```bash
mvn spring-boot:run
```

## 📚 Documentação da API
A documentação da API está disponível através do Swagger UI. Após iniciar a aplicação, acesse:
```
http://localhost:8080/swagger-ui.html
```

## 🔐 Segurança
O sistema utiliza Spring Security com JWT para autenticação. As rotas protegidas requerem um token JWT válido no header da requisição.

## 🧪 Testes
Para executar os testes:
```bash
mvn test
```

## 📦 Estrutura do Projeto
```
src/
├── main/
│   ├── java/
│   │   └── com/jacto/scheduler/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── service/
│   │       └── SchedulerApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/jacto/scheduler/
```

## 🤝 Contribuindo
1. Faça um Fork do projeto
2. Crie uma Branch para sua Feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a Branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request
