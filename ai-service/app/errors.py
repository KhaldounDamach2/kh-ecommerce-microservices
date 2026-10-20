# Our own exceptions. llm.py raises them, main.py turns them into HTTP answers
# (like a custom exception + @ControllerAdvice in Spring).


class AssistantUnavailableError(Exception):
    """The AI provider failed (timeout, connection, 4xx/5xx, limit reached)."""
