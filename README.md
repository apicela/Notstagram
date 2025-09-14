# 📸 Notstagram API

A **Notstagram API** é uma aplicação backend que simula uma rede social de compartilhamento de fotos e vídeos, inspirada
no Instagram.  
Permite autenticação de usuários, gerenciamento de seguidores e upload de postagens com mídia.

---

## ✨ Funcionalidades

- ⚡ **Segurança**
    - Autenticação baseada em **JWT (JSON Web Token)**.

- 🔑 **Autenticação e Registro**
    - Login com geração de *Access Token* e *Refresh Token*.
    - Renovação de sessão via refresh token.
    - Fluxo completo de cadastro e recuperação de senha via e-mail (código de verificação, confirmação e definição de credenciais).

- 👤 **Usuários**
    - Obter um perfil de usuário.
    - Editar perfil de usuário.
    - Seguir e deixar de seguir outros usuários.
    - Ativar e desativar perfis.

- 📝 **Posts**
    - Fazer upload de posts com mídia.
    - Interagir com posts (Comentários, like, dislike, etc.).
    - Visualizar feed de postagens de vídeos e fotos dos usuários seguidos.


---

## 👤 User Controller

### **POST** `/users/{username}/follow`

Seguir um usuário.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Path Param**: `username` (string)
- **Responses**:
    - `204` Usuário seguido com sucesso
    - `404` Usuário não encontrado

---

### **DELETE** `/users/{username}/follow`

Deixar de seguir um usuário.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Path Param**: `username` (string)
- **Responses**:
    - `204` Usuário deixado de seguir com sucesso
    - `404` Usuário não encontrado

---

### **POST** `/users/me/activate`

Ativa o usuário autenticado.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Responses**:
    - `204` Usuário ativado com sucesso

---

### **DELETE** `/users/me/deactivate`

Desativa o usuário autenticado.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Responses**:
    - `204` Usuário desativado com sucesso

---

## 📝 Post Controller

### **POST** `/posts/upload`

Cria um novo post.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Body**:
    - `file` (string - binary)
    - `description` (string)
- **Responses**:
    - `204` Post criado com sucesso
    - `400` Invalid content type
    - `500` Internal server error

---

### **GET** `/posts/{id}`

Busca post por ID (UUID).

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Path Param**: `id` (UUID)
- **Responses**:
    - `200` Post encontrado

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "description": "string",
  "mediaUrl": "string",
  "type": "IMAGE",
  "contentType": "string",
  "createdAt": "2025-09-13T01:49:36.296Z",
  "user": {
    "username": "string"
  },
  "likesCount": 0,
  "likedByMe": true,
  "commentsCount": 0,
  "recentComments": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "text": "string",
      "createdAt": "2025-09-13T01:49:36.296Z",
      "user": {
        "username": "string"
      }
    }
  ]
}
```

- `404` Post não encontrado

---

### **GET** `/posts/media/{id}`

Obtém a mídia de um post.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Path Param**: `id` (UUID)
- **Responses**:
    - `200` Conteúdo binário da mídia (`image/png`, `image/jpeg`, `video/mp4`, etc.)
    - `404` Mídia não encontrada

---

### **GET** `/posts/feed`

Retorna o feed de postagens dos usuários seguidos pelo usuário autenticado.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Responses**:
    - `200` Feed carregado com sucesso (retorna `List<PostDTO>`)

```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "description": "string",
    "mediaUrl": "string",
    "type": "IMAGE",
    "contentType": "string",
    "createdAt": "2025-09-13T01:49:36.301Z",
    "user": {
      "username": "string"
    },
    "likesCount": 0,
    "likedByMe": true,
    "commentsCount": 0,
    "recentComments": [
      {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "text": "string",
        "createdAt": "2025-09-13T01:49:36.301Z",
        "user": {
          "username": "string"
        }
      }
    ]
  }
]
```

- `401` Usuário não autenticado

---

## 🔑 Auth Controller

### **POST** `/auth/register/email`

Inicia o processo de registro de usuário.

- **Body**:

```json
{
  "email": "usuario@email.com"
}
```

- **Responses**:
    - `201` Usuário pendente criado, código enviado por email

```json
{
  "acessToken": "string",
  "refreshToken": "string",
  "access_expires_in": 0
}
```

- `400` Email já está em uso

---

### **POST** `/auth/register/confirm`

Confirma o email do usuário.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>`. access_token retorndo por `/auth/register/email`
- **Query Param**: `verificationCode`

```json
{
  "code": 123456
}
```

- **Responses**:
    - `200` Email confirmado com sucesso

```json
{
  "acessToken": "string",
  "refreshToken": "string",
  "access_expires_in": 0
}
```

- `400` Código de verificação inválido
- `401` Token inválido ou ausente

---

### **POST** `/auth/register/complete`

Conclui o registro do usuário.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `/auth/register/confirm`
- **Body**:

```json
{
  "username": "string",
  "password": "string",
  "publicProfile": true
}
```

- **Responses**:
    - `200` Registro concluído com sucesso

```json
{
  "acessToken": "string",
  "refreshToken": "string",
  "access_expires_in": 0
}
```

- `400` Nome de usuário já em uso
- `401` Token inválido ou ausente

---

### **POST** `/auth/login`

Autentica o usuário e gera tokens de acesso.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Body**:

```json
{
  "email": "usuario@email.com",
  "password": "123"
}
```

- **Responses**:
    - `200` Login realizado com sucesso

```json
{
  "acessToken": "string",
  "refreshToken": "string",
  "access_expires_in": 0
}
```

- `401` Credenciais inválidas

---

### **POST** `/auth/refresh`

Gera novo access token.

- **Headers necessários**:
    - `Authorization: Bearer <access_token>` access_token retornado por `auth/login`
- **Body**:

```json
{
  "refreshToken": "string"
}
```

- **Responses**:
    - `200` Novo access token gerado

```json
{
  "acessToken": "string",
  "refreshToken": "string",
  "access_expires_in": 0
}
```

- `401` Refresh token inválido ou expirado

---
