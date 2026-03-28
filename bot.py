"""
BotGithub - verifica se tem novo commit e dá git pull com sua permissão.
"""

import json
import os
import subprocess
import time

import requests
from dotenv import load_dotenv

load_dotenv()

TELEGRAM_TOKEN = os.getenv("TELEGRAM_TOKEN")
TELEGRAM_CHAT_ID = os.getenv("TELEGRAM_CHAT_ID")
GITHUB_REPO = os.getenv("GITHUB_REPO")  # formato: "owner/repo"
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")
GITHUB_BRANCH = os.getenv("GITHUB_BRANCH", "main")
REPO_PATH = os.getenv("REPO_PATH", ".")
CHECK_INTERVAL = int(os.getenv("CHECK_INTERVAL", "60"))  # segundos
POLLING_SLEEP = 5  # segundos entre cada iteração do loop principal
GIT_PULL_TIMEOUT = 120  # segundos máximos para o git pull


def get_latest_commit():
    """Busca o commit mais recente do repositório via GitHub API."""
    headers = {"Accept": "application/vnd.github.v3+json"}
    if GITHUB_TOKEN:
        headers["Authorization"] = f"token {GITHUB_TOKEN}"

    url = f"https://api.github.com/repos/{GITHUB_REPO}/commits"
    response = requests.get(
        url,
        headers=headers,
        params={"sha": GITHUB_BRANCH, "per_page": 1},
        timeout=10,
    )
    response.raise_for_status()

    commits = response.json()
    if commits:
        commit = commits[0]
        sha = commit["sha"]
        message = commit["commit"]["message"].splitlines()[0]
        author = commit["commit"]["author"]["name"]
        return sha, message, author
    return None, None, None


def send_telegram_message(text, reply_markup=None):
    """Envia uma mensagem via Telegram Bot API."""
    url = f"https://api.telegram.org/bot{TELEGRAM_TOKEN}/sendMessage"
    data = {
        "chat_id": TELEGRAM_CHAT_ID,
        "text": text,
        "parse_mode": "HTML",
    }
    if reply_markup:
        data["reply_markup"] = json.dumps(reply_markup)

    response = requests.post(url, data=data, timeout=10)
    result = response.json()
    if not result.get("ok"):
        print(f"Aviso: falha ao enviar mensagem Telegram: {result.get('description')}")
    return result


def answer_callback_query(callback_query_id, text=None):
    """Responde ao callback de um botão inline."""
    url = f"https://api.telegram.org/bot{TELEGRAM_TOKEN}/answerCallbackQuery"
    data = {"callback_query_id": callback_query_id}
    if text:
        data["text"] = text
    requests.post(url, data=data, timeout=10)


def get_updates(offset=None):
    """Busca atualizações (mensagens/callbacks) do Telegram com long polling."""
    url = f"https://api.telegram.org/bot{TELEGRAM_TOKEN}/getUpdates"
    params = {"timeout": 30, "allowed_updates": json.dumps(["callback_query"])}
    if offset is not None:
        params["offset"] = offset

    response = requests.get(url, params=params, timeout=35)
    return response.json()


def git_pull():
    """Executa git pull no diretório do repositório."""
    result = subprocess.run(
        ["git", "pull"],
        cwd=REPO_PATH,
        capture_output=True,
        text=True,
        timeout=GIT_PULL_TIMEOUT,
    )
    output = (result.stdout + result.stderr).strip()
    return result.returncode == 0, output


def build_commit_message(sha, author, message):
    return (
        f"🔔 <b>Novo commit detectado!</b>\n\n"
        f"📝 <b>Commit:</b> <code>{sha[:7]}</code>\n"
        f"👤 <b>Autor:</b> {author}\n"
        f"💬 <b>Mensagem:</b> {message}\n\n"
        f"Deseja fazer <b>git pull</b>?"
    )


def main():
    last_commit_sha = None
    update_offset = None
    awaiting_approval = False  # True enquanto aguarda resposta do usuário no Telegram

    # Inicializa o SHA do último commit para não acionar pull no primeiro check
    try:
        sha, _, _ = get_latest_commit()
        last_commit_sha = sha
        print(f"Bot iniciado. Commit atual: {sha[:7] if sha else 'N/A'}")
    except Exception as exc:
        print(f"Aviso: não foi possível buscar o commit inicial: {exc}")

    # last_check=0.0 força a verificação imediatamente na primeira iteração
    last_check = 0.0

    while True:
        current_time = time.time()

        # --- Verifica novos commits periodicamente ---
        if current_time - last_check >= CHECK_INTERVAL:
            last_check = current_time
            try:
                sha, message, author = get_latest_commit()
                if sha and sha != last_commit_sha:
                    print(f"Novo commit: {sha[:7]} — {message}")
                    last_commit_sha = sha
                    awaiting_approval = True

                    reply_markup = {
                        "inline_keyboard": [
                            [
                                {
                                    "text": "✅ Sim, fazer pull",
                                    "callback_data": "pull_yes",
                                },
                                {"text": "❌ Não", "callback_data": "pull_no"},
                            ]
                        ]
                    }
                    send_telegram_message(
                        build_commit_message(sha, author, message),
                        reply_markup,
                    )
            except Exception as exc:
                print(f"Erro ao verificar commits: {exc}")

        # --- Verifica respostas do Telegram ---
        if awaiting_approval:
            try:
                updates = get_updates(offset=update_offset)
                if updates.get("ok"):
                    for update in updates["result"]:
                        update_offset = update["update_id"] + 1

                        if "callback_query" not in update:
                            continue

                        callback = update["callback_query"]
                        data = callback.get("data")
                        callback_id = callback["id"]

                        if data == "pull_yes":
                            answer_callback_query(
                                callback_id, "Executando git pull..."
                            )
                            try:
                                success, output = git_pull()
                            except subprocess.TimeoutExpired:
                                send_telegram_message(
                                    "⏱️ <b>Tempo limite excedido ao executar git pull.</b>"
                                )
                                awaiting_approval = False
                                continue
                            if success:
                                send_telegram_message(
                                    f"✅ <b>Git pull executado com sucesso!</b>\n\n"
                                    f"<code>{output}</code>"
                                )
                            else:
                                send_telegram_message(
                                    f"❌ <b>Erro ao executar git pull:</b>\n\n"
                                    f"<code>{output}</code>"
                                )
                            awaiting_approval = False

                        elif data == "pull_no":
                            answer_callback_query(callback_id, "Pull cancelado.")
                            send_telegram_message("❌ Git pull cancelado.")
                            awaiting_approval = False
            except Exception as exc:
                print(f"Erro ao buscar atualizações do Telegram: {exc}")

        time.sleep(POLLING_SLEEP)


if __name__ == "__main__":
    main()
