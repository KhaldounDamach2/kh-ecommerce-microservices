# Per-user rate limit for the chat (sliding window, kept in memory).
#
# Limits of this simple version: the notebook is empty again after a restart, and
# several ai-service copies would each have their own notebook. For many copies,
# a shared store like Redis (or the gateway's RequestRateLimiter) is the usual answer.
import math
from os import times
import threading
import time

from collections import defaultdict, deque
from fastapi import Depends, HTTPException
from app import config
from app.auth import CurrentUser, get_current_user


class SlidingWindowLimiter:
    def __init__(self, max_requests: int, window_seconds: float):
        self.max_requests = max_requests
        self.window_seconds = window_seconds
        # The "notebook": user id -> times of that user's recent requests, oldest first.
        self._requests: dict[int, deque[float]] = defaultdict(deque)
        # FastAPI runs normal "def" endpoints in several threads at once, so two
        # requests of the same user may arrive together: only one may write at a time.
        self._lock = threading.Lock()

    def check(self, key: int) -> float:
#   Records one request for `key`. Returns 0.0 when it is allowed,
#   otherwise how many seconds the user has to wait.
        now = time.monotonic()

#   2. with the lock: take this user's deque of times
        with self._lock:
            times = self._requests[key]

#   3. remove times older than the window from the LEFT (oldest first)
            while times and times[0] <= now - self.window_seconds:
                times.popleft()

#   4. still max_requests or more left -> return the wait time (oldest + window - now)
            if len(times) >= self.max_requests:
                return times[0] + self.window_seconds - now
        
#   5. otherwise append now and return 0.0
            times.append(now)
            return 0.0
# End of SlidingWindowLimiter class and its usage for chat rate limiting.
chat_limiter = SlidingWindowLimiter(config.CHAT_LIMIT_PER_MINUTE, window_seconds=60.0)


def enforce_chat_limit(user: CurrentUser = Depends(get_current_user)) -> CurrentUser:
    # A dependency that uses another dependency: first "who are you?" (403 if unknown),
    # then "are you allowed to send another message right now?" (429 if not).
    wait_seconds = chat_limiter.check(user.id)
    if wait_seconds > 0:
        raise HTTPException(
            status_code=429,
            detail="You are sending messages too fast. Please wait a moment.",
            headers={"Retry-After": str(math.ceil(wait_seconds))},
        )
    return user
