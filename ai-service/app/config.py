# Settings come from environment variables (12-factor, like the Java services).
# Docker: docker-compose passes them in from .env.
# Local:  uvicorn app.main:app --reload --port 8086 --env-file ../.env
import os

GROQ_API_KEY = os.environ.get("GROQ_API_KEY", "")
GROQ_MODEL = os.environ.get("GROQ_MODEL", "openai/gpt-oss-20b")

# Fail fast at startup with a clear message (same idea as JWT_SECRET in auth-service).
if not GROQ_API_KEY:
    raise RuntimeError("GROQ_API_KEY is not set. Put it in the .env file in the project root.")
