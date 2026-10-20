#   1. import FastAPI and Header from fastapi
import logging

from fastapi import Depends, FastAPI, Header, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from app.llm import ask
from app.auth import CurrentUser, get_current_user
from app.errors import AssistantUnavailableError


#   2. create the app object
# Show our own INFO logs (tool calls) next to uvicorn's logs.
logging.basicConfig(level=logging.INFO, format="%(levelname)s:     %(name)s - %(message)s")

app = FastAPI(title="ai-service")


# Central error mapping (like @ControllerAdvice): the AI provider failed -> 503 with a
# friendly message. The details are already in the log (written in llm.py).
@app.exception_handler(AssistantUnavailableError)
async def assistant_unavailable(request: Request, exc: AssistantUnavailableError) -> JSONResponse:
    return JSONResponse(
        status_code=503,
        content={"detail": "The assistant is not available right now. Please try again in a moment."},
    )

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

#class ChatRequest(BaseModel): is a class that extends BaseModel (Python writes the parent in ( )).
#  It's a DTO, like your Java record LoginRequest(...).
class ChatRequest(BaseModel):
#Field(min_length=1, max_length=2000) is the same as @NotBlank @Size(max = 2000). 
# If the message is empty or too long, FastAPI answers 422 automatically, before your code runs. 
# The max length also protects your free token limit.
    message: str = Field(min_length=1, max_length=2000)

class ChatResponse(BaseModel):
    reply: str

#@app.post(...) is @PostMapping. Because the parameter has the type ChatRequest, FastAPI reads the JSON body into it, like @RequestBody.
@app.post("/ai/chat")
def chat(request: ChatRequest, user: CurrentUser = Depends(get_current_user)) -> ChatResponse:
    return ChatResponse(reply=ask(request.message, user))