# pytest loads this file automatically. Fixtures defined here can be used by any test
# simply by naming them as a parameter (like @Autowired, but per test).
import os

# config.py refuses to start without a key. No test talks to the real Groq.
os.environ.setdefault("GROQ_API_KEY", "test-key-not-real")

from types import SimpleNamespace  # noqa: E402  (imports after setting the key on purpose)

import httpx  # noqa: E402
import pytest  # noqa: E402
from fastapi.testclient import TestClient  # noqa: E402

from app import llm, rate_limit, tools  # noqa: E402
from app.main import app  # noqa: E402
from tests.fakes import FakeGroq  # noqa: E402


@pytest.fixture
def client() -> TestClient:
    """Calls the FastAPI app directly, in the same process: no server, no port."""
    return TestClient(app)


@pytest.fixture(autouse=True)
def fresh_rate_limiter(monkeypatch):
    """autouse: runs for EVERY test, so each test starts with an empty notebook."""
    monkeypatch.setattr(
        rate_limit,
        "chat_limiter",
        rate_limit.SlidingWindowLimiter(rate_limit.config.CHAT_LIMIT_PER_MINUTE, window_seconds=60.0),
    )


@pytest.fixture
def fake_groq(monkeypatch):
    """Usage in a test: fake = fake_groq(answer1, answer2, ...)."""

    def install(*answers) -> FakeGroq:
        fake = FakeGroq(*answers)
        fake_client = SimpleNamespace(chat=SimpleNamespace(completions=SimpleNamespace(create=fake.create)))
        monkeypatch.setattr(llm, "client", fake_client)  # undone automatically after the test
        return fake

    return install


@pytest.fixture
def fake_services(monkeypatch):
    """Usage in a test: seen = fake_services(handler), where handler(request) -> httpx.Response.

    Replaces the shared HTTP client of the tools, so product-service and order-service
    calls go to the test's handler instead of the network. `seen` collects every request.
    """

    def install(handler) -> list[httpx.Request]:
        seen: list[httpx.Request] = []

        def recording_handler(request: httpx.Request) -> httpx.Response:
            seen.append(request)
            return handler(request)

        monkeypatch.setattr(tools, "http", httpx.Client(transport=httpx.MockTransport(recording_handler)))
        return seen

    return install
