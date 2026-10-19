# The only file that knows we use Groq ("provider adapter").
# Everything else calls ask(...), so switching to another AI later means changing only this file.


#   1. import Groq from groq, and the settings from app.config
from groq import Groq
from app import config


#   2. create one Groq client (with a timeout)
client = Groq(api_key=config.GROQ_API_KEY, timeout=30.0)


#   3. write the SYSTEM_PROMPT
SYSTEM_PROMPT = (
    "You are the shopping assistant of an online shop. "
    "Answer briefly and politely, in the language the customer uses. "
    "If you do not know something, say so. Never invent products, prices or orders."
    "Right now you can only have a general conversation: you cannot look up products, "
    "prices or orders yet, and this shop has no returns or exchanges feature. "

)
#   4. def ask(user_message: str) -> str: build the messages, call Groq, return the answer text
def ask(user_message: str) -> str:
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": user_message},
    ]
    response = client.chat.completions.create(
        model=config.GROQ_MODEL,
        messages=messages,
    )
    return response.choices[0].message.content