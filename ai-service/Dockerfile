# Python is interpreted: no compile step, so one stage is enough
# (the Java services need a JDK stage to build the jar first).
FROM python:3.13-slim

LABEL maintainer="Khaldoun Damach" \
      description="AI Shopping Assistant for E-Commerce Microservices"

# No .pyc files inside the image; print logs immediately (no buffering).
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

RUN groupadd -g 1001 appuser \
    && useradd -u 1001 -g appuser -M -s /usr/sbin/nologin appuser

WORKDIR /app

# Dependencies first: this layer stays cached until requirements.txt changes
# (same trick as "mvnw dependency:go-offline" in the Java Dockerfiles).
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app ./app

USER appuser

EXPOSE 8086

# uvicorn is the web server (like the Tomcat inside Spring Boot);
# "app.main:app" = the variable "app" in the file app/main.py.
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8086"]
