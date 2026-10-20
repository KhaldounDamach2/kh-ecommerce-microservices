# Stand-ins for the outside world: a fake Groq and helpers to build its answers.
# The real Groq SDK returns objects with attributes (response.choices[0].message...);
# SimpleNamespace gives us objects with exactly those attributes, nothing more.
import json
from types import SimpleNamespace

CUSTOMER = {"X-User-Id": "3", "X-User-Role": "CUSTOMER"}
SELLER = {"X-User-Id": "15", "X-User-Role": "SELLER"}


def text_reply(text: str) -> SimpleNamespace:
    """Groq's final answer: plain text, no tool request."""
    message = SimpleNamespace(content=text, tool_calls=None)
    return SimpleNamespace(choices=[SimpleNamespace(message=message)])


def tool_request(name: str, arguments: dict, call_id: str = "call_1") -> SimpleNamespace:
    """Groq asks us to run one tool (arguments arrive as a JSON string, like for real)."""
    call = SimpleNamespace(
        id=call_id,
        function=SimpleNamespace(name=name, arguments=json.dumps(arguments)),
    )
    message = SimpleNamespace(content=None, tool_calls=[call])
    return SimpleNamespace(choices=[SimpleNamespace(message=message)])


class FakeGroq:
    """Plays Groq: returns the scripted answers in order and remembers every request.

    An answer can also be an exception: then it is raised, like a real Groq failure.
    """

    def __init__(self, *answers):
        self.answers = list(answers)
        self.requests: list[dict] = []

    def create(self, **kwargs):
        # Copy the message list: ask() keeps appending to the same list afterwards.
        self.requests.append({**kwargs, "messages": list(kwargs["messages"])})
        answer = self.answers.pop(0)
        if isinstance(answer, Exception):
            raise answer
        return answer


def spring_page(items: list[dict], total: int | None = None) -> dict:
    """The JSON shape of a Spring Data Page, as product-service and order-service send it."""
    return {"content": items, "totalElements": len(items) if total is None else total}
