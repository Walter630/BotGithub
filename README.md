# BotGithub

Esse bot verifica se tem novo commit no GitHub e faz `git pull` com sua permissão via Telegram.

## Como funciona

1. O bot consulta periodicamente a API do GitHub em busca de novos commits.
2. Ao detectar um novo commit, envia uma notificação no Telegram com os detalhes (SHA, autor e mensagem).
3. Você aprova ou rejeita o `git pull` clicando em um botão inline na mensagem.
4. Se aprovado, o bot executa `git pull` no diretório configurado e envia o resultado.

## Pré-requisitos

- Python 3.8+
- Um [Telegram Bot](https://core.telegram.org/bots#botfather) (crie via `@BotFather`)
- Seu `chat_id` do Telegram (descubra via `@userinfobot`)

## Instalação

```bash
# 1. Clone o repositório
git clone https://github.com/Walter630/BotGithub.git
cd BotGithub

# 2. Instale as dependências
pip install -r requirements.txt

# 3. Configure as variáveis de ambiente
cp .env.example .env
# Edite o arquivo .env com seus dados
```

## Configuração (`.env`)

| Variável          | Obrigatória | Descrição                                              |
|-------------------|-------------|--------------------------------------------------------|
| `TELEGRAM_TOKEN`  | ✅          | Token do seu bot Telegram (`@BotFather`)               |
| `TELEGRAM_CHAT_ID`| ✅          | ID do chat onde as notificações serão enviadas          |
| `GITHUB_REPO`     | ✅          | Repositório a monitorar (`dono/repositorio`)           |
| `GITHUB_TOKEN`    | ❌          | Token do GitHub (necessário para repositórios privados) |
| `GITHUB_BRANCH`   | ❌          | Branch a monitorar (padrão: `main`)                    |
| `REPO_PATH`       | ❌          | Caminho local do repositório (padrão: `.`)             |
| `CHECK_INTERVAL`  | ❌          | Intervalo de verificação em segundos (padrão: `60`)    |

## Uso

```bash
python bot.py
```

O bot ficará rodando continuamente, verificando novos commits no intervalo configurado.
