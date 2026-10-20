# Who may call the chat at all? (No AI involved: refused before anything runs.)


def test_chat_without_gateway_headers_is_refused(client):
    response = client.post("/ai/chat", json={"message": "hi"})

    assert response.status_code == 403


def test_chat_with_a_non_numeric_user_id_is_refused(client):
    headers = {"X-User-Id": "3; DROP TABLE users", "X-User-Role": "CUSTOMER"}

    response = client.post("/ai/chat", json={"message": "hi"}, headers=headers)

    assert response.status_code == 403
