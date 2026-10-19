# Who is asking? The api-gateway validated the JWT and added two trusted headers.
# Same idea as HeaderAuthFilter in the Java services.
from dataclasses import dataclass

from fastapi import Header, HTTPException


@dataclass(frozen=True)
class CurrentUser:
    id: int
    role: str  # CUSTOMER, SELLER or ADMIN


def get_current_user(
    x_user_id: str | None = Header(default=None),
    x_user_role: str | None = Header(default=None),
) -> CurrentUser:
    # No headers = the request did not come through the gateway: refuse it.
    if not x_user_id or not x_user_role or not x_user_id.isdigit():
        raise HTTPException(status_code=403, detail="Missing or invalid gateway headers")
    return CurrentUser(id=int(x_user_id), role=x_user_role)
