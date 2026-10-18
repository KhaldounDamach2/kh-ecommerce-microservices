# ai-service entry point (Step A1: skeleton, no AI yet).
#
#   1. import FastAPI and Header from fastapi
from fastapi import FastAPI, Header

#   2. create the app object
app = FastAPI(title="ai-service")

#   3. GET /health      -> {"status": "UP"}               (Docker healthcheck, not via the gateway)
@app.get("/health")
def health():
    return {"status": "UP"}

#   4. GET /ai/whoami   -> the X-User-Id and X-User-Role headers the gateway added
@app.get("/ai/whoami")
def whoami(
    x_user_id: str | None = Header(default=None), 
    x_user_role: str | None = Header(default=None),
):
    return {"userId": x_user_id, "role": x_user_role}
