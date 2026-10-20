# Settings come from environment variables (12-factor, like the Java services).
# Docker: docker-compose passes them in from .env.
# Local:  python -m uvicorn app.main:app --reload --port 8086 --env-file ../.env
import os

GROQ_API_KEY = os.environ.get("GROQ_API_KEY", "")
GROQ_MODEL = os.environ.get("GROQ_MODEL", "openai/gpt-oss-20b")

# Where the tools find the other services. The default fits a local run
# (ports published on localhost); docker-compose sets the container names.
PRODUCT_SERVICE_URL = os.environ.get("PRODUCT_SERVICE_URL", "http://localhost:8083")
ORDER_SERVICE_URL = os.environ.get("ORDER_SERVICE_URL", "http://localhost:8084")

# Time limits, from the outside in: nginx (frontend) gives up after 60 s, so one whole
# chat must finish well before that, and every single call inside it even earlier.
CHAT_TIME_BUDGET_SECONDS = 40.0  # everything for one chat: all Groq rounds + tools
GROQ_TIMEOUT_SECONDS = 15.0  # one Groq call (and never more than the budget has left)
TOOL_TIMEOUT_SECONDS = 5.0  # one call to product-service / order-service

# Fail fast at startup with a clear message (same idea as JWT_SECRET in auth-service).
if not GROQ_API_KEY:
    raise RuntimeError("GROQ_API_KEY is not set. Put it in the .env file in the project root.")
