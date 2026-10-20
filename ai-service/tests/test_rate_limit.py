# The per-user rate limit. YOUR tests: see the explanation in the chat.
from app.rate_limit import SlidingWindowLimiter
from tests.fakes import CUSTOMER

# 1: test_allows_up_to_the_limit_then_asks_to_wait
def test_allows_up_to_the_limit_then_asks_to_wait():
    # Create a sliding window limiter that allows 3 requests per 60 seconds.
    limiter = SlidingWindowLimiter(3, window_seconds=60.0)
    # Send 3 requests, which should all be allowed.
    results = [limiter.check(1) for _ in range(3)]
    # The fourth request should be blocked and return a positive wait time.
    assert results == [0.0, 0.0, 0.0]
    # The fourth request should be blocked and return a positive wait time.
    assert limiter.check(1) > 0

# 2: test_every_user_has_their_own_limit
def test_every_user_has_their_own_limit():
    limiter = SlidingWindowLimiter(3, window_seconds=60.0)
    for _ in range(3):
        limiter.check(1)           # user 1 uses up all 3 requests
    assert limiter.check(2) ==0.0  # user 2 is still allowed
    assert limiter.check(1) > 0    # user 1 must still wait

# 3: test_eleventh_message_in_a_minute_gets_429_with_retry_after (uses `client`)
def test_eleventh_message_in_a_minute_gets_429_with_retry_after(client):
    # Send 10 requests, which should all be allowed.
    for _ in range(10):
        client.post("/ai/chat", json={"message": ""}, headers=CUSTOMER)

    # The 11th request should be blocked.
    response = client.post("/ai/chat", json={"message": ""}, headers=CUSTOMER) # 11th request
    
    # Assert that the response indicates the user must wait before sending more requests.
    assert response.status_code == 429
    assert int(response.headers["Retry-After"]) > 0
