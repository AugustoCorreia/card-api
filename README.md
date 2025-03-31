# Card API - Sistema de Gerenciamento de Cartões

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1.0-green.svg)
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 📚 Índice

1. [Visão Geral](#-visão-geral)
2. [Tecnologias Principais](#-tecnologias-principais)
3. [Configuração Rápida](#-configuração-rápida)
4. [Testes](#-testes)
5. [Notas Adicionais](#-notas-adicionais)
6. [Como Funciona o Docker Compose](#️-como-funciona-o-docker-compose)
7. [Contribuição](#-contribuição)
8. [Licença](#-licença)

## 📌 Visão Geral

API robusta para gerenciamento seguro de cartões de crédito/débito com:

- 🔒 Autenticação JWT
- 📁 Processamento em lote de arquivos
- 🛡️ Criptografia de dados sensíveis
- 📊 Monitoramento de operações

## 🚀 Tecnologias Principais

| Tecnologia        | Finalidade                          |
|-------------------|-------------------------------------|
| Spring Boot 3.1.0 | Framework principal                 |
| Spring Security   | Autenticação e autorização          |
| JWT               | Tokens de autenticação stateless    |
| MySql             | Persistência de dados               |
| Lombok            | Redução de código boilerplate       |

## 🔧 Configuração Rápida

1. **Pré-requisitos**:
    - JDK 17
    - Maven 3.8+
    - Docker (instale a partir de [Docker](https://docs.docker.com/get-docker/))
    - Docker Compose (instale a partir de [Docker Compose](https://docs.docker.com/compose/install/))

2. **Clone o projeto**:
   ```bash
   git clone https://github.com/AugustoCorreia/card-api.git
   cd card-api
   ```

3. **Execute a aplicação**:
   ```bash
   mvn clean install
   docker-compose up --build
   ```

4. **Acessar a Aplicação**:

   - A aplicação estará rodando em [http://localhost:8081](http://localhost:8081).
   - A documentação da API estará disponível em [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html).

## 🧪 Testes

### Autenticação:
- **POST** `/api/auth/authenticate` - Autentica usuário e retorna token JWT

### Usuários
- **POST** `/api/users/register` - Registra novo usuário e retorna token JWT

### Cartões
- **POST** `/api/cards` - Cadastra novo cartão
- **GET** `/api/cards/user/{userId}` - Lista cartões do usuário
- **GET** `/api/cards/{cardId}` - Buscar cartão pelo ID 
- **POST** `/api/cards/upload` - Processa arquivo com múltiplos cartões
- **GET** `/api/cards/search?cardNumber={cardNumber}` - Busca cartão pelo número

## 📝 Notas Adicionais
- **Banco de dados:** A aplicação utiliza o MySQL, configurado via Docker Compose.
- **Autenticação:** A autenticação é realizada com JWT. Para testar os endpoints que exigem autenticação, primeiro registre um usuário e utilize o token gerado.
- **Criptografia:** Dados sensíveis como os números dos cartões são criptografados antes de serem armazenados no banco de dados.

## 🤝 Contribuição
Contribuições são bem-vindas! Por favor, faça um fork do repositório e envie um pull request com suas alterações.

## 📄 Licença
Este projeto está licenciado sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

## ⚙️ Como Funciona o Docker Compose
O arquivo `docker-compose.yml` cuida da criação dos containers para a aplicação e o banco de dados MySQL. Durante o comando `docker-compose up --build`, os containers serão criados e configurados automaticamente:
- **MySQL:** O banco de dados é configurado com a senha, nome do banco de dados e usuário predefinidos.
- **Card API:** A aplicação será acessível na porta 8081 da sua máquina local.

_Para resetar completamente a aplicação, rode o comando `docker-compose down -v` e depois execute novamente o comando `docker-compose up --build`. Isso irá parar todos os containers e removerá todos os arquivos de dados do banco de dados._
